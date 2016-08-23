package me.dags.plots.plot;

import me.dags.plots.Plots;

import java.util.Collection;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class PlotMask {

    static final PlotMask EMPTY = new PlotMask(PlotBounds.EMPTY);
    public static final PlotMask ANYWHERE = new PlotMask(PlotBounds.EMPTY) {
        @Override
        public boolean contains(int x, int z) {
            return true;
        }
    };

    private final PlotBounds bounds;
    private PlotMask and = PlotMask.EMPTY;

    private PlotMask(PlotBounds bounds) {
        this.bounds = bounds;
    }

    public boolean contains(int x, int z) {
        return bounds.contains(x, z) || (and.present() && and.contains(x, z));
    }

    private PlotMask and(PlotBounds other) {
        if (present()) {
            this.and = new PlotMask(other);
            return and;
        }
        return this;
    }

    private boolean present() {
        return this != PlotMask.EMPTY;
    }

    static PlotMask calculate(String worldName, Collection<PlotId> plotIds) {
        Optional<PlotWorld> world = Plots.getApi().getPlotWorldExact(worldName);
        if (world.isPresent()) {
            PlotMask root = PlotMask.EMPTY, mask = root;
            for (PlotId id : plotIds) {
                PlotBounds bounds = world.get().getPlotBounds(id);
                mask = root.present() ? mask.and(bounds) : (root = new PlotMask(bounds));
            }
            return root;
        }
        return PlotMask.EMPTY;
    }
}
