package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.generator.GeneratorProperties;

/**
 * @author dags <dags@dags.me>
 */
public class PlotSchema {

    private final int top;
    private final int wallWidth;
    private final int gridXWidth;
    private final int gridZWidth;
    private final Vector2i minOffset;
    private final Vector2i maxOffset;

    public PlotSchema(GeneratorProperties properties) {
        this.top = properties.getMaxY();
        this.wallWidth = properties.getWallWidth();
        this.gridXWidth = properties.getXWidth() + (2 * properties.getWallWidth()) + properties.getPathWidth();
        this.gridZWidth = properties.getZWidth() + (2 * properties.getWallWidth()) + properties.getPathWidth();
        this.minOffset = new Vector2i(properties.getWallWidth(), properties.getWallWidth()).sub(1, 1);
        this.maxOffset = new Vector2i(properties.getWallWidth() + properties.getXWidth(), properties.getWallWidth() + properties.getZWidth());
    }

    public int surfaceHeight() {
        return top;
    }

    public int gridXWidth() {
        return gridXWidth;
    }

    public int gridZWidth() {
        return gridZWidth;
    }

    public int wallWidth() {
        return wallWidth;
    }

    public Vector2i minOffset() {
        return minOffset;
    }

    public Vector2i maxOffset() {
        return maxOffset;
    }

    public PlotId containingPlot(Vector3i position) {
        PlotId id = plotId(position);
        if (id.present() && plotBounds(id).contains(position)) {
            return id;
        }
        return PlotId.EMPTY;
    }

    public PlotId plotId(Vector3d position) {
        return plotId((int) position.getX(), (int) position.getZ());
    }

    public PlotId plotId(Vector3i position) {
        return plotId(position.getX(), position.getZ());
    }

    public PlotId plotId(int x, int z) {
        x = PlotId.transform(x, gridXWidth());
        z = PlotId.transform(z, gridZWidth());
        return new PlotId(x, z);
    }

    public PlotBounds plotBounds(PlotId id) {
        int gridX = id.plotX() * gridXWidth();
        int gridZ = id.plotZ() * gridZWidth();
        Vector2i min = new Vector2i(gridX, gridZ).add(minOffset());
        Vector2i max = new Vector2i(gridX, gridZ).add(maxOffset());
        return new PlotBounds(min, max);
    }
}
