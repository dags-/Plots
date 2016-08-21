package me.dags.plots.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Mask2D;
import me.dags.plots.plot.PlotMask;

import javax.annotation.Nullable;

/**
 * @author dags <dags@dags.me>
 */
public class WEPlotMask implements Mask, Mask2D {

    private final PlotMask mask;

    WEPlotMask(PlotMask mask) {
        this.mask = mask;
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        return this;
    }

    @Override
    public boolean test(Vector vector) {
        return mask.contains(vector.getBlockX(), vector.getBlockZ());
    }

    @Override
    public boolean test(Vector2D vector) {
        return mask.contains(vector.getBlockX(), vector.getBlockZ());
    }
}
