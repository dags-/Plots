package me.dags.plots.plot;

import me.dags.plots.Plots;

import java.util.Collection;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class PlotMask {

    private final PlotBounds bounds;
    private PlotMask and = null;

    private PlotMask(PlotBounds bounds) {
        this.bounds = bounds;
    }

    public boolean contains(int x, int y, int z) {
        return (y > 0 && y < 256) && bounds.contains(x, z) || (and != null && and.contains(x, y, z));
    }

    private PlotMask and(PlotBounds other) {
        if (present()) {
            return this.and = new PlotMask(other);
        }
        return this;
    }

    private boolean present() {
        return this != PlotMask.NOWHERE;
    }

    public static final PlotMask ANYWHERE = new PlotMask(PlotBounds.EMPTY) {
        @Override
        public boolean contains(int x, int y, int z) {
            return y > 0 && y < 256;
        }
    };

    static final PlotMask NOWHERE = new PlotMask(PlotBounds.EMPTY) {
        @Override
        public boolean contains(int x, int y, int z) {
            return false;
        }
    };

    static PlotMask calculate(String worldName, Collection<PlotId> plotIds) {
        Optional<PlotWorld> world = Plots.getApi().getPlotWorldExact(worldName);
        if (world.isPresent()) {
            PlotMask root = PlotMask.NOWHERE, mask = root;
            for (PlotId id : plotIds) {
                PlotBounds bounds = world.get().getPlotBounds(id);
                mask = root.present() ? mask.and(bounds) : (root = new PlotMask(bounds));
            }
            return root;
        }
        return PlotMask.NOWHERE;
    }
}
