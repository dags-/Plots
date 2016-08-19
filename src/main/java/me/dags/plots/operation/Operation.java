package me.dags.plots.operation;

/**
 * @author dags <dags@dags.me>
 */
public interface Operation {

    int process(int blocksToProcess);

    boolean complete();

    void onComplete(Runnable callback);
}
