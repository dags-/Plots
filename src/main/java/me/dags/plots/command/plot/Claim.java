package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
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

    @Command(alias = "claim", parent = "plot")
    @Permission(Permissions.PLOT_CLAIM)
    @Description("Claim a plot")
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

    private static Consumer<Boolean> claimIfFree(Player player, PlotWorld world, PlotId plotId) {
        return owned -> {
            if (owned) {
                FMT.error("Plot ").stress(plotId).error(" is already owned").tell(player);
            } else {
                claim(player, world, plotId);
            }
        };
    }

    static void claim(Player player, PlotWorld world, PlotId plotId) {
        if (!plotId.present()) {
            FMT.error("Plot ").stress(plotId).error(" is not present").tell(player);
        } else {
            PlotUser user = world.user(player.getUniqueId());
            if (user.maxClaimCount() > -1 && user.plotCount() >= user.maxClaimCount()) {
                FMT.error("You cannot claim any more plots").tell(player);
                return;
            }

            Runnable async = () -> {
                PlotActions.setPlotOwner(world.database(), plotId, player.getUniqueId());
                UserActions.addPlot(world.database(), player.getUniqueId(), plotId);
            };

            Runnable callback = () -> {
                FMT.info("Claimed plot ").stress(plotId).tell(player);
                world.refreshUser(player.getUniqueId());
                Walls.setWalls(player, world, plotId, Plots.config().getOwnedPlotWall(), 1);
            };

            Plots.executor().async(async, callback);
        }
    }
}
