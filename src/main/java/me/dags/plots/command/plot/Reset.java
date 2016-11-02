package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.plot.PlotId;
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
public class Reset {

    @Command(aliases = "reset", parent = "plot", desc = "Reset the plot", perm = @Permission(Permissions.PLOT_RESET))
    public void reset(@Caller Player player) {
        Cmd.FMT.warn("Resetting a plot will delete everything inside it!").append(Text.NEW_LINE)
                .warn("Use ").stress("/plot reset true").warn(" if you want to proceed")
                .tell(player);
    }

    @Command(aliases = "reset", parent = "plot", desc = "Reset the plot", perm = @Permission(Permissions.PLOT_RESET))
    public void reset(@Caller Player player, @One("confirm") boolean confirm) {
        if (!confirm) {
            Cmd.FMT.error("You must confirm you want to delete the plot by using ").append(Text.NEW_LINE)
                    .stress("/plot reset true")
                    .tell(player);
            return;
        }

        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Supplier<Optional<UUID>> owner = () -> PlotActions.findPlotOwner(world.database(), plotId);
            Consumer<Optional<UUID>> callback = reset(player, world, plotId);
            Plots.executor().async(owner, callback);
        }
    }

    static Consumer<Optional<UUID>> reset(Player player, PlotWorld world, PlotId plotId) {
        return uuid -> {
            if ((uuid.isPresent() && player.getUniqueId().equals(uuid.get())) || player.hasPermission(Permissions.PLOT_RESET_OTHER)) {
                Cmd.FMT.info("Resetting plot ").stress(plotId).info("...").tell(player);
                world.resetPlot(plotId, () -> Cmd.FMT.info("Reset complete for plot ").stress(plotId).tell(player));
            } else {
                Cmd.FMT.error("You do not own plot ").stress(plotId).tell(player);
            }
        };
    }
}
