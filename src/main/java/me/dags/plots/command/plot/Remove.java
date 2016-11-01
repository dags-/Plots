package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.database.UserActions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Remove {

    @Command(aliases = "remove", parent = "plot", desc = "Remove someone from the plot", perm = @Permission(Permissions.PLOT_REMOVE))
    public void remove(@Caller Player player, @One("player") User user) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            boolean removeAny = player.hasPermission(Permissions.PLOT_REMOVE_ANY);

            if (player.getUniqueId().equals(user.getUniqueId()) && !removeAny) {
                Cmd.FMT.error("You cannot remove yourself from a plot").tell(player);
                return;
            }

            Supplier<Optional<UUID>> owner = () -> PlotActions.findPlotOwner(world.database(), plotId);
            Consumer<Optional<UUID>> remove = remove(player, user, world, plotId, removeAny);
            Plots.executor().async(owner, remove);
        }
    }

    static Consumer<Optional<UUID>> remove(Player player, User target, PlotWorld world, PlotId plotId, boolean any) {
        return uuid -> {
            if (uuid.isPresent()) {
                if (player.getUniqueId().equals(uuid.get()) || any) {
                    Runnable async = () -> UserActions.removePlot(world.database(), target.getUniqueId(), plotId);
                    Runnable sync = () -> {
                        world.refreshUser(target.getUniqueId());
                        Cmd.FMT.info("Removed ").stress(target.getName()).info(" from plot ").stress(plotId).tell(player);
                        target.getPlayer().ifPresent(Cmd.FMT.stress(player.getName()).info(" removed you from plot ").stress(plotId)::tell);
                    };
                    Plots.executor().async(async, sync);
                } else {
                    Cmd.FMT.error("You do not have permission to remove people from plot ").stress(plotId).tell(player);
                }
            } else {
                Cmd.FMT.error("Nobody owns plot ").stress(plotId).tell(player);
            }
        };
    }
}
