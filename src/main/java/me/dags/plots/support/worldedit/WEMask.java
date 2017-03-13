package me.dags.plots.support.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Mask2D;
import me.dags.plots.plot.PlotMask;

import javax.annotation.Nullable;

/**
 * @author dags <dags@dags.me>
 */
public class WEMask implements Mask, Mask2D {

    private final PlotMask mask;

    WEMask(PlotMask mask) {
        this.mask = mask;
    }

    @Override
    public boolean test(Vector vector) {
        return mask.contains(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    @Override
    public boolean test(Vector2D vector) {
        return mask.contains(vector.getBlockX(), 1, vector.getBlockZ());
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        return this;
    }
}
