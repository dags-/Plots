package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.database.UserActions;
import me.dags.plots.database.WorldDatabase;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Copy {

    @Command(aliases = "copy", parent = "plot", desc = "Copy one plot to another", perm = @Permission(Permissions.PLOT_COPY))
    public void copy(@Caller Player player, @One("to") String to) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            Supplier<Pair<PlotId, PlotId>> validPlots = validPlots(player, plot.first(), plot.second().toString(), to);
            Consumer<Pair<PlotId, PlotId>> copy = copy(player, plot.first());
            Plots.executor().async(validPlots, copy);
        }
    }

    @Command(aliases = "copy", parent = "plot", desc = "Copy one plot to another", perm = @Permission(Permissions.PLOT_COPY))
    public void copy(@Caller Player player, @One("from") String from, @One("to") String to) {
        Optional<PlotWorld> world = Cmd.getWorld(player);
        if (world.isPresent()) {
            copy(player, world.get(), from, to);
        }
    }

    static void copy(Player player, PlotWorld world, String from, String to) {
        Supplier<Pair<PlotId, PlotId>> validPlots = validPlots(player, world, from, to);
        Consumer<Pair<PlotId, PlotId>> copy = copy(player, world);
        Plots.executor().async(validPlots, copy);
    }

    static Supplier<Pair<PlotId, PlotId>> validPlots(Player player, PlotWorld world, String from, String to) {
        WorldDatabase database = world.database();
        UUID uuid = player.getUniqueId();
        return () -> {
            PlotId fromId = matchPlot(database, from);
            PlotId toId = matchPlot(database, to);

            if (fromId.present() && toId.present()) {
                if (UserActions.hasPlot(database, uuid, fromId)) {
                    Optional<UUID> toOwner = PlotActions.findPlotOwner(database, toId);
                    if (toOwner.isPresent() && toOwner.get().equals(uuid)) {
                        return Pair.of(fromId, toId);
                    }
                }
            }

            return Pair.empty();
        };
    }

    static Consumer<Pair<PlotId, PlotId>> copy(Player player, PlotWorld world) {
        return plots -> {
            if (plots.present()) {
                PlotId from = plots.first();
                PlotId to = plots.second();
                FMT.info("Copying plot ").stress(from).info(" to ").stress(to).info("...").tell(player);
                Runnable callback = () -> FMT.info("Finished copying plot ").stress(from).info(" to ").stress(to).tell(player);
                world.copyPlot(from, to, callback);
            } else {
                FMT.error("You must be owner or added to the 'from' plot, and owner of the 'to' plot").tell(player);
            }
        };
    }

    private static PlotId matchPlot(WorldDatabase database, String plot) {
        if (PlotId.isValid(plot)) {
            return PlotId.parse(plot);
        }
        return PlotActions.plotFromAlias(database, plot);
    }
}
