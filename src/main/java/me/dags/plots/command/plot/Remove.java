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
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Remove {

    @Command("plot remove <user>")
    @Permission(Permissions.PLOT_REMOVE)
    @Description("Remove someone from the plot")
    public void remove(@Src Player player, User user) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            boolean removeAny = player.hasPermission(Permissions.PLOT_REMOVE_ANY);

            if (player.getUniqueId().equals(user.getUniqueId()) && !removeAny) {
                Fmt.error("You cannot remove yourself from a plot").tell(player);
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
                        Fmt.info("Removed ").stress(target.getName()).info(" from plot ").stress(plotId).tell(player);
                        target.getPlayer().ifPresent(Fmt.stress(player.getName()).info(" removed you from plot ").stress(plotId)::tell);
                    };
                    Plots.executor().async(async, sync);
                } else {
                    Fmt.error("You do not have permission to remove people from plot ").stress(plotId).tell(player);
                }
            } else {
                Fmt.error("Nobody owns plot ").stress(plotId).tell(player);
            }
        };
    }
}
