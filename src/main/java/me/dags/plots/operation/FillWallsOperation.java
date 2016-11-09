package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector2i;
import me.dags.plots.command.Cmd;
import me.dags.plots.plot.PlotBounds;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public class FillWallsOperation implements Operation {

    private final BlockState state;
    private final Viewer viewer;
    private final PlotId plotId;
    private final String world;
    private final Vector2i min;
    private final Vector2i max;
    private final int level;
    private boolean complete = false;

    public FillWallsOperation(Viewer viewer, PlotWorld plotWorld, PlotId plotId, BlockType blockType) {
        PlotBounds bounds = plotWorld.plotSchema().plotBounds(plotId);
        this.state = blockType.getDefaultState();
        this.plotId = plotId;
        this.viewer = viewer;
        this.world = plotWorld.world();
        this.level = plotWorld.plotSchema().surfaceHeight();
        this.min = bounds.getMin();
        this.max = bounds.getMax();
        this.complete = !(plotId.present() && bounds.present());
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public int process(int blocksToProcess) {
        if (viewer instanceof Player && !((Player) viewer).isOnline()) {
            complete = true;
            return blocksToProcess;
        }
        for (int x = min.getX(); x <= max.getX(); x++) {
            viewer.sendBlockChange(x, level, min.getY() - 1, state);
            viewer.sendBlockChange(x, level, max.getY() + 1, state);
            blocksToProcess -= 2;
        }
        for (int z = min.getY(); z <= max.getY(); z++) {
            viewer.sendBlockChange(min.getX() - 1, level, z, state);
            viewer.sendBlockChange(max.getX() + 1, level, z, state);
            blocksToProcess -= 2;
        }
        complete = true;
        if (viewer instanceof Player) {
            Cmd.FMT().info("Highlighted plot ").stress(plotId).tell((Player) viewer);
        }
        return Math.max(blocksToProcess, 0);
    }

    @Override
    public boolean complete() {
        return complete;
    }

    @Override
    public void onComplete(Runnable callback) {

    }
}
