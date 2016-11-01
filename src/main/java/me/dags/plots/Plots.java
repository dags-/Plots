package me.dags.plots;

import com.google.inject.Inject;
import com.mongodb.MongoClient;
import me.dags.commandbus.CommandBus;
import me.dags.plots.command.plot.*;
import me.dags.plots.database.WorldDatabase;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Executor;
import me.dags.plots.util.IO;
import me.dags.plots.util.Support;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.Plugin;
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

    private final PlotsApi plots;
    private final Executor executor;
    private final MongoClient client;
    final Path configDir;

    private Config config;

    @Inject
    public Plots(@ConfigDir(sharedRoot = false) Path configDir) {
        // TODO: for testing purposes, to be removed!
        TestServer.start();

        Plots.instance = this;
        this.configDir = configDir;
        this.plots = new PlotsApi(this);
        this.executor  = new Executor(this);
        this.client = new MongoClient("127.0.0.1", 8080);
    }

    @Listener
    public void init(GameInitializationEvent event) {
        config = IO.getConfig(configDir.resolve("config.conf"));

        API().reloadGenerators();
        API().loadWorldGenerators();

        CommandBus.builder().logger(logger).build()
                .register(Add.class)
                .register(Alias.class)
                .register(Approve.class)
                .register(Auto.class)
                .register(Biome.class)
                .register(Claim.class)
                .register(Copy.class)
                .register(Info.class)
                .register(Like.class)
                .register(Likes.class)
                .register(List.class)
                .register(Maskall.class)
                .register(Remove.class)
                .register(Reset.class)
                .register(Teleport.class)
                .register(Unclaim.class)
                .register(Unlike.class)
                .submit(this);

        executor().sync(Support.of("WorldEdit", "com.sk89q.worldedit.WorldEdit", "me.dags.plots.worldedit.WESessionListener"));
        executor().sync(Support.of("VoxelSniper", "com.thevoxelbox.voxelsniper.brush.mask.Mask", "me.dags.plots.voxelsniper.SniperListener"));
    }

    @Listener (order = Order.POST)
    public void onWorldLoad(LoadWorldEvent event) {
        World world = event.getTargetWorld();
        if (world.getWorldGenerator().getBaseGenerationPopulator() instanceof PlotGenerator) {
            System.out.println("Found plotworld " + world.getName());
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
        executor().close();
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

    public static void log(String message, Object... args) {
        logger.info(message, args);
    }
}
