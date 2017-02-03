package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class PlotMask {

    static final PlotMask EMPTY = new PlotMask();

    private final int gridX, gridZ;
    private final Map<PlotId, PlotBounds> plots;

    private PlotMask() {
        gridX = 1;
        gridZ = 1;
        plots = Collections.emptyMap();
    }

    PlotMask(PlotSchema plotSchema, Map<PlotId, PlotBounds> plots) {
        this.gridX = plotSchema.gridXWidth();
        this.gridZ = plotSchema.gridZWidth();
        this.plots = ImmutableMap.copyOf(plots);
    }

    public boolean contains(PlotId plotId) {
        return plots.containsKey(plotId);
    }

    public boolean contains(Vector2i position) {
        return contains(position.getX(), 1, position.getY());
    }

    public boolean contains(Vector3i position) {
        return contains(position.getX(), position.getY(), position.getZ());
    }

    public boolean contains(int x, int y, int z) {
        if (this.present() && y > 0 && y < 256) {
            int xx = PlotId.transform(x, gridX);
            int zz = PlotId.transform(z, gridZ);
            PlotBounds bounds = plots.get(PlotId.of(xx, zz));
            return bounds != null && bounds.contains(x, z);
        }
        return false;
    }

    public Map<PlotId, PlotBounds> plots() {
        return plots;
    }

    private boolean present() {
        return this != EMPTY;
    }

    @Override
    public String toString() {
        return present() ? plots.toString() : "EMPTY";
    }

    public static final PlotMask ANYWHERE = new PlotMask() {
        @Override
        public boolean contains(int x, int y, int z) {
            return y > 0 && y < 256;
        }
    };

    public static final PlotMask NOWHERE = new PlotMask() {
        @Override
        public boolean contains(int x, int y, int z) {
            return false;
        }
    };

    public static PlotMask of(PlotSchema schema, Map<PlotId, PlotBounds> plots) {
        return new PlotMask(schema, plots);
    }
}
