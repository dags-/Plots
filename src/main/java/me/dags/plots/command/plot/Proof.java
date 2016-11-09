package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.UserActions;
import me.dags.plots.database.WorldDatabase;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class Proof {

    @Command(aliases = "proof", parent = "plot", desc = "Highlight your solo plots", perm = @Permission(Permissions.PLOT_PROOF))
    public void highlight(@Caller Player player) {
        highlight(player, player);
    }

    @Command(aliases = "proof", parent = "plot", desc = "Highlight a player's solo plots", perm = @Permission(Permissions.PLOT_PROOF_OTHER))
    public void highlight(@Caller Player player, @One("player") User user) {
        Pair<PlotWorld, PlotId> pair = Cmd.getPlot(player);
        if (pair.present()) {
            PlotId centre = pair.second();
            UUID uuid = user.getUniqueId();
            WorldDatabase database = pair.first().database();
            Supplier<List<PlotId>> find = () -> UserActions.findNearbySoloPlots(database, uuid, centre, 3).collect(Collectors.toList());
            Consumer<List<PlotId>> highlight = Highlight.highlight(pair.first(), player, Plots.config().proofBlock());
            Plots.executor().async(find, highlight);
        }
    }
}
