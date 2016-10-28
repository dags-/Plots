package me.dags.plots.operation;

/**
 * @author dags <dags@dags.me>
 */
public interface Operation {

    String getWorld();

    int process(int blocksToProcess);

    boolean complete();

    void onComplete(Runnable callback);
}
