package me.dags.plots.operation;

import me.dags.plots.PlotsPlugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class OperationDispatcher implements Runnable {

    private final ArrayDeque<Operation> queue = new ArrayDeque<>();
    private final List<Operation> operations = new ArrayList<>();
    private final String name;
    private final int bpt;

    private boolean closed = false;

    public OperationDispatcher(String name, int blocksPerTick) {
        this.name = name;
        this.bpt = blocksPerTick;
    }

    public void queueOperation(Operation operation) {
        if (closed) {
            return;
        }
        queue.add(operation);
    }

    @Override
    public void run() {
        if (closed) {
            return;
        }

        drainQueue();

        if (operations.size() > 0) {
            // Calc blocks per operation
            int bpo = bpt / operations.size(), extra = 0;

            Iterator<Operation> iterator = operations.iterator();
            while (iterator.hasNext()) {
                Operation operation = iterator.next();

                try {
                    // Returns the number of unused iterations. Let the next operation use them instead.
                    extra = operation.process(bpo + extra);

                    // If operation has finished, remove from list
                    if (operation.complete()) {
                        iterator.remove();
                    }
                } catch (Throwable t) {
                    // Log & dispose the failing operation to avoid repeat errors
                    t.printStackTrace();
                    iterator.remove();
                }
            }
        }
    }

    public void finishAll(String world) {
        if (closed) {
            return;
        }

        // Remove queued operations for the world
        if (queue.size() > 0) {
            Iterator<Operation> queued = queue.iterator();
            while (queued.hasNext()) {
                Operation operation = queued.next();
                if (operation.getWorld().equals(world)) {
                    queued.remove();
                }
            }
        }

        // Finish all active operations
        if (operations.size() > 0) {
            Iterator<Operation> iterator = operations.iterator();

            while (iterator.hasNext()) {
                Operation operation = iterator.next();

                if (!operation.getWorld().equals(world)) {
                    continue;
                }

                try {
                    while (!operation.complete()) {
                        operation.process(Integer.MAX_VALUE);
                    }
                    iterator.remove();
                } catch (Throwable t) {
                    t.printStackTrace();
                    iterator.remove();
                }
            }
        }
    }

    public void finishAll() {
        if (closed) {
            return;
        }

        closed = true;
        queue.clear();

        PlotsPlugin.log("Finishing all remaining block operations for world: {}", name);
        for (Operation operation : operations) {
            while (!operation.complete()) {
                operation.process(Integer.MAX_VALUE);
            }
        }
    }

    private void drainQueue() {
        while (queue.size() > 0) {
            Operation operation = queue.poll();
            if (operation != null) {
                operations.add(operation);
            }
        }
    }
}
