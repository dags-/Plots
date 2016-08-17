package me.dags.plots.database;

import me.dags.plots.Plots;
import me.dags.plots.database.statment.*;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotMeta;
import me.dags.plots.plot.PlotUser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class Database {

    private final String database;
    private final Object plugin;

    private SpongeExecutorService service;
    private DataSource dataSource;

    public Database(Object plugin, String database) {
        this.plugin = plugin;
        this.database = database;
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

    public void createTable(Table table) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                Plots.log("Table: {}", table.getStatement());

                connection.createStatement().execute(table.getStatement());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public <T> void select(Select<T> select, Consumer<T> callback) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                Plots.log("Select: {}", select.getStatement());
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

    private void update(Statement statement, Consumer<Boolean> callback) {
        getService().execute(() -> {
            try (Connection connection = getDataSource().getConnection()) {
                Plots.log("Update: {}", statement.getStatement());

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
}
