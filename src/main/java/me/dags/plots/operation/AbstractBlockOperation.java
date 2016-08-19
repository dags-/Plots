package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector3i;

/**
 * @author dags <dags@dags.me>
 */
public abstract class AbstractBlockOperation implements Operation {

    private final int maxX, maxY, maxZ;

    private boolean complete = false;
    private int x = 0;
    private int y = 0;
    private int z = 0;
    private Runnable callback;

    AbstractBlockOperation(Vector3i min, Vector3i max) {
        this.maxX = max.getX() - min.getX();
        this.maxY = max.getY() - min.getY();
        this.maxZ = max.getZ() - min.getZ();
    }

    @Override
    public int process(int blocksToProcess) {
        for (; y < maxY && blocksToProcess > 0; y++) {
            for (int x = this.x; x < maxX && blocksToProcess > 0; x++) {
                for (int z = this.z; z < maxZ && blocksToProcess-- > 0; z++) {
                    processAt(x, y, z);
                }
                this.z = 0;
            }
            this.x = 0;
        }
        if (y == maxY && x == maxX && z == maxZ) {
            this.complete = true;
            if (callback != null) {
                callback.run();
            }
        }
        return blocksToProcess;
    }

    @Override
    public boolean complete() {
        return complete;
    }

    @Override
    public void onComplete(Runnable callback) {
        this.callback = callback;
    }

    abstract void processAt(int x, int y, int z);
}
