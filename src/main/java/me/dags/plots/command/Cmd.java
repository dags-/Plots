package me.dags.plots.command;

import me.dags.commandbus.utils.Format;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Cmd {

    public static final Format FMT = Format.builder().build();

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

    // Get a PlotWorld by name, alerts the source if it is not a PlotWorld
    public static Optional<PlotWorld> getWorld(CommandSource source, String world) {
        Optional<PlotWorld> optional = Plots.API().plotWorld(world);
        if (!optional.isPresent()) {
            FMT.error("World {} is not a PlotWorld", world).tell(source);
        }
        return optional;
    }
}
