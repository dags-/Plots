package me.dags.plots.util;

import me.dags.data.NodeAdapter;
import me.dags.data.node.Node;
import me.dags.data.node.NodeTypeAdapter;
import me.dags.plots.Config;
import me.dags.plots.Plots;
import me.dags.plots.generator.GeneratorProperties;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class IO {

    private static final NodeAdapter HOCON = NodeAdapter.hocon();
    private static final NodeTypeAdapter<GeneratorProperties> GENERATOR_PROPERTIES = new GeneratorPropertiesAdapter();
    private static final NodeTypeAdapter<Config> CONFIG_ADAPTER = new ConfigAdapter();

    public static void saveProperties(GeneratorProperties properties, Path dir) {
        Node node = GENERATOR_PROPERTIES.toNode(properties);
        Path path = dir.resolve(properties.name() + ".conf");
        HOCON.to(node, path);
        Plots.log("Saved: {} to: {}", properties.name(), path);
    }

    public static Stream<GeneratorProperties> loadGeneratorProperties(Path dir) {
        Plots.log("Loading Generators from: {}", dir);
        return HOCON.fromDir(dir, ".conf").map(GENERATOR_PROPERTIES::fromNode);
    }

    public static Config getConfig(Path path) {
        Node node = HOCON.from(path);

        Config config;

        if (!node.isPresent()) {
            Plots.log("Creating default config");
            config = new Config(true);
        } else {
            Plots.log("Loading config from");
            config = CONFIG_ADAPTER.fromNode(node);
        }

        Plots.log("Saving config to: {}", path);
        Node updated = CONFIG_ADAPTER.toNode(config);
        HOCON.to(updated, path);

        return config;
    }
}
