package me.dags.plots.operation;

import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeVolume;

/**
 * @author dags <dags@dags.me>
 */
public class FillBiomeOperation implements Operation {

    private final String world;
    private final MutableBiomeVolume volume;
    private final BiomeType biome;

    private boolean complete = false;

    public FillBiomeOperation(String world, MutableBiomeVolume volume, BiomeType biome) {
        this.world = world;
        this.volume = volume;
        this.biome = biome;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public int process(int blocksToProcess) {
        volume.getBiomeWorker().fill((x, y, z) -> biome);
        complete = true;
        return 0;
    }

    @Override
    public boolean complete() {
        return complete;
    }

    @Override
    public void onComplete(Runnable callback) {

    }
}
