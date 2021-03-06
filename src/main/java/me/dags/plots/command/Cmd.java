package me.dags.plots.command;

import me.dags.commandbus.fmt.Fmt;
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

    private static final CommandCache<GeneratorProperties.Builder> genBuilders = new CommandCache<>("gen", 3, TimeUnit.MINUTES);

    public static CommandCache<GeneratorProperties.Builder> genBuilders() {
        return genBuilders;
    }

    // Get the plot currently containing the Player
    public static Pair<PlotWorld, PlotId> getContainingPlot(Player player) {
        Optional<PlotWorld> world = getWorld(player, player.getWorld().getName());
        if (world.isPresent()) {
            PlotId plotId = world.get().plotSchema().containingPlot(player.getLocation().getBlockPosition());
            if (plotId.present()) {
                return Pair.of(world.get(), plotId);
            }
            Fmt.error("You are not inside a plot").tell(player);
        }
        return Pair.empty();
    }

    // Get the nearest plot to the given Player
    public static Pair<PlotWorld, PlotId> getPlot(Player player) {
        Optional<PlotWorld> world = getWorld(player, player.getWorld().getName());
        if (world.isPresent()) {
            PlotId plotId = world.get().plotSchema().plotId(player.getLocation().getBlockPosition());
            if (plotId.present()) {
                return Pair.of(world.get(), plotId);
            }
            Fmt.error("Could not find the nearest plot :[").tell(player);
        }
        return Pair.empty();
    }

    public static Optional<PlotWorld> getWorld(Player source) {
        return getWorld(source, source.getWorld().getName());
    }

    // Get a PlotWorld by name, alerts the source if it is not a PlotWorld
    public static Optional<PlotWorld> getWorld(CommandSource source, String world) {
        Optional<PlotWorld> optional = Plots.core().plotWorld(world);
        if (!optional.isPresent()) {
            Fmt.error("World {} is not a PlotWorld", world).tell(source);
        }
        return optional;
    }
}
