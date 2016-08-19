package me.dags.plots.operation;

import me.dags.plots.Plots;

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

    public void addOperation(Operation operation) {
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

                // The number of blocks actually processed.
                // If the operation completes before hitting it's bpo, pass the spare to the next operation.
                int processed = operation.process(bpo + extra);
                extra = processed < bpo ? bpo - processed : 0;

                // If operation has finished, remove from list
                if (operation.complete()) {
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

        Plots.log("Finishing all remaining block operations for world: {}", name);
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
