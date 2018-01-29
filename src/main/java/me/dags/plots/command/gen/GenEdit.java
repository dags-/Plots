package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Formatter;
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

    @Permission(Permissions.GEN_EDIT)
    @Command("gen dims <x> <z> <path> <wall>")
    public void dims(@Src CommandSource source, int x, int z, int path, int wall) {
        plot(source, x, z);
        wall(source, wall);
        path(source, path);
    }

    @Permission(Permissions.GEN_EDIT)
    @Command("gen dim path <width>")
    public void path(@Src CommandSource source, int width) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().pathWidth(width);
            Fmt.info("Set path width to ").stress(width).tell(source);
        }
    }

    @Permission(Permissions.GEN_EDIT)
    @Command("gen dim plot <x> <z>")
    public void plot(@Src CommandSource source, int xWidth, int zWidth) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().xWidth(xWidth).zWidth(zWidth);
            Fmt.info("Set plot dimensions to ").stress(xWidth).info("x").stress(zWidth).tell(source);
        }
    }

    @Permission(Permissions.GEN_EDIT)
    @Command("gen dim wall <width>")
    public void wall(@Src CommandSource source, int width) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().wallWidth(width);
            Fmt.info("Set wall width to ").stress(width).tell(source);
        }
    }

    @Permission(Permissions.GEN_EDIT)
    @Command("gen biome <biome>")
    public void biome(@Src CommandSource source, String biome) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            Optional<BiomeType> type = Sponge.getRegistry().getType(BiomeType.class, biome);
            if (type.isPresent()) {
                builder.get().biome(type.get());
                Fmt.info("Set biome to ").stress(biome).tell(source);
            } else {
                Fmt.error("Biome ").stress(biome).error(" not recognised").tell(source);
            }
        }
    }

    @Permission(Permissions.GEN_EDIT)
    @Command("gen rule <rule> <value>")
    public void rule(@Src CommandSource source, String rule, String value) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            builder.get().gameRule(rule, value);
            Fmt.info("Set gamerule ").stress(rule).info(" to ").stress(value).tell(source);
        }
    }

    @Permission(Permissions.GEN_EDIT)
    @Command("gen layer <plot_material> <path_material> <wall_material> <thickness>")
    public void layer(@Src CommandSource source, String plot, String path, String wall, int thickness) {
        Optional<GeneratorProperties.Builder> builder = get(source);
        if (builder.isPresent()) {
            Optional<BlockType> plotType = Sponge.getRegistry().getType(BlockType.class, plot);
            Optional<BlockType> pathType = Sponge.getRegistry().getType(BlockType.class, path);
            Optional<BlockType> wallType = Sponge.getRegistry().getType(BlockType.class, wall);

            boolean error = false;
            Formatter err = Fmt.error("Unknown material(s): ");
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
                Fmt.info("Set layer to (plot=").stress(plot)
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
