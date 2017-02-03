package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotSchema;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.extent.MutableBlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public class WallsOperation implements Operation {

    private final MutableBlockVolume volume;
    private final BlockState state;
    private final String world;
    private final Vector3i min;
    private final Vector3i max;
    private final int depth;
    private final int level;
    private final int width;
    private boolean complete = false;

    private Runnable callback = () -> {};
    private int phase = 0;
    private int xPos = 0;
    private int zPos = 0;

    public WallsOperation(String world, MutableBlockVolume volume, int depth, PlotSchema schema, BlockState state) {
        this.state = state;
        this.world = world;
        this.depth = depth;
        this.width = schema.wallWidth();
        this.level = schema.surfaceHeight() - 1;
        this.volume = volume;
        this.min = volume.getBlockMin().add(schema.wallWidth(), 0, schema.wallWidth());
        this.max = volume.getBlockMax().sub(schema.wallWidth(), 0, schema.wallWidth());
        this.xPos = min.getX();
        this.zPos = min.getZ();
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public int process(int blocksToProcess) {
        while (blocksToProcess > 0) {
            if (phase == 0) {
                blocksToProcess = phase0(blocksToProcess);
            } else if (phase == 1) {
                blocksToProcess = phase1(blocksToProcess);
            } else if (phase == 2) {
                blocksToProcess = phase2(blocksToProcess);
            } else if (phase == 3){
                blocksToProcess = phase3(blocksToProcess);
            } else {
                complete = true;
                break;
            }
        }

        if (complete) {
            callback.run();
        }

        return Math.max(0, blocksToProcess);
    }

    @Override
    public boolean complete() {
        return complete;
    }

    @Override
    public void onComplete(Runnable callback) {
        this.callback = callback;
    }

    private int phase0(int blocksToProcess) {
        for (int y = level; y > level - depth && y >= 0; y--) {
            for (int z = zPos; z > volume.getBlockMin().getZ(); z--) {
                volume.setBlock(xPos, y, z, state, Plots.PLOTS_CAUSE());
                blocksToProcess--;
            }
        }

        if (++xPos > max.getX()) {
            xPos = max.getX();
            phase++;
        }

        return blocksToProcess;
    }

    private int phase1(int blocksToProcess) {
        for (int y = level; y > level - depth && y >= 0; y--) {
            for (int x = xPos; x < volume.getBlockMax().getX(); x++) {
                volume.setBlock(x, y, zPos, state, Plots.PLOTS_CAUSE());
                blocksToProcess--;
            }
        }

        if (++zPos > max.getZ()) {
            zPos = max.getZ();
            phase++;
        }

        return blocksToProcess;
    }

    private int phase2(int blocksToProcess) {
        for (int y = level; y > level - depth && y >= 0; y--) {
            for (int z = zPos; z < volume.getBlockMax().getZ(); z++) {
                volume.setBlock(xPos, y, z, state, Plots.PLOTS_CAUSE());
                blocksToProcess--;
            }
        }

        if (--xPos < min.getX()) {
            xPos = min.getX();
            phase++;
        }

        return blocksToProcess;
    }

    private int phase3(int blocksToProcess) {
        for (int y = level; y > level - depth && y >= 0; y--) {
            for (int x = xPos; x > volume.getBlockMin().getX(); x--) {
                volume.setBlock(x, y, zPos, state, Plots.PLOTS_CAUSE());
                blocksToProcess--;
            }
        }

        if (--zPos < min.getZ()) {
            zPos = min.getZ();
            phase++;
        }

        return blocksToProcess;
    }
}
