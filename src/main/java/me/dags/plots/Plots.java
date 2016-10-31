package me.dags.plots;

import com.google.inject.Inject;
import com.mongodb.MongoClient;
import me.dags.commandbus.CommandBus;
import me.dags.plots.command.plot.Approve;
import me.dags.plots.command.plot.Auto;
import me.dags.plots.command.plot.Claim;
import me.dags.plots.command.plot.Unclaim;
import me.dags.plots.database.WorldDatabase;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Executor;
import me.dags.plots.util.IO;
import me.dags.plots.util.Support;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = Plots.ID, name = Plots.ID, version = "0.1")
public class Plots {

    public static final String ID = "plots";

    private static final Logger logger = LoggerFactory.getLogger(ID);
    private static Plots instance;

    private final PlotsApi plots = new PlotsApi(this);
    private final Executor executor = new Executor(this);
    private final MongoClient client = new MongoClient("127.0.0.1", 4567);
    final Path configDir;

    private Config config;

    @Inject
    public Plots(@ConfigDir(sharedRoot = false) Path configDir) {
        Plots.instance = this;
        this.configDir = configDir;
    }

    @Listener
    public void init(GameInitializationEvent event) {
        config = IO.getConfig(configDir.resolve("config.conf"));

        API().reloadGenerators();
        API().loadWorldGenerators();

        CommandBus.builder().logger(logger).build()
                .register(Approve.class, Auto.class, Claim.class, Unclaim.class)
                .submit(this);

        Sponge.getScheduler().createTaskBuilder()
                .execute(Support.of("WorldEdit", "com.sk89q.worldedit.WorldEdit", "me.dags.plots.worldedit.WESessionListener"))
                .submit(this);

        Sponge.getScheduler().createTaskBuilder()
                .execute(Support.of("VoxelSniper", "com.thevoxelbox.voxelsniper.brush.mask.Mask", "me.dags.plots.voxelsniper.SniperListener"))
                .submit(this);
    }

    @Listener (order = Order.POST)
    public void onWorldLoad(LoadWorldEvent event) {
        World world = event.getTargetWorld();
        if (world.getWorldGenerator().getBaseGenerationPopulator() instanceof PlotGenerator) {
            PlotGenerator plotGenerator = (PlotGenerator) world.getWorldGenerator().getBaseGenerationPopulator();
            WorldDatabase database = new WorldDatabase(client.getDatabase(world.getName().toLowerCase()));
            PlotWorld plotWorld = new PlotWorld(world, database, plotGenerator.plotSchema());
            Plots.API().registerPlotWorld(plotWorld);
        }
    }

    @Listener (order = Order.EARLY)
    public void onWorldUnload(UnloadWorldEvent event) {
        World world = event.getTargetWorld();
        if (world.getWorldGenerator().getBaseGenerationPopulator() instanceof PlotGenerator) {
            Plots.API().removePlotWorld(world.getName());
        }
    }

    @Listener (order = Order.EARLY)
    public void onShutDown(GameStoppingServerEvent event) {
        client.close();
        API().dispatcher().finishAll();
    }

    public static PlotsApi API() {
        return instance.plots;
    }

    public static Executor executor() {
        return instance.executor;
    }

    public static Config config() {
        return instance.config;
    }

    public static String toGeneratorId(String name) {
        return String.format("%s:%s", ID, name.toLowerCase());
    }

    public static void log(String message, Object... args) {
        logger.info(message, args);
    }

    public static void submitTask(Task.Builder builder) {
        builder.submit(instance);
    }
}
