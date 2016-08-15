package me.dags.plots.generator;

import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.WorldGenerator;

/**
 * @author dags <dags@dags.me>
 */
public class PlotWorldGenerator implements PlotGenerator {

    private final int maxY;
    private final Layer[] layers;
    private final BiomeType biomeType;
    private final GeneratorProperties properties;

    PlotWorldGenerator(GeneratorProperties properties) {
        this.properties = properties;
        this.layers = properties.layers();
        this.biomeType = properties.biomeType();
        int max = 0;
        for (Layer layer : layers) {
            max += layer.thickness();
        }
        this.maxY = max;
    }

    @Override
    public PlotProvider plotProvider() {
        return new PlotProvider(properties);
    }

    @Override
    public void onLoadWorld(World world) {
        int pathMiddle = properties.getPathWidth() / 2;
        world.getProperties().setSpawnPosition(new Vector3i(0 - pathMiddle, this.maxY, 0 - pathMiddle));
        properties.gameRules().entrySet().forEach(e -> world.getProperties().setGameRule(e.getKey(), e.getValue()));
    }

    @Override
    public void modifyWorldGenerator(WorldCreationSettings world, DataContainer settings, WorldGenerator worldGenerator) {
        worldGenerator.getPopulators().clear();
        clearBiomePopulators(worldGenerator);
        worldGenerator.setBiomeGenerator(this);
        worldGenerator.setBaseGenerationPopulator(this);
    }

    @Override
    public String getId() {
        return Plots.toGeneratorId(getName());
    }

    @Override
    public String getName() {
        return properties.name();
    }

    @Override
    public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeArea biomes) {
        for (int y = buffer.getBlockMin().getY(); y < maxY; y++) {
            Layer layer = layerAtHeight(y);
            layer.populate(buffer, y);
        }
    }

    @Override
    public void generateBiomes(MutableBiomeArea buffer) {
        for (int x = buffer.getBiomeMin().getX(); x <= buffer.getBiomeMax().getX(); x++) {
            for (int z = buffer.getBiomeMin().getY(); z <= buffer.getBiomeMax().getY(); z++) {
                buffer.setBiome(x, z, biomeType);
            }
        }
    }

    private Layer layerAtHeight(int y) {
        Layer result = layers[layers.length - 1];
        int height = 0;
        for (Layer layer : layers) {
            result = layer;
            height += result.thickness();
            if (y < height) {
                break;
            }
        }
        return result;
    }

    private static void clearBiomePopulators(WorldGenerator generator) {
        Sponge.getRegistry().getAllOf(BiomeType.class).stream().map(generator::getBiomeSettings).forEach(settings -> settings.getPopulators().clear());
    }
}
