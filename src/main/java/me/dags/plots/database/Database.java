package me.dags.plots.database;

import me.dags.plots.PlotsPlugin;
import me.dags.plots.database.statment.*;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotMeta;
import me.dags.plots.plot.PlotUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class Database {

    private static final Logger logger = LoggerFactory.getLogger(PlotsPlugin.ID + "_db");

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
        this.log = PlotsPlugin.getConfig().logDatabase();
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

    public void updateUser(PlotUser user, PlotId plotId) {
        if (user.isWhitelisted(plotId)) {
            Insert builder = Queries.updateUserPlot(user, plotId, user.getMeta(plotId)).build();
            update(builder, b -> {});
        } else {
            Delete builder = Queries.deleteUserPlot(user, plotId).build();
            update(builder, b -> {});
        }
    }

    public void saveUser(PlotUser user) {
        getService().execute(() -> {
            try (Connection connection = dataSource.getConnection()) {
                for (Map.Entry<PlotId, PlotMeta> entry : user.getPlots()) {
                    Insert insert = Queries.updateUserPlot(user, entry.getKey(), entry.getValue()).build();
                    connection.createStatement().executeUpdate(insert.getStatement());
                    connection.commit();
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
                    connection.createStatement().execute(delete.getStatement());
                }
                scheduleCallback(whitelist, callback);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void createTable(Table table) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                log("Table: {}", table.getStatement());

                connection.createStatement().execute(table.getStatement());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public <T> void select(Select<T> select, Consumer<T> callback) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                log("Select: {}", select.getStatement());
                ResultSet resultSet = connection.createStatement().executeQuery(select.getStatement());
                T result = select.transform(resultSet);
                if (result != null) {
                    scheduleCallback(result, callback);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void update(Statement statement, Consumer<Boolean> callback) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                log("Update: {}", statement.getStatement());

                int result = connection.createStatement().executeUpdate(statement.getStatement());
                scheduleCallback(result != 0, callback);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
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
