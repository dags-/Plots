package me.dags.plots.operation;

import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeArea;

/**
 * @author dags <dags@dags.me>
 */
public class FillBiomeOperation extends AbstractBiomeOperation {

    private final MutableBiomeArea biomeArea;
    private final BiomeType biomeType;

    public FillBiomeOperation(MutableBiomeArea biomeArea, BiomeType biomeType) {
        super(biomeArea.getBiomeMin(), biomeArea.getBiomeMax());
        this.biomeArea = biomeArea;
        this.biomeType = biomeType;
    }

    @Override
    void processAt(int x, int z) {
        biomeArea.setBiome(x, z, biomeType);
    }
}
