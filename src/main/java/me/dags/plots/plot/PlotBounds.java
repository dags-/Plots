package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;

/**
 * @author dags <dags@dags.me>
 */
public class PlotBounds {

    private final Vector2i min;
    private final Vector2i max;

    public PlotBounds(Vector2i min, Vector2i max) {
        this.min = min;
        this.max = max;
    }

    public boolean contains(Vector3i pos) {
        return contains(pos.getX(), pos.getZ());
    }

    public boolean contains(Vector2i pos) {
        return contains(pos.getX(), pos.getY());
    }

    public boolean contains(int x, int z) {
        return x >= min.getX() && x <= max.getX() && z >= min.getY() && z <= max.getY();
    }
}
