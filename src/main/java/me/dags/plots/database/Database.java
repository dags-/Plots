package me.dags.plots.database;

import me.dags.plots.Plots;
import me.dags.plots.database.statment.*;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotMeta;
import me.dags.plots.plot.PlotUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.util.Tristate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class Database {

    private static final Logger logger = LoggerFactory.getLogger(Plots.ID + "_db");

    private final String database;
    private final Object plugin;
    private volatile boolean log;

    private SpongeExecutorService service;
    private DataSource dataSource;

    public Database(Object plugin, String database) {
        this.plugin = plugin;
        this.database = database;
    }

    public void init() {
        this.log = Plots.getConfig().logDatabase();
    }

    public synchronized void close() {
        try {
            log("Shutting down executor service");
            service.shutdown();

            log("Awaiting termination");
            service.awaitTermination(1, TimeUnit.SECONDS);
        } catch (Throwable t) {
            log("Error during shutdown:\n{}", t.getMessage());
        }
    }

    private SpongeExecutorService getService() {
        if (service == null) {
            service = Sponge.getScheduler().createAsyncExecutor(plugin);
        }
        return service;
    }

    public DataSource getDataSource() throws SQLException {
        if (dataSource == null) {
            dataSource = Sponge.getServiceManager().provideUnchecked(SqlService.class).getDataSource(database);
        }
        return dataSource;
    }

    public void loadWorld(String world) {
        getService().execute(() -> {
            Table builder = new Table.Builder()
                    .name(world)
                    .primary(Keys.UID)
                    .column(Keys.UID, "VARCHAR(56)")
                    .column(Keys.USER_ID, "VARCHAR(40)")
                    .column(Keys.PLOT_ID, "VARCHAR(16)")
                    .column(Keys.META_NAME, "VARCHAR(16)")
                    .column(Keys.META_OWNER, "BOOLEAN")
                    .build();

            createTable(builder);
        });
    }

    public void loadUser(String world, UUID uuid, Consumer<PlotUser> callback) {
        Select<PlotUser> select = Queries.selectUser(world, uuid).build();
        select(select, callback);
    }

    public void saveUser(PlotUser user) {
        getService().execute(() -> {
            try (Connection connection = dataSource.getConnection()) {
                for (Map.Entry<PlotId, PlotMeta> entry : user.getPlots()) {
                    Insert insert = Queries.updateUserPlot(user, entry.getKey(), entry.getValue()).build();

                    try (java.sql.Statement stmt = connection.createStatement()) {
                        stmt.executeUpdate(insert.getStatement());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void deletePlot(String world, PlotId plotId, Consumer<Set<UUID>> callback) {
        getService().execute(() -> {
            Select<Set<UUID>> whitelisted = Queries.selectWhitelistedUsers(world, plotId).build();
            try (Connection connection = getDataSource().getConnection()) {
                ResultSet resultSet = connection.createStatement().executeQuery(whitelisted.getStatement());
                Set<UUID> whitelist = whitelisted.transform(resultSet);
                for (UUID uuid : whitelist) {
                    Delete delete = new Delete.Builder().in(world).where(Where.of(Keys.UID, "=" ,Keys.uid(uuid, plotId)).build()).build();

                    try (java.sql.Statement stmt = connection.createStatement()) {
                        stmt.execute(delete.getStatement());
                    }
                }
                scheduleCallback(whitelist, callback);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void findFreePlot(String world, PlotId closest, Consumer<PlotId> callback) {
        getService().execute(() -> {
            try {
                PlotId plotId = findFreePlot(world, closest);
                scheduleCallback(plotId, callback);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void createTable(Table table) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                log("Table: {}", table.getStatement());

                try (java.sql.Statement stmt = connection.createStatement()) {
                    stmt.execute(table.getStatement());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public <T> void selectAndUpdate(Select<T> select, Consumer<Tristate> updateCallback) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                log("Select: {}", select.getStatement());

                try (java.sql.Statement stmt = connection.createStatement()) {
                    ResultSet resultSet = stmt.executeQuery(select.getStatement());
                    T result = select.transform(resultSet);
                    Optional<Statement> statement = select.andUpdate(result);

                    if (statement.isPresent()) {
                        update(statement.get(), updateCallback);
                    } else {
                        scheduleCallback(Tristate.UNDEFINED, updateCallback);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public <T> void select(Select<T> select, Consumer<T> callback) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                log("Select: {}", select.getStatement());

                try (java.sql.Statement stmt = connection.createStatement()) {
                    ResultSet resultSet = stmt.executeQuery(select.getStatement());
                    T result = select.transform(resultSet);
                    if (result != null) {
                        scheduleCallback(result, callback);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void update(Statement statement, Consumer<Tristate> callback) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                log("Update: {}", statement.getStatement());

                try (java.sql.Statement stmt = connection.createStatement()) {
                    int result = stmt.executeUpdate(statement.getStatement());
                    scheduleCallback(Tristate.fromBoolean(result != 0), callback);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void contains(Select lookup, Consumer<Boolean> callback) {
        getService().execute(() -> {
            try {
                boolean result = contains(lookup);
                scheduleCallback(result, callback);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean contains(Select lookup) throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            try (java.sql.Statement stmt = connection.createStatement()) {
                return stmt.executeQuery(lookup.getStatement()).next();
            }
        }
    }

    private PlotId findFreePlot(String world, PlotId closest) throws SQLException {
        int x = closest.plotX(), z = closest.plotZ(), xMax = x, xMin = x, zMax = z, zMin = z;
        int timeout = 10000;
        for (int d = 1; d < timeout; d++) {
            xMax += d; xMin -= d; zMax += d; zMin -= d;
            while (x < xMax) {
                if (!plotIsClaimed(world, x, z)) {
                    return new PlotId(x, z);
                }
                x++;
            }
            while (z < zMax) {
                if (!plotIsClaimed(world, x, z)) {
                    return new PlotId(x, z);
                }
                z++;
            }
            while (x > xMin) {
                if (!plotIsClaimed(world, x, z)) {
                    return new PlotId(x, z);
                }
                x--;
            }
            while (z > zMin) {
                if (!plotIsClaimed(world, x, z)) {
                    return new PlotId(x, z);
                }
                z--;
            }
        }
        return PlotId.EMPTY;
    }

    private boolean plotIsClaimed(String world, int x, int z) throws SQLException {
        String id = PlotId.string(x, z);

        Select select = Select.builder()
                .select(Keys.PLOT_ID)
                .from(world)
                .where(Where.of(Keys.PLOT_ID, "=", id).build())
                .build();

        return contains(select);
    }

    private <T> void scheduleCallback(T result, Consumer<T> callback) {
        Sponge.getScheduler().createTaskBuilder().execute(() -> callback.accept(result)).submit(plugin);
    }

    private void log(String message, Object... args) {
        if (log) {
            logger.debug(message, args);
        }
    }
}
