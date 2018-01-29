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
import me.dags.plots.database.UserActions;
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
public class Unclaim {

    @Command("plot unclaim")
    @Permission(Permissions.PLOT_UNCLAIM)
    @Description("Unclaim a plot and reset it")
    public void unclaim(@Src Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            Fmt.warn("Unclaiming a plot will remove all whitelisted users including the owner!")
                    .line().warn("To confirm, use either:")
                    .line().stress(" /plot unclaim true").warn(" - to unclaim and reset the plot")
                    .line().stress(" /plot unclaim false").warn(" - to unclaim and not reset the plot")
                    .tell(player);
        }
    }

    @Command("plot unclaim <confirm>")
    @Permission(Permissions.PLOT_UNCLAIM)
    @Description("Unclaim a plot and reset it")
    public void unclaim(@Src Player player, boolean reset) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();

            if (!reset && !player.hasPermission(Permissions.PLOT_UNCLAIM_BYPASS)) {
                Fmt.warn("Your plot must be reset if you want to unclaim it, use ")
                        .stress("/plot unclaim true")
                        .error(" to proceed")
                        .tell(player);
                return;
            }

            Supplier<Optional<UUID>> owner = () -> PlotActions.findPlotOwner(world.database(), plotId);
            Consumer<Optional<UUID>> unclaim = unclaimIfOwned(player, world, plotId, reset);
            Plots.executor().async(owner, unclaim);
        }
    }

    static Consumer<Optional<UUID>> unclaimIfOwned(Player player, PlotWorld plotWorld, PlotId plotId, boolean reset) {
        return owner -> {
            if (!owner.isPresent()) {
                Fmt.error("Plot ").stress(plotId).error(" is not owned by anyone").tell(player);
            } else if (player.hasPermission(Permissions.PLOT_UNCLAIM_OTHER) || owner.get().equals(player.getUniqueId())) {
                unclaim(player, plotWorld, plotId, reset);
            } else {
                Fmt.error("You do not own plot ").stress(plotId).tell(player);
            }
        };
    }

    static void unclaim(Player player, PlotWorld plotWorld, PlotId plotId, boolean reset) {
        Fmt.info("Unclaiming plot ").stress(plotId).tell(player);

        Runnable async = () -> {
            PlotActions.removePlot(plotWorld.database(), plotId);
            UserActions.removeAllPlot(plotWorld.database(), plotId);
        };

        Runnable callback = () -> plotWorld.refreshUser(player.getUniqueId());

        Plots.executor().async(async, callback);

        if (reset) {
            plotWorld.resetPlot(plotId, () -> Fmt.info("Plot ").stress(plotId).info(" has been reset").tell(player));
        }
    }
}
