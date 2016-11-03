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
public class Like {

    @Command(aliases = "like", parent = "plot", desc = "Like a plot", perm = @Permission(Permissions.PLOT_LIKE))
    public void like(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Supplier<Boolean> owned = () -> PlotActions.findPlotOwner(world.database(), plotId).isPresent();
            Consumer<Boolean> like = like(player, world, plotId);
            Plots.executor().async(owned, like);
        }
    }

    static Consumer<Boolean> like(Player player, PlotWorld world, PlotId plotId) {
        return owned -> {
            if (owned) {
                Cmd.FMT().info("You liked plot ").stress(plotId).tell(player);
                Plots.executor().async(() -> PlotActions.addLike(world.database(), plotId, player.getUniqueId()));
            } else {
                Cmd.FMT().error("Plot ").stress(plotId).error(" is not owned").tell(player);
            }
        };
    }
}
