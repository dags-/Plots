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
        for (; y <= maxY; y++) {
            for (; x <= maxX; x++) {
                for (; z <= maxZ; z++) {
                    processAt(x, y, z);

                    if (blocksToProcess-- <= 0) {
                        return 0;
                    }
                }
                this.z = 0;
            }
            this.x = 0;
        }
        complete = true;
        if (callback != null) {
            callback.run();
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
