package me.dags.plots;

import me.dags.plots.util.Executor;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.operation.OperationDispatcher;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.IO;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class PlotsApi {

    private final Map<String, PlotWorld> worlds = new HashMap<>();
    private final Map<String, GeneratorProperties> generators = new HashMap<>();
    private final Plots plugin;

    private OperationDispatcher dispatcher;

    PlotsApi(Plots plots) {
        this.plugin = plots;
    }

    public Path configDir() {
        return plugin.configDir;
    }

    public Path generatorsDir() {
        return plugin.configDir.resolve("generators");
    }

    public void loadWorldGenerators() {
        IO.loadGeneratorProperties(configDir().resolve("worlds"))
                .map(GeneratorProperties::toGenerator)
                .forEach(this::registerWorldGenerator);
    }

    public void reloadGenerators() {
        if (!Files.exists(generatorsDir().resolve("default.conf"))) {
            IO.saveProperties(GeneratorProperties.DEFAULT, generatorsDir());
        }
        generators.clear();
        IO.loadGeneratorProperties(generatorsDir()).forEach(this::registerBaseGenerator);
    }

    public OperationDispatcher dispatcher() {
        if (dispatcher == null) {
            int bpt = Plots.getConfig().blocksPerTick();

            Plots.log("Initializing OperationDispatcher. BPT={}", bpt);
            dispatcher = new OperationDispatcher(Plots.ID, bpt);
            Sponge.getScheduler().createTaskBuilder().intervalTicks(1).delayTicks(1).execute(dispatcher).submit(plugin);
        }
        return dispatcher;
    }

    public Optional<GeneratorProperties> baseGenerator(String name) {
        return Optional.ofNullable(generators.get(name));
    }

    public Optional<PlotWorld> plotWorld(String name) {
        PlotWorld world = worlds.get(name);
        return world != null ? Optional.of(world) : matchPlotWorld(name);
    }

    public Optional<PlotWorld> plotWorldExact(String name) {
        return Optional.ofNullable(worlds.get(name));
    }

    public Optional<PlotWorld> matchPlotWorld(String name) {
        String lowercaseName = name.toLowerCase();
        PlotWorld bestMatch = null;
        for (Map.Entry<String, PlotWorld> entry : worlds.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(lowercaseName)) {
                return Optional.of(entry.getValue());
            }
            if (entry.getKey().toLowerCase().startsWith(lowercaseName)) {
                if (bestMatch == null || entry.getValue().world().length() < bestMatch.world().length()) {
                    bestMatch = entry.getValue();
                }
            }
        }
        return Optional.ofNullable(bestMatch);
    }

    public void removePlotWorld(String world) {
        plotWorldExact(world).ifPresent(plotWorld -> {
            plotWorld.unregister();
            dispatcher().finishAll(plotWorld.world());
            worlds.remove(world);
        });
    }

    public void registerPlotWorld(PlotWorld plotWorld) {
        worlds.put(plotWorld.world(), plotWorld);
        plotWorld.register(plugin);
    }

    public void registerBaseGenerator(GeneratorProperties generatorProperties) {
        Plots.log("Registering base generator {}", generatorProperties);
        generators.put(generatorProperties.name(), generatorProperties);
    }

    public void registerWorldGenerator(PlotGenerator plotGenerator) {
        Plots.log("Registering world generator for {}", plotGenerator.getName());
        Sponge.getRegistry().register(WorldGeneratorModifier.class, plotGenerator);
    }
}
