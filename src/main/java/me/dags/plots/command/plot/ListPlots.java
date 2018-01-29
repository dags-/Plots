package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Format;
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
public class ListPlots {

    @Permission(Permissions.PLOT_LIST)
    @Description("List your plots")
    @Command("plot list")
    public void list(@Src Player player) {
        list(player, player);
    }

    @Permission(Permissions.PLOT_LIST_OTHER)
    @Description("List <player>'s plots")
    @Command("plot list <user>")
    public void list(@Src Player player, User user) {
        Optional<PlotWorld> world = Cmd.getWorld(player);
        if (world.isPresent()) {
            PlotWorld plotWorld = world.get();
            String title = user.getName() + "'s Plots";
            UUID uuid = user.getUniqueId();
            Format format = Fmt.copy();
            Supplier<PaginationList> fetch = () -> UserActions.listPlots(plotWorld.database(), title, uuid, format);
            Consumer<PaginationList> send = list -> list.sendTo(player);
            Plots.executor().async(fetch, send);
        }
    }
}
