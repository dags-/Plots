package me.dags.plots;

import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.generator.PlotGenerator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class PlotsAPI {

    private final Map<String, PlotWorld> worlds = new HashMap<>();
    private final Plots plugin;

    PlotsAPI(Plots plots) {
        this.plugin = plots;
    }

    public Path configDir() {
        return plugin.configDir;
    }

    public PlotWorld getPlotWorld(String name) {
        return worlds.get(name);
    }

    public void registerPlotWorld(PlotWorld plotWorld) {
        worlds.put(plotWorld.getWorld(), plotWorld);
        Sponge.getEventManager().registerListeners(plugin, plotWorld);
    }

    public Optional<PlotGenerator> getGenerator(String name) {
        return Sponge.getRegistry().getType(WorldGeneratorModifier.class, Plots.toGeneratorId(name))
                .filter(PlotGenerator.class::isInstance)
                .map(PlotGenerator.class::cast);
    }

    public void register(GeneratorProperties generatorProperties) {
        Plots.log("Registering Generator: {}", generatorProperties);
        Sponge.getRegistry().register(WorldGeneratorModifier.class, generatorProperties.toGenerator());
    }
}
