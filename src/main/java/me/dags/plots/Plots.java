package me.dags.plots;

import com.google.inject.Inject;
import me.dags.commandbus.CommandBus;
import me.dags.plots.commands.PlotCommands;
import me.dags.plots.commands.PlotworldCommands;
import me.dags.plots.database.Database;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.World;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = Plots.ID, version = "0.1")
public class Plots {

    static final String ID = "plots";

    private static final Logger logger = LoggerFactory.getLogger(ID);
    private static Plots instance;

    private final PlotsAPI plotsAPI = new PlotsAPI(this);
    private final Database database;
    final Path configDir;

    @Inject
    public Plots(@ConfigDir(sharedRoot = false) Path configDir) {
        instance = this;
        this.configDir = configDir;
        this.database = new Database(this, "jdbc:h2:" + configDir.resolve("plots_data").toAbsolutePath());
    }

    @Listener
    public void init(GameInitializationEvent event) {
        if (!Files.exists(instance.configDir.resolve("generators").resolve("default.conf"))) {
            IO.saveProperties(GeneratorProperties.DEFAULT, instance.configDir.resolve("generators"));
        }
        IO.loadGeneratorProperties(instance.configDir.resolve("generators")).forEach(Plots.getApi()::register);
        CommandBus.newInstance(logger).register(PlotCommands.class).submit(this);
        CommandBus.newInstance(logger).register(PlotworldCommands.class).submit(this);
    }

    @Listener (order = Order.PRE)
    public void onWorldLoad(LoadWorldEvent event) {
        World world = event.getTargetWorld();
        if (world.getWorldGenerator().getBaseGenerationPopulator() instanceof PlotGenerator) {
            PlotGenerator plotGenerator = (PlotGenerator) world.getWorldGenerator().getBaseGenerationPopulator();
            Plots.getApi().registerPlotWorld(new PlotWorld(world, plotGenerator.plotProvider()));
            Plots.getDatabase().loadWorld(world.getName());
        }
    }

    public static Database getDatabase() {
        return instance.database;
    }

    public static PlotsAPI getApi() {
        return instance.plotsAPI;
    }

    public static String toGeneratorId(String name) {
        return String.format("%s:%s", ID, name.toLowerCase());
    }

    public static void log(String message, Object... args) {
        logger.info(message, args);
    }
}
