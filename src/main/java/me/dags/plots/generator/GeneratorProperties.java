package me.dags.plots.generator;

import me.dags.plots.util.Defaults;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class GeneratorProperties {

    public static final GeneratorProperties DEFAULT = builder()
            .gameRule(Defaults.defaultGameRules())
            .layer(Defaults.defaultLayers())
            .name("default")
            .wallWidth(1)
            .pathWidth(6)
            .xWidth(42)
            .zWidth(42)
            .build();

    private final String name;
    private final int xWidth;
    private final int zWidth;
    private final int wallWidth;
    private final int pathWidth;
    private final BiomeType biomeType;
    private final List<LayerProperties> layers;
    private final Map<String, String> gameRules;

    private GeneratorProperties(Builder builder) {
        this.name = builder.name;
        this.xWidth = builder.xWidth;
        this.zWidth = builder.zWidth;
        this.wallWidth = builder.wallWidth;
        this.pathWidth = builder.pathWidth;
        this.biomeType = builder.biomeType;
        this.layers = new ArrayList<>(builder.layers.size());
        this.layers.addAll(builder.layers);
        this.gameRules = Collections.unmodifiableMap(new HashMap<>(builder.gameRules));
    }

    public PlotWorldGenerator toGenerator() {
        return new PlotWorldGenerator(this);
    }

    public String name() {
        return name;
    }

    public BiomeType biomeType() {
        return biomeType;
    }

    public List<LayerProperties> layerProperties() {
        return layers;
    }

    public Layer[] layers() {
        Layer[] layers = new Layer[this.layers.size()];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = toLayer(this.layers.get(i));
        }
        return layers;
    }

    public int getMaxY() {
        int y = 0;
        for (LayerProperties properties : layers) {
            y += properties.thickness();
        }
        return y;
    }

    public int getPathWidth() {
        return pathWidth;
    }

    public int getWallWidth() {
        return wallWidth;
    }

    public int getXWidth() {
        return xWidth;
    }

    public int getZWidth() {
        return zWidth;
    }

    public Map<String, String> gameRules() {
        return gameRules;
    }

    private Layer toLayer(LayerProperties properties) {
        return Layer.builder()
                .thickness(properties.thickness())
                .body(properties.body())
                .path(properties.path())
                .wall(properties.wall())
                .pathWidth(pathWidth)
                .wallWidth(wallWidth)
                .plotXWidth(xWidth)
                .plotZWidth(zWidth)
                .build();
    }

    @Override
    public String toString() {
        return String.format("name=%s, dims=[%s,%s,%s,%s], biome=%s", name, xWidth, zWidth, wallWidth, pathWidth, biomeType.getName());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name = "DEFAULT";
        private int xWidth = 42;
        private int zWidth = 42;
        private int wallWidth = 1;
        private int pathWidth = 7;
        private BiomeType biomeType = BiomeTypes.PLAINS;
        private List<LayerProperties> layers = new ArrayList<>();
        private Map<String, String> gameRules = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder biome(BiomeType biomeType) {
            this.biomeType = biomeType;
            return this;
        }

        public Builder xWidth(int width) {
            xWidth = width;
            return this;
        }

        public Builder zWidth(int width) {
            zWidth = width;
            return this;
        }

        public Builder wallWidth(int width) {
            wallWidth = width;
            return this;
        }

        public Builder pathWidth(int width) {
            pathWidth = width;
            return this;
        }

        public Builder layer(BlockType body, BlockType wall, BlockType path, int thickness) {
            layers.add(new LayerProperties(body, wall, path, thickness));
            return this;
        }

        public Builder layer(List<LayerProperties> list) {
            layers.addAll(list);
            return this;
        }

        public Builder gameRule(Map<String, String> map) {
            this.gameRules.putAll(map);
            return this;
        }

        public Builder gameRule(String rule, String value) {
            this.gameRules.put(rule, value);
            return this;
        }

        public Builder clearLayers() {
            layers.clear();
            return this;
        }

        public GeneratorProperties build() {
            return new GeneratorProperties(this);
        }
    }
}