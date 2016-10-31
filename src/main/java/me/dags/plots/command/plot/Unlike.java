package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Unlike {

    @Command(aliases = "unlike", parent = "plot", desc = "Un-like a plot", perm = @Permission(id = Permissions.PLOT_LIKE, description = ""))
    public void unlike(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Plots.executor().async(isOwned(world, plotId), unlike(player, world, plotId));
        }
    }

    static Supplier<Boolean> isOwned(PlotWorld world, PlotId plotId) {
        return () -> PlotActions.findPlotOwner(world.database(), plotId).isPresent();
    }

    static Consumer<Boolean> unlike(Player player, PlotWorld world, PlotId plotId) {
        return owned -> {
            if (owned) {
                Cmd.FMT.info("You un-liked plot ").stress(plotId).tell(player);
                Plots.executor().async(() -> PlotActions.removeLike(world.database(), plotId, player.getUniqueId()));
            } else {
                Cmd.FMT.error("Plot ").stress(plotId).error(" is not owned").tell(player);
            }
        };
    }
}
