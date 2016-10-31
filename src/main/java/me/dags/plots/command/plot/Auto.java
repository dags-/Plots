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
public class Auto {

    @Command(aliases = "auto", parent = "plot", desc = "Auto-claim a plot", perm = @Permission(id = Permissions.PLOT_AUTO, description = ""))
    public void auto(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Plots.executor().async(findPlot(world, plotId), claim(player, world));
        }
    }

    static Supplier<PlotId> findPlot(PlotWorld world, PlotId plotId) {
        return () -> PlotActions.findNextFreePlot(world.database(), plotId);
    }

    static Consumer<PlotId> claim(Player player, PlotWorld world) {
        return plotId -> Claim.claim(player, world, plotId);
    }
}
