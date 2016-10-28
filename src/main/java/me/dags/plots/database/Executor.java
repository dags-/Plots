package me.dags.plots.database;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Executor {

    private final SpongeExecutorService syncExecutor;
    private final SpongeExecutorService asyncExecutor;

    public Executor(Object plugin) {
        syncExecutor = Sponge.getScheduler().createSyncExecutor(plugin);
        asyncExecutor = Sponge.getScheduler().createAsyncExecutor(plugin);
    }

    public void sync(Runnable runnable) {
        syncExecutor.execute(runnable);
    }

    public void async(Runnable runnable) {
        asyncExecutor.execute(runnable);
    }

    public <T> void async(Supplier<T> asyncTask, Consumer<T> syncCallback) {
        asyncExecutor.execute(() -> {
            T t = asyncTask.get();
            sync(() -> syncCallback.accept(t));
        });
    }
}
