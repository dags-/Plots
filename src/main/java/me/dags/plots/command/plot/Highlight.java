package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.database.WorldDatabase;
import me.dags.plots.operation.FillWallsOperation;
import me.dags.plots.plot.PlotBounds;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Highlight {

    @Command(aliases = "highlight", parent = "plot", desc = "Highlight your nearby plots", perm = @Permission(Permissions.PLOT_HIGHLIGHT))
    public void highlight(@Caller Player player) {
        highlight(player, player);
    }

    @Command(aliases = "highlight", parent = "plot", desc = "Highlight player's nearby plots", perm = @Permission(Permissions.PLOT_HIGHLIGHT_OTHER))
    public void highlight(@Caller Player player, @One("player") User user) {
        Pair<PlotWorld, PlotId> pair = Cmd.getPlot(player);
        if (pair.present()) {
            WorldDatabase database = pair.first().database();
            UUID uuid = user.getUniqueId();
            Supplier<List<PlotId>> search = () -> PlotActions.findOwnedPlots(database, uuid, pair.second(), 3);
            Consumer<List<PlotId>> highlight = highlight(pair.first(), player);
            Plots.executor().async(search, highlight);
            Cmd.FMT().info("Highlighting your nearby plots...").tell(player);
        }
    }

    private static Consumer<List<PlotId>> highlight(PlotWorld plotWorld, Player viewer) {
        return plotIds -> {
            if (plotWorld.equalsWorld(viewer.getWorld())) {
                PlotUser plotUser = plotWorld.user(viewer.getUniqueId());
                for (PlotId plotId : plotIds) {
                    PlotBounds bounds = plotUser.plotMask().plots().get(plotId);
                    if (bounds == null) {
                        continue;
                    }
                    FillWallsOperation operation = new FillWallsOperation(viewer, plotWorld, plotId);
                    Plots.API().dispatcher().queueOperation(operation);
                }
            }
        };
    }
}
