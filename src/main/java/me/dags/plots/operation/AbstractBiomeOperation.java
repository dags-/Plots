package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector2i;

/**
 * @author dags <dags@dags.me>
 */
public abstract class AbstractBiomeOperation implements Operation {

    private final int maxX;
    private final int maxZ;
    private int x = 0;
    private int z = 0;
    private boolean complete = false;
    private Runnable callback = null;

    AbstractBiomeOperation(Vector2i min, Vector2i max) {
        this.maxX = max.getX() - min.getX();
        this.maxZ = max.getY() - min.getY();
    }

    @Override
    public int process(int blocksToProcess) {
        for (int x = this.x; x < maxX && blocksToProcess > 0; x++) {
            for (int z = this.z; z < maxZ && blocksToProcess-- > 0; z++) {
                processAt(x, z);
            }
            this.z = 0;
        }
        if (this.x == maxX) {
            complete = true;
            if (callback != null) {
                callback.run();
            }
        }
        return blocksToProcess;
    }

    @Override
    public void onComplete(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public boolean complete() {
        return complete;
    }

    abstract void processAt(int x, int z);
}
