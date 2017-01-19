package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeVolume;

/**
 * @author dags <dags@dags.me>
 */
public class FillBiomeOperation extends AbstractBiomeOperation {

    private final MutableBiomeVolume biomeArea;
    private final Vector3i min;
    private final BiomeType biomeType;

    public FillBiomeOperation(String world, MutableBiomeVolume biomeArea, BiomeType biomeType) {
        super(world, biomeArea.getBiomeMin(), biomeArea.getBiomeMax());
        this.biomeArea = biomeArea;
        this.min = biomeArea.getBiomeMin();
        this.biomeType = biomeType;
    }

    @Override
    void processAt(int x, int y, int z) {
        biomeArea.setBiome(min.getX() + x, min.getY() + y, min.getZ() + z, biomeType);
    }
}
