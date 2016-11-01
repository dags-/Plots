package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.database.UserActions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Claim {

    @Command(aliases = "claim", parent = "plot", desc = "Claim a plot", perm = @Permission(Permissions.PLOT_CLAIM))
    public void claim(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Supplier<Boolean> owned = () -> PlotActions.findPlotOwner(world.database(), plotId).isPresent();
            Consumer<Boolean> claim = claimIfFree(player, world, plotId);
            Plots.executor().async(owned, claim);
        }
    }

    static Consumer<Boolean> claimIfFree(Player player, PlotWorld world, PlotId plotId) {
        return owned -> {
            if (owned) {
                Cmd.FMT.error("Plot ").stress(plotId).error(" is already owned").tell(player);
            } else {
                claim(player, world, plotId);
            }
        };
    }

    static void claim(Player player, PlotWorld world, PlotId plotId) {
        if (!plotId.present()) {
            Cmd.FMT.error("Plot ").stress(plotId).error(" is not present").tell(player);
        } else {
            PlotUser user = world.user(player.getUniqueId());
            if (user.hasPlot() && !user.approved() && !player.hasPermission(Permissions.PLOT_APPROVAL_BYPASS)) {
                Cmd.FMT.error("You must have one of your plots approved before claiming a new one").tell(player);
                return;
            }

            Runnable async = () -> {
                PlotActions.setPlotOwner(world.database(), plotId, player.getUniqueId());
                UserActions.addPlot(world.database(), player.getUniqueId(), plotId);
            };

            Runnable callback = () -> {
                world.refreshUser(player.getUniqueId());
                Cmd.FMT.info("Claimed plot ").stress(plotId).tell(player);
            };

            Plots.executor().async(async, callback);
        }
    }
}
