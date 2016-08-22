package me.dags.plots;

import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.operation.OperationDispatcher;
import me.dags.plots.plot.PlotWorld;
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

    private OperationDispatcher dispatcher;

    PlotsAPI(Plots plots) {
        this.plugin = plots;
    }

    public Path configDir() {
        return plugin.configDir;
    }

    public Path generatorsDir() {
        return plugin.configDir.resolve("generators");
    }

    public OperationDispatcher getDispatcher() {
        if (dispatcher == null) {
            int bpt = Plots.getConfig().blocksPerTick();

            Plots.log("Initializing OperationDispatcher. BPT={}", bpt);
            dispatcher = new OperationDispatcher(Plots.ID, bpt);
            Sponge.getScheduler().createTaskBuilder().intervalTicks(1).delayTicks(1).execute(dispatcher).submit(plugin);
        }
        return dispatcher;
    }

    public Optional<PlotWorld> getPlotWorld(String name) {
        PlotWorld world = worlds.get(name);
        return world != null ? Optional.of(world) : matchPlotWorld(name);
    }

    public Optional<PlotWorld> matchPlotWorld(String name) {
        String lowercaseName = name.toLowerCase();
        PlotWorld bestMatch = null;
        for (Map.Entry<String, PlotWorld> entry : worlds.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(lowercaseName)) {
                return Optional.of(entry.getValue());
            }
            if (entry.getKey().toLowerCase().startsWith(lowercaseName)) {
                if (bestMatch == null || entry.getValue().getWorld().length() < bestMatch.getWorld().length()) {
                    bestMatch = entry.getValue();
                }
            }
        }
        return Optional.ofNullable(bestMatch);
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
