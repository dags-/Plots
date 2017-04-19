package me.dags.plots.generator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class GeneratorProperties implements CatalogType {

    public static final GeneratorProperties DEFAULT = builder()
            .gameRules(Defaults.defaultGameRules())
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
        this.layers = ImmutableList.copyOf(builder.layers);
        this.gameRules = ImmutableMap.copyOf(builder.gameRules);
    }

    @Override
    public String getId() {
        return name();
    }

    @Override
    public String getName() {
        return name();
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

    public GeneratorProperties copyTo(String name) {
        return builder()
                .name(name)
                .xWidth(xWidth)
                .zWidth(zWidth)
                .wallWidth(wallWidth)
                .pathWidth(pathWidth)
                .gameRules(gameRules)
                .biome(biomeType)
                .layer(layers)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name = "default";
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
            layers.add(new LayerProperties(body.getDefaultState(), wall.getDefaultState(), path.getDefaultState(), thickness));
            return this;
        }

        public Builder layer(BlockState body, BlockState wall, BlockState path, int thickness) {
            layers.add(new LayerProperties(body, wall, path, thickness));
            return this;
        }

        public Builder layer(List<LayerProperties> list) {
            layers.addAll(list);
            return this;
        }

        public Builder gameRules(Map<String, String> map) {
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

        public Builder defaultGameRules() {
            return gameRules(Defaults.defaultGameRules());
        }

        public GeneratorProperties build() {
            return new GeneratorProperties(this);
        }

        public String getName() {
            return name;
        }

        public int getxWidth() {
            return xWidth;
        }

        public int getzWidth() {
            return zWidth;
        }

        public int getPathWidth() {
            return pathWidth;
        }

        public int getWallWidth() {
            return wallWidth;
        }

        public BiomeType getBiomeType() {
            return biomeType;
        }
    }
}
