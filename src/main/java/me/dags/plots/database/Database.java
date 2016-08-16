package me.dags.plots.database;

import me.dags.plots.Plots;
import me.dags.plots.database.table.Table;
import me.dags.plots.plot.PlotUser;
import org.spongepowered.api.Sponge;

import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class Database {

    private final String database;
    private final Object plugin;

    public Database(Object plugin, String database) {
        this.plugin = plugin;
        this.database = database;
    }

    public void loadUser(final String world, final UUID uuid, final Consumer<PlotUser> callback) {
        Plots.log("[DB] Loading user: {} for world: {}", uuid, world);
        Sponge.getScheduler().createTaskBuilder()
                .async()
                .execute(() -> {
                    try {
                        Table table = User.createUserTable(database, uuid);
                        PlotUser.Builder userBuilder = PlotUser.builder().uuid(uuid).world(world);

                        table.get()
                                .select("*")
                                .lookupKey(Keys.PLOT_WORLD)
                                .lookupVal(world)
                                .consumer(resultSet -> User.userBuilderPopulator(userBuilder, resultSet))
                                .submit();

                        scheduleCallback(userBuilder.build(), callback);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .submit(plugin);
    }

    public void saveUser(PlotUser plotUser) {
        Plots.log("[DB] Saving user: {} for world: {}", plotUser.getUuid(), plotUser.getWorld());
        Sponge.getScheduler().createTaskBuilder()
                .async()
                .execute(() -> {
                    try {
                        Table table = User.createUserTable(database, plotUser.getUuid());
                        User.plotEntryPopulator(table, plotUser);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .submit(plugin);
    }

    private <T> void scheduleCallback(T result, Consumer<T> callback) {
        Sponge.getScheduler().createTaskBuilder().execute(() -> callback.accept(result)).submit(plugin);
    }
}
