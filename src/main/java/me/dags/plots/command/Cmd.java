package me.dags.plots.command;

import me.dags.commandbus.utils.CommandSourceCache;
import me.dags.commandbus.utils.Format;
import me.dags.plots.Plots;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class Cmd {

    private static Format FMT = Format.builder().build();
    private static final CommandSourceCache<CommandSource, GeneratorProperties.Builder> genBuilders = CommandSourceCache.builder()
            .expireTime(3)
            .timeUnit(TimeUnit.MINUTES)
            .messageFormat(Format.DEFAULT)
            .addMessage("Started new generator editor session")
            .expireMessage("Your generator editor session has expired")
            .noElementMessage("You are not currently editing a generator")
            .build();

    public static Format FMT() {
        return FMT;
    }

    public static CommandSourceCache<CommandSource, GeneratorProperties.Builder> genBuilders() {
        return genBuilders;
    }

    public static void setFormat(Format format) {
        if (format != null) {
            Cmd.FMT = format;
        }
    }

    // Get the plot currently containing the Player
    public static Pair<PlotWorld, PlotId> getContainingPlot(Player player) {
        Optional<PlotWorld> world = getWorld(player, player.getWorld().getName());
        if (world.isPresent()) {
            PlotId plotId = world.get().plotSchema().containingPlot(player.getLocation().getBlockPosition());
            return Pair.of(world.get(), plotId);
        }
        return Pair.empty();
    }

    // Get the nearest plot to the given Player
    public static Pair<PlotWorld, PlotId> getPlot(Player player) {
        Optional<PlotWorld> world = getWorld(player, player.getWorld().getName());
        if (world.isPresent()) {
            PlotId plotId = world.get().plotSchema().plotId(player.getLocation().getBlockPosition());
            return Pair.of(world.get(), plotId);
        }
        return Pair.empty();
    }

    public static Optional<PlotWorld> getWorld(Player source) {
        return getWorld(source, source.getWorld().getName());
    }

    // Get a PlotWorld by name, alerts the source if it is not a PlotWorld
    public static Optional<PlotWorld> getWorld(CommandSource source, String world) {
        Optional<PlotWorld> optional = Plots.API().plotWorld(world);
        if (!optional.isPresent()) {
            FMT().error("World {} is not a PlotWorld", world).tell(source);
        }
        return optional;
    }
}
