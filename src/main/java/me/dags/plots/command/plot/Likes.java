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
import me.dags.plots.database.WorldDatabase;
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
public class Likes {

    @Permission(Permissions.PLOT_LIKES)
    @Description("List the plots that you 'like'")
    @Command("plot likes")
    public void likes(@Src Player player) {
        likes(player, player);
    }

    @Permission(Permissions.PLOT_LIKES_OTHER)
    @Description("List the plots that <player> 'liked'")
    @Command("plot likes <user>")
    public void likes(@Src Player player, User other) {
        Optional<PlotWorld> world = Cmd.getWorld(player);
        if (world.isPresent()) {
            String name = other.getName();
            UUID uuid = other.getUniqueId();
            WorldDatabase database = world.get().database();
            Format format = Fmt.copy();
            Supplier<PaginationList> search = () -> UserActions.listLikes(database, name, uuid, format);
            Consumer<PaginationList> likes = list -> list.sendTo(player);
            Plots.executor().async(search, likes);
        }
    }
}
