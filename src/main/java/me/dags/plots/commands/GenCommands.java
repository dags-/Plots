package me.dags.plots.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import me.dags.commandbus.Format;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.plots.Plots;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.util.IO;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.biome.BiomeType;

import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class GenCommands {

    private static final int TIME_OUT = 120;
    private static final Format FORMAT = Plots.getConfig().getMessageFormat();
    private static final Cache<CommandSource, GeneratorProperties.Builder> BUILDERS = CacheBuilder.newBuilder()
            .expireAfterAccess(TIME_OUT, TimeUnit.SECONDS)
            .removalListener(new RemoveListener())
            .weakKeys()
            .build();

    private Optional<GeneratorProperties.Builder> get(CommandSource source) {
        Optional<GeneratorProperties.Builder> get = Optional.ofNullable(BUILDERS.getIfPresent(source));
        if (!get.isPresent()) {
            FORMAT.error("You are not currently building a generator").tell(source);
        }
        return get;
    }

    @Command(aliases = {"help", "?"}, parent = "gen", perm = "plots.commands.gen.help")
    public void help(@Caller CommandSource source) {
        FORMAT.info("/gen create <name>").tell(source);
        FORMAT.info("/gen biome <biome>").tell(source);
        FORMAT.info("/gen rule <gamerule> <value>").tell(source);
        FORMAT.info("/gen dims <plot x width> <plot z width> <path width> <wall width>").tell(source);
        FORMAT.info("/gen layer <plot material> <path material> <wall material> <layer thickness>").tell(source);
        FORMAT.info("/gen reload").tell(source);
        FORMAT.info("/gen save").tell(source);
    }

    @Command(aliases = "create", parent = "gen", perm = "plots.command.gen.create")
    public void create(@Caller CommandSource source, @One("name") String name) {
        BUILDERS.put(source, GeneratorProperties.builder().name(name));
        FORMAT.info("Building new Generator ").stress(name).tell(source);
        FORMAT.subdued("See ").stress("/gen help").subdued(" for a list of available commands").tell(source);
        FORMAT.subdued("Changes will be discarded if you are inactive for +" + TIME_OUT + " seconds").tell(source);
    }

    @Command(aliases = "dims", parent = "gen", perm = "plots.command.gen.dims")
    public void dims(@Caller CommandSource source, @One("x width") int x, @One("z width") int z, @One("path width") int path, @One("wall width") int wall) {
        plot(source, x, z);
        wall(source, wall);
        path(source, path);
    }

    @Command(aliases = "path", parent = "gen dim", perm = "plots.command.gen.dims")
    public void path(@Caller CommandSource source, @One("width") int width) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().pathWidth(width);
            FORMAT.info("Set path width to ").stress(width).tell(source);
        }
    }

    @Command(aliases = "plot", parent = "gen dim", perm = "plots.command.gen.dims")
    public void plot(@Caller CommandSource source, @One("x width") int xWidth, @One("z width") int zWidth) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().xWidth(xWidth).zWidth(zWidth);
            FORMAT.info("Set plot dimensions to ").stress(xWidth).info("x").stress(zWidth).tell(source);
        }
    }

    @Command(aliases = "wall", parent = "gen dim", perm = "plots.command.gen.dims")
    public void wall(@Caller CommandSource source, @One("width") int width) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().wallWidth(width);
            FORMAT.info("Set wall width to ").stress(width).tell(source);
        }
    }

    @Command(aliases = "biome", parent = "gen", perm = "plots.command.gen.biome")
    public void biome(@Caller CommandSource source, @One("biome") String biome) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            Optional<BiomeType> type = Sponge.getRegistry().getType(BiomeType.class, biome);
            if (type.isPresent()) {
                builder.get().biome(type.get());
                FORMAT.info("Set biome to ").stress(biome).tell(source);
            } else {
                FORMAT.error("Biome ").stress(biome).error(" not recognised").tell(source);
            }
        }
    }

    @Command(aliases = "rule", parent = "gen", perm = "plots.command.gen.rule")
    public void rule(@Caller CommandSource source, @One("rule") String rule, @One("value") String value) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().gameRule(rule, value);
            FORMAT.info("Set gamerule ").stress(rule).info(" to ").stress(value).tell(source);
        }
    }

    @Command(aliases = "layer", parent = "gen", perm = "plots.command.gen.layer")
    public void layer(@Caller CommandSource source, @One("plot material") String plot, @One("path material") String path, @One("wall material") String wall, @One("thickness") int thickness) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            Optional<BlockType> plotType = Sponge.getRegistry().getType(BlockType.class, plot);
            Optional<BlockType> pathType = Sponge.getRegistry().getType(BlockType.class, path);
            Optional<BlockType> wallType = Sponge.getRegistry().getType(BlockType.class, wall);

            boolean error = false;
            Format.MessageBuilder errorMessage = FORMAT.error("Unknown material(s): ");
            if (!plotType.isPresent()) {
                errorMessage.error("plot=").stress(plot);
                error = true;
            }
            if (!pathType.isPresent()) {
                errorMessage.error(error ? ", path=" : "path=").stress(path);
                error = true;
            }
            if (!wallType.isPresent()) {
                errorMessage.error(error ? ", wall=" : "wall=").stress(path);
            }

            if (plotType.isPresent() && wallType.isPresent() && pathType.isPresent()) {
                builder.get().layer(plotType.get(), wallType.get(), pathType.get(), thickness);
                FORMAT.info("Set layer to (plot=").stress(plot)
                        .info(", path=").stress(path)
                        .info(", wall=").stress(wall)
                        .info(")x").stress(thickness)
                        .tell(source);
            } else {
                errorMessage.tell(source);
            }
        }
    }

    @Command(aliases = "save", parent = "gen", perm = "plots.command.gen.save")
    public void save(@Caller CommandSource source) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            GeneratorProperties properties = builder.get().build();
            IO.saveProperties(properties, Plots.getApi().generatorsDir());
            FORMAT.info("Saved generator ").stress(properties.name()).info(" to file").tell(source);
        }
    }

    @Command(aliases = "reload", parent = "gen", perm = "plots.command.gen.reload")
    public void reload(@Caller CommandSource source) {
        FORMAT.info("Reloading generators...").tell(source);
        if (!Files.exists(Plots.getApi().configDir().resolve("generators").resolve("default.conf"))) {
            IO.saveProperties(GeneratorProperties.DEFAULT, Plots.getApi().configDir().resolve("generators"));
        }
        IO.loadGeneratorProperties(Plots.getApi().configDir().resolve("generators")).forEach(Plots.getApi()::register);
    }

    private static class RemoveListener implements RemovalListener<CommandSource, GeneratorProperties.Builder> {

        @Override
        public void onRemoval(RemovalNotification<CommandSource, GeneratorProperties.Builder> notification) {
            CommandSource source = notification.getKey();
            if (source != null && (!(source instanceof Player) || ((Player) source).isOnline())) {
                FORMAT.subdued("Your generator creation session has timed out").tell(source);
            }
        }
    }
}
