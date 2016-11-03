package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.UserActions;
import me.dags.plots.plot.PlotWorld;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class List {

    @Command(aliases = "list", parent = "plot", desc = "List your plots", perm = @Permission(Permissions.PLOT_LIST))
    public void list(@Caller Player player) {
        list(player, player);
    }

    @Command(aliases = "list", parent = "plot", desc = "List plots of a player", perm = @Permission(Permissions.PLOT_LIST_OTHER))
    public void list(@Caller Player player, @One("player")User user) {
        Optional<PlotWorld> world = Cmd.getWorld(player);
        if (world.isPresent()) {
            PlotWorld plotWorld = world.get();
            String title = user.getName() + "'s Plots";
            UUID uuid = user.getUniqueId();
            Supplier<PaginationList> fetch = () -> UserActions.listPlots(plotWorld.database(), title, uuid, Cmd.FMT());
            Consumer<PaginationList> send = list -> list.sendTo(player);
            Plots.executor().async(fetch, send);
        }
    }
}
