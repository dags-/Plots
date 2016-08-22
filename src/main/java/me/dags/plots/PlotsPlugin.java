package me.dags.plots;

import com.google.inject.Inject;
import me.dags.commandbus.CommandBus;
import me.dags.plots.commands.GenCommands;
import me.dags.plots.commands.PlotCommands;
import me.dags.plots.commands.WorldCommands;
import me.dags.plots.database.Database;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.plot.PlotWorld;
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
import org.spongepowered.api.world.World;

import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = PlotsPlugin.ID, name = PlotsPlugin.ID, version = "0.1")
public class PlotsPlugin {

    public static final String ID = "plots";

    private static final Logger logger = LoggerFactory.getLogger(ID);
    private static PlotsPlugin instance;

    private final Plots plots = new Plots(this);
    private final Database database;
    final Path configDir;

    private Config config;

    @Inject
    public PlotsPlugin(@ConfigDir(sharedRoot = false) Path configDir) {
        PlotsPlugin.instance = this;
        this.configDir = configDir;
        this.database = new Database(this, "jdbc:h2:" + configDir.resolve("plots_data").toAbsolutePath());
    }

    @Listener
    public void init(GameInitializationEvent event) {
        config = IO.getConfig(configDir.resolve("config.conf"));
        database.init();

        getPlots().reloadGenerators();
        getPlots().loadWorldGenerators();

        CommandBus.newInstance(logger)
                .register(GenCommands.class)
                .register(PlotCommands.class)
                .register(WorldCommands.class)
                .submit(this);

        Sponge.getScheduler().createTaskBuilder()
                .execute(Support.of("WorldEdit", "com.sk89q.worldedit.WorldEdit", "me.dags.plots.worldedit.WESessionListener"))
                .submit(this);
    }

    @Listener (order = Order.POST)
    public void onWorldLoad(LoadWorldEvent event) {
        World world = event.getTargetWorld();
        if (world.getWorldGenerator().getBaseGenerationPopulator() instanceof PlotGenerator) {
            PlotGenerator plotGenerator = (PlotGenerator) world.getWorldGenerator().getBaseGenerationPopulator();
            PlotsPlugin.getPlots().registerPlotWorld(new PlotWorld(world, plotGenerator.plotProvider()));
            PlotsPlugin.getDatabase().loadWorld(world.getName());
        }
    }

    @Listener (order = Order.EARLY)
    public void onWorldUnload(UnloadWorldEvent event) {
        World world = event.getTargetWorld();
        if (world.getWorldGenerator().getBaseGenerationPopulator() instanceof PlotGenerator) {
            PlotsPlugin.getPlots().removePlotWorld(world.getName());
        }
    }

    @Listener (order = Order.EARLY)
    public void onShutDown(GameStoppingServerEvent event) {
        database.close();
        getPlots().getDispatcher().finishAll();
    }

    public static Database getDatabase() {
        return instance.database;
    }

    public static Plots getPlots() {
        return instance.plots;
    }

    public static Config getConfig() {
        return instance.config;
    }

    public static String toGeneratorId(String name) {
        return String.format("%s:%s", ID, name.toLowerCase());
    }

    public static void log(String message, Object... args) {
        logger.info(message, args);
    }
}