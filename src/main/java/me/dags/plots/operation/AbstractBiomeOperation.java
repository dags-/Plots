package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector2i;

/**
 * @author dags <dags@dags.me>
 */
public abstract class AbstractBiomeOperation implements Operation {

    private final String world;
    private final int maxX;
    private final int maxZ;
    private int x = 0;
    private int z = 0;
    private boolean complete = false;
    private Runnable callback = null;

    AbstractBiomeOperation(String world, Vector2i min, Vector2i max) {
        this.world = world;
        this.maxX = max.getX() - min.getX();
        this.maxZ = max.getY() - min.getY();
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public int process(int blocksToProcess) {
        for (; x <= maxX; x++) {
            for (; z <= maxZ; z++) {
                processAt(x, z);

                if (blocksToProcess-- <= 0) {
                    return 0;
                }
            }
            this.z = 0;
        }
        complete = true;
        if (callback != null) {
            callback.run();
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
