package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.generator.GeneratorProperties;

/**
 * @author dags <dags@dags.me>
 */
public class PlotProvider {

    private final int gridXWidth;
    private final int gridZWidth;
    private final Vector2i minOffset;
    private final Vector2i maxOffset;

    public PlotProvider(GeneratorProperties properties) {
        this.gridXWidth = properties.getXWidth() + (2 * properties.getWallWidth()) + properties.getPathWidth();
        this.gridZWidth = properties.getZWidth() + (2 * properties.getWallWidth()) + properties.getPathWidth();
        this.minOffset = new Vector2i(properties.getWallWidth(), properties.getWallWidth());
        this.maxOffset = new Vector2i(properties.getWallWidth() + properties.getXWidth(), properties.getWallWidth() + properties.getZWidth());
    }

    public PlotId plotId(Vector3i position) {
        int x = position.getX(), z = position.getZ();
        x = (x < 0 ? x - gridXWidth : x) / gridXWidth;
        z = (z < 0 ? z - gridZWidth : z) / gridZWidth;
        return new PlotId(x, z);
    }

    public PlotBounds plotBounds(PlotId id) {
        int gridX = id.plotX() * gridXWidth;
        int gridZ = id.plotZ() * gridZWidth;
        Vector2i min = new Vector2i(gridX, gridZ).add(minOffset);
        Vector2i max = new Vector2i(gridX, gridZ).add(maxOffset);
        return new PlotBounds(min, max);
    }

    public Plot.Builder plotBuilder(Vector3i position) {
        PlotId id = plotId(position);
        PlotBounds bounds = plotBounds(id);
        return Plot.builder().id(id).bounds(bounds);
    }
}
