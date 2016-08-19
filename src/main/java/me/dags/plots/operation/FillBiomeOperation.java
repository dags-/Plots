package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeArea;

/**
 * @author dags <dags@dags.me>
 */
public class FillBiomeOperation extends AbstractBiomeOperation {

    private final MutableBiomeArea biomeArea;
    private final Vector2i min;
    private final BiomeType biomeType;

    public FillBiomeOperation(MutableBiomeArea biomeArea, BiomeType biomeType) {
        super(biomeArea.getBiomeMin(), biomeArea.getBiomeMax());
        this.biomeArea = biomeArea;
        this.min = biomeArea.getBiomeMin();
        this.biomeType = biomeType;
    }

    @Override
    void processAt(int x, int z) {
        biomeArea.setBiome(min.getX() + x, min.getY() + z, biomeType);
    }
}
