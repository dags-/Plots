package me.dags.plots.command;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import me.dags.commandbus.fmt.Fmt;
import org.spongepowered.api.command.CommandSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class CommandCache<V> implements RemovalListener<CommandSource, V> {

    private final Cache<CommandSource, V> cache;
    private final String name;

    public CommandCache(String name, long expireTime, TimeUnit timeUnit) {
        this.name = name;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(expireTime, timeUnit)
                .removalListener(this)
                .weakKeys()
                .build();
    }

    public Optional<V> get(CommandSource source) {
        V v = cache.getIfPresent(source);
        if (v == null) {
            Fmt.subdued("No command: '%s' session present", name).tell(source);
        }
        return Optional.ofNullable(v);
    }

    public void add(CommandSource source, V v) {
        cache.put(source, v);
        Fmt.subdued("Started new command: '%s' session", name).tell(source);
    }

    public void remove(CommandSource source) {
        cache.invalidate(source);
    }

    @Override
    public void onRemoval(@Nullable CommandSource key, @Nullable V value, @Nonnull RemovalCause cause) {
        if (key != null && cause == RemovalCause.EXPIRED) {
            Fmt.subdued("Command: '%s' session expired", name).tell(key);
        }
    }
}
