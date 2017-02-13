package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.commandbus.format.Formatter;
import me.dags.plots.Permissions;
import me.dags.plots.command.Cmd;
import me.dags.plots.generator.GeneratorProperties;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.world.biome.BiomeType;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class GenEdit {

    private static Optional<GeneratorProperties.Builder> get(CommandSource source) {
        return Cmd.genBuilders().get(source);
    }

    @Command(aliases = "dims", parent = "gen", perm = @Permission(Permissions.GEN_EDIT))
    public void dims(@Caller CommandSource source, @One("x width") int x, @One("z width") int z, @One("path width") int path, @One("wall width") int wall) {
        plot(source, x, z);
        wall(source, wall);
        path(source, path);
    }

    @Command(aliases = "path", parent = "gen dim", perm = @Permission(Permissions.GEN_EDIT))
    public void path(@Caller CommandSource source, @One("width") int width) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().pathWidth(width);
            FMT.info("Set path width to ").stress(width).tell(source);
        }
    }

    @Command(aliases = "plot", parent = "gen dim", perm = @Permission(Permissions.GEN_EDIT))
    public void plot(@Caller CommandSource source, @One("x width") int xWidth, @One("z width") int zWidth) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().xWidth(xWidth).zWidth(zWidth);
            FMT.info("Set plot dimensions to ").stress(xWidth).info("x").stress(zWidth).tell(source);
        }
    }

    @Command(aliases = "wall", parent = "gen dim", perm = @Permission(Permissions.GEN_EDIT))
    public void wall(@Caller CommandSource source, @One("width") int width) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().wallWidth(width);
            FMT.info("Set wall width to ").stress(width).tell(source);
        }
    }

    @Command(aliases = "biome", parent = "gen", perm = @Permission(Permissions.GEN_EDIT))
    public void biome(@Caller CommandSource source, @One("biome") String biome) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            Optional<BiomeType> type = Sponge.getRegistry().getType(BiomeType.class, biome);
            if (type.isPresent()) {
                builder.get().biome(type.get());
                FMT.info("Set biome to ").stress(biome).tell(source);
            } else {
                FMT.error("Biome ").stress(biome).error(" not recognised").tell(source);
            }
        }
    }

    @Command(aliases = "rule", parent = "gen", perm = @Permission(Permissions.GEN_EDIT))
    public void rule(@Caller CommandSource source, @One("rule") String rule, @One("value") String value) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().gameRule(rule, value);
            FMT.info("Set gamerule ").stress(rule).info(" to ").stress(value).tell(source);
        }
    }

    @Command(aliases = "layer", parent = "gen", perm = @Permission(Permissions.GEN_EDIT))
    public void layer(@Caller CommandSource source, @One("plot material") String plot, @One("path material") String path, @One("wall material") String wall, @One("thickness") int thickness) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            Optional<BlockType> plotType = Sponge.getRegistry().getType(BlockType.class, plot);
            Optional<BlockType> pathType = Sponge.getRegistry().getType(BlockType.class, path);
            Optional<BlockType> wallType = Sponge.getRegistry().getType(BlockType.class, wall);

            boolean error = false;
            Formatter err = FMT.error("Unknown material(s): ");
            if (!plotType.isPresent()) {
                err.error("plot=").stress(plot);
                error = true;
            }
            if (!pathType.isPresent()) {
                err.error(error ? ", path=" : "path=").stress(path);
                error = true;
            }
            if (!wallType.isPresent()) {
                err.error(error ? ", wall=" : "wall=").stress(path);
            }

            if (plotType.isPresent() && wallType.isPresent() && pathType.isPresent()) {
                builder.get().layer(plotType.get(), wallType.get(), pathType.get(), thickness);
                FMT.info("Set layer to (plot=").stress(plot)
                        .info(", path=").stress(path)
                        .info(", wall=").stress(wall)
                        .info(")x").stress(thickness)
                        .tell(source);
            } else {
                err.tell(source);
            }
        }
    }
}
