package me.dags.plots;

import com.google.inject.Inject;
import com.mongodb.MongoClient;
import me.dags.commandbus.CommandBus;
import me.dags.plots.command.Cmd;
import me.dags.plots.command.gen.*;
import me.dags.plots.command.plot.*;
import me.dags.plots.command.world.WorldCreate;
import me.dags.plots.command.world.WorldSpawn;
import me.dags.plots.command.world.WorldTP;
import me.dags.plots.command.world.WorldWeather;
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
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.weather.Weathers;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = Plots.ID, name = Plots.ID, version = "1.0", description = "shh")
public class Plots {

    public static final String ID = "plots";

    private static final Logger logger = LoggerFactory.getLogger("PLOTS");
    private static Plots instance;

    private final boolean enabled;
    private final PlotsCore plots;
    private final Cause plotsCause;
    private final Executor executor;
    private final MongoClient client;
    final Path configDir;

    private Config config;

    private boolean safeMode() {
        return !enabled || client == null;
    }

    @Inject
    public Plots(@ConfigDir(sharedRoot = false) Path configDir, PluginContainer container) {
        final Config.Database database = IO.getConfig(configDir.resolve("config.conf")).database();

        boolean enabled = false;
        MongoClient client = null;
        try {
            client = new MongoClient(database.address(), database.port());
            client.getAddress();
            enabled = true;
        } catch (Exception e) {
            client = null;
            enabled = false;
            critical("MONGO DATABASE NOT AVAILABLE ON {}:{} - PLOTS SET TO SAFE-MODE", database.address(), database.port());
        } finally {
            Plots.instance = this;
            this.configDir = configDir;
            this.plots = new PlotsCore(this);
            this.executor  = new Executor(this);
            this.client = client;
            this.enabled = client != null && enabled;
            this.plotsCause = Cause.source(container).build();

            System.out.println(plotsCause);
        }
    }

    @Listener
    public void init(GameInitializationEvent event) {
        config = IO.getConfig(configDir.resolve("config.conf"));

        if (!safeMode()) {
            IO.writeConfig(config, configDir.resolve("config.conf"));
        }

        Cmd.setFormat(config.formatter());

        core().reloadGenerators();
        core().loadWorldGenerators();

        if (safeMode()) {
            log("Running in Safe Mode, commands will not be registered");
            return;
        }

        CommandBus commandBus = CommandBus.builder().logger(logger).build();

        commandBus.register(Add.class)
                .register(Alias.class)
                .register(Approve.class)
                .register(Auto.class)
                .register(Biome.class)
                .register(Claim.class)
                .register(Copy.class)
                .register(Export.class)
                .register(Highlight.class)
                .register(Info.class)
                .register(Like.class)
                .register(Likes.class)
                .register(Likers.class)
                .register(ListPlots.class)
                .register(Maskall.class)
                .register(Proof.class)
                .register(Remove.class)
                .register(Reset.class)
                .register(Teleport.class)
                .register(Top.class)
                .register(Unclaim.class)
                .register(Unlike.class)
                .register(Whitelist.class);

        commandBus.register(WorldCreate.class)
                .register(WorldSpawn.class)
                .register(WorldTP.class)
                .register(WorldWeather.class);

        commandBus.register(GenCreate.class)
                .register(GenEdit.class)
                .register(GenHelp.class)
                .register(GenReload.class)
                .register(GenSave.class);

        commandBus.submit(this);

        executor().async(() -> {
            try {
                IO.delete(configDir.resolve("exports"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        executor().sync(Support.of(
                "WorldEdit",
                "com.sk89q.worldedit.WorldEdit",
                "me.dags.plots.support.worldedit.WESessionListener")
        );

        executor().sync(Support.of(
                "VoxelSniper",
                "com.thevoxelbox.voxelsniper.VoxelSniper",
                "me.dags.plots.support.voxelsniper.SniperListener")
        );

        executor().sync(Support.of(
                "PlotsWeb",
                "me.dags.plotsweb.service.ExportService",
                "me.dags.plots.support.plotsweb.PlotsWeb")
        );
    }

    @Listener (order = Order.POST)
    public void onWorldLoad(LoadWorldEvent event) {
        World world = event.getTargetWorld();
        if (world.getWorldGenerator().getBaseGenerationPopulator() instanceof PlotGenerator) {
            if (safeMode()) {
                critical("PLOTS IS IN SAFE-MODE. UNLOADING PLOTWORLD: {}", world.getName());
                Sponge.getServer().unloadWorld(world);
                return;
            }
            world.setWeather(Weathers.CLEAR, Integer.MAX_VALUE);
            PlotGenerator plotGenerator = (PlotGenerator) world.getWorldGenerator().getBaseGenerationPopulator();
            WorldDatabase database = new WorldDatabase(client.getDatabase(world.getName().toLowerCase()));
            PlotWorld plotWorld = new PlotWorld(world, database, plotGenerator.plotSchema());
            Plots.core().registerPlotWorld(plotWorld);
        }
    }

    @Listener (order = Order.EARLY)
    public void onWorldUnload(UnloadWorldEvent event) {
        if (safeMode()) {
            return;
        }
        World world = event.getTargetWorld();
        if (world.getWorldGenerator().getBaseGenerationPopulator() instanceof PlotGenerator) {
            Plots.core().removePlotWorld(world.getName());
        }
    }

    @Listener (order = Order.EARLY)
    public void onShutDown(GameStoppingServerEvent event) {
        if (safeMode()) {
            return;
        }
        client.close();
        executor().close();
        core().dispatcher().finishAll();
    }

    public static Cause PLOTS_CAUSE() {
        return instance.plotsCause;
    }

    public static PlotsCore core() {
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

    private static void critical(String message, Object... args) {
        synchronized (System.out) {
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.warn("");
            logger.warn("");
            logger.warn("");
            logger.warn(message, args);
            logger.warn("");
            logger.warn("");
            logger.warn("");
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }
}
