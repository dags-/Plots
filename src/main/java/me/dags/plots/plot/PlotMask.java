package me.dags.plots.plot;

import me.dags.plots.PlotsPlugin;

import java.util.Collection;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class PlotMask {

    static PlotMask EMPTY = new PlotMask(PlotBounds.EMPTY);

    private final PlotBounds bounds;
    private PlotMask and = PlotMask.EMPTY;

    private PlotMask(PlotBounds bounds) {
        this.bounds = bounds;
    }

    public boolean contains(int x, int z) {
        return bounds.contains(x, z) || (and.present() && and.contains(x, z));
    }

    private PlotMask and(PlotBounds other) {
        this.and = new PlotMask(other);
        return and;
    }

    private boolean present() {
        return this != PlotMask.EMPTY;
    }

    static PlotMask calculate(String worldName, Collection<PlotId> plotIds) {
        Optional<PlotWorld> world = PlotsPlugin.getPlots().getPlotWorldExact(worldName);
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
