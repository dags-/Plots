package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
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

    @Permission(Permissions.PLOT_LIKE)
    @Description("'Like' a plot")
    @Command("plot like")
    public void like(@Src Player player) {
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
                Fmt.info("You liked plot ").stress(plotId).tell(player);
                Plots.executor().async(() -> PlotActions.addLike(world.database(), plotId, player.getUniqueId()));
            } else {
                Fmt.error("Plot ").stress(plotId).error(" is not owned").tell(player);
            }
        };
    }
}
