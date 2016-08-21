package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;

/**
 * @author dags <dags@dags.me>
 */
public class PlotBounds {

    static final PlotBounds EMPTY = new PlotBounds();

    private final Vector2i min;
    private final Vector2i max;

    private PlotBounds() {
        this.min = new Vector2i(1, 1);
        this.max = new Vector2i(-1, -1);
    }

    public PlotBounds(Vector2i min, Vector2i max) {
        this.min = min;
        this.max = max;
    }

    public boolean present() {
        return this != EMPTY;
    }

    public Vector2i getMin() {
        return min;
    }

    public Vector2i getMax() {
        return max;
    }

    public Vector3i getBlockMin() {
        return new Vector3i(min.getX(), 0, min.getY());
    }

    public Vector3i getBlockMax() {
        return new Vector3i(max.getX(), 255, max.getY());
    }

    public boolean contains(Vector3i pos) {
        return contains(pos.getX(), pos.getZ());
    }

    public boolean contains(Vector2i pos) {
        return contains(pos.getX(), pos.getY());
    }

    public boolean contains(int x, int z) {
        return x > min.getX() && x < max.getX() && z > min.getY() && z < max.getY();
    }
}
