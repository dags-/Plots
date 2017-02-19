package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.*;
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
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Unclaim {

    @Command(alias = "unclaim", parent = "plot")
    @Permission(Permissions.PLOT_UNCLAIM)
    @Description("Unclaim a plot and reset it")
    public void unclaim(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            FMT.warn("Unclaiming a plot will remove all whitelisted users including the owner!").append(Text.NEW_LINE)
                    .warn("To confirm, use either:").append(Text.NEW_LINE)
                    .stress(" /plot unclaim true").warn(" - to unclaim and reset the plot").append(Text.NEW_LINE)
                    .stress(" /plot unclaim false").warn(" - to unclaim and not reset the plot")
                    .tell(player);
        }
    }

    @Command(alias = "unclaim", parent = "plot")
    @Permission(Permissions.PLOT_UNCLAIM)
    @Description("Unclaim a plot and reset it")
    public void unclaim(@Caller Player player, @One("reset") boolean reset) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            PlotUser user = world.user(player.getUniqueId());

            if (!reset && !user.approved() && !player.hasPermission(Permissions.PLOT_APPROVAL_BYPASS)) {
                FMT.warn("Your plot must be reset if you want to unclaim it, use ")
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
                FMT.error("Plot ").stress(plotId).error(" is not owned by anyone").tell(player);
            } else if (player.hasPermission(Permissions.PLOT_UNCLAIM_OTHER) || owner.get().equals(player.getUniqueId())) {
                unclaim(player, plotWorld, plotId, reset);
            } else {
                FMT.error("You do not own plot ").stress(plotId).tell(player);
            }
        };
    }

    static void unclaim(Player player, PlotWorld plotWorld, PlotId plotId, boolean reset) {
        FMT.info("Unclaiming plot ").stress(plotId).tell(player);

        Runnable async = () -> {
            PlotActions.removePlot(plotWorld.database(), plotId);
            UserActions.removeAllPlot(plotWorld.database(), plotId);
        };

        Runnable callback = () -> plotWorld.refreshUser(player.getUniqueId());

        Plots.executor().async(async, callback);

        if (reset) {
            plotWorld.resetPlot(plotId, () -> FMT.info("Plot ").stress(plotId).info(" has been reset").tell(player));
        }
    }
}
