package me.dags.plots.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Executor {

    private final Object plugin;
    private SpongeExecutorService syncExecutor;
    private SpongeExecutorService asyncExecutor;

    public Executor(Object plugin) {
        this.plugin = plugin;
    }

    private SpongeExecutorService sync() {
        if (syncExecutor == null) {
            syncExecutor = Sponge.getScheduler().createSyncExecutor(plugin);
        }
        return syncExecutor;
    }

    private SpongeExecutorService async() {
        if (asyncExecutor == null) {
            asyncExecutor = Sponge.getScheduler().createAsyncExecutor(plugin);
        }
        return asyncExecutor;
    }

    public void close() {
        try {
            async().shutdown();
            async().awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sync(Runnable runnable) {
        sync().execute(runnable);
    }

    public void async(Runnable runnable) {
        async().execute(runnable);
    }

    public void async(Runnable runnable, Runnable callback) {
        async(() -> {
            runnable.run();
            sync(callback);
        });
    }

    public <T> void async(Supplier<T> asyncTask, Consumer<T> syncCallback) {
        async(() -> {
            T t = asyncTask.get();
            sync(() -> syncCallback.accept(t));
        });
    }
}
