package me.dags.plots.voxelsniper;

import com.thevoxelbox.voxelsniper.brush.mask.Mask;
import com.thevoxelbox.voxelsniper.brush.mask.Mask2D;
import me.dags.plots.plot.PlotMask;

/**
 * @author dags <dags@dags.me>
 */
public class SniperMask implements Mask, Mask2D {

    private final PlotMask plotMask;

    public SniperMask(PlotMask mask) {
        this.plotMask = mask;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return this.plotMask.contains(x, y, z);
    }

    @Override
    public boolean contains(int x, int z) {
        return this.plotMask.contains(x, 128, z);
    }

    @Override
    public Mask2D toMask2D() {
        return this;
    }
}
