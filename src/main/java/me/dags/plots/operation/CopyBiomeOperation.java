package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.BiomeArea;
import org.spongepowered.api.world.extent.MutableBiomeArea;

/**
 * @author dags <dags@dags.me>
 */
public class CopyBiomeOperation extends AbstractBiomeOperation {

    private final BiomeArea from;
    private final MutableBiomeArea to;
    private final Vector2i fromMin;
    private final Vector2i toMin;

    public CopyBiomeOperation(String world, BiomeArea from, MutableBiomeArea to) {
        super(world, from.getBiomeMin(), from.getBiomeMax());
        if (!from.getBiomeSize().equals(to.getBiomeSize())) {
            throw new UnsupportedOperationException("Volumes must be equal in size!");
        }
        this.from = from;
        this.to = to;
        this.fromMin = from.getBiomeMin();
        this.toMin = to.getBiomeMin();
    }

    @Override
    void processAt(int x, int z) {
        BiomeType biome = from.getBiome(fromMin.getX() + x, fromMin.getY() + z);
        to.setBiome(toMin.getX() + x, toMin.getY() + z, biome);
    }
}
