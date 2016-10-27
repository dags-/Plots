package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class PlotMask {

    private static final PlotMask EMPTY = new PlotMask(null, null);

    private final PlotBounds bounds;
    private final PlotMask and;

    private PlotMask(PlotBounds bounds, PlotMask and) {
        this.bounds = bounds;
        this.and = and;
    }

    public boolean contains(Vector2i position) {
        return contains(position.getX(), 1, position.getY());
    }

    public boolean contains(Vector3i position) {
        return contains(position.getX(), position.getY(), position.getZ());
    }

    public boolean contains(int x, int y, int z) {
        return this.present() && (y > 0 && y < 256) && bounds.contains(x, z) || (and.contains(x, y, z));
    }

    @Override
    public String toString() {
        return present() ? "[" + bounds + "] and " + and : "EMPTY";
    }

    private boolean present() {
        return this != EMPTY;
    }

    public static final PlotMask ANYWHERE = new PlotMask(PlotBounds.EMPTY, null) {
        @Override
        public boolean contains(int x, int y, int z) {
            return y > 0 && y < 256;
        }
    };

    public static final PlotMask NOWHERE = new PlotMask(PlotBounds.EMPTY, null) {
        @Override
        public boolean contains(int x, int y, int z) {
            return false;
        }
    };

    public static PlotMask of(List<PlotBounds> plots) {
        Mutable root = Mutable.EMPTY, mutable = root;
        for (PlotBounds bounds : plots) {
            mutable = root.present() ? mutable.and(bounds) : (root = new Mutable(bounds));
        }
        return root.build();
    }

    private static class Mutable {

        private static Mutable EMPTY = new Mutable(null);

        private final PlotBounds bounds;
        private Mutable and;

        private Mutable(PlotBounds bounds) {
            this.bounds = bounds;
        }

        private Mutable and(PlotBounds other) {
            if (present()) {
                return this.and = new Mutable(other);
            }
            return this;
        }

        private boolean present() {
            return this != EMPTY;
        }

        private PlotMask build() {
            if (and == null) {
                return new PlotMask(bounds, PlotMask.EMPTY);
            }
            return new PlotMask(bounds, and.build());
        }
    }
}
