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
public class Add {

    @Permission(Permissions.PLOT_ADD)
    @Description("Add someone to the plot")
    @Command("plot add <user>")
    public void add(@Src Player player, User user) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            boolean whitelistAny = player.hasPermission(Permissions.PLOT_ADD_ANY);

            if (!user.hasPermission(Permissions.PLOT_WHITELIST_RECIPIENT) && !whitelistAny) {
                Fmt.error("User ").stress(user.getName()).error(" does not hav permission to be added to a plot").tell(player);
                return;
            }

            if (player.getUniqueId().equals(user.getUniqueId()) && !whitelistAny) {
                Fmt.error("You cannot add yourself to a plot").tell(player);
                return;
            }

            Supplier<Optional<UUID>> owner = () -> PlotActions.findPlotOwner(world.database(), plotId);
            Consumer<Optional<UUID>> add = add(player, user, world, plotId, whitelistAny);
            Plots.executor().async(owner, add);
        }
    }

    static Consumer<Optional<UUID>> add(Player player, User target, PlotWorld world, PlotId plotId, boolean any) {
        return uuid -> {
            if (uuid.isPresent()) {
                if (player.getUniqueId().equals(uuid.get()) || any) {
                    Runnable async = () -> UserActions.addPlot(world.database(), target.getUniqueId(), plotId);
                    Runnable sync = () -> {
                        world.refreshUser(target.getUniqueId());
                        Fmt.info("Added ").stress(target.getName()).info(" to plot ").stress(plotId).tell(player);
                        target.getPlayer().ifPresent(Fmt.stress(player.getName()).info(" added you to plot ").stress(plotId)::tell);
                    };
                    Plots.executor().async(async, sync);
                } else {
                    Fmt.error("You do not have permission to add people to plot ").stress(plotId).tell(player);
                }
            } else {
                Fmt.error("Nobody owns plot ").stress(plotId).tell(player);
            }
        };
    }
}
