package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.commandbus.format.FormattedListBuilder;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Likers {

    @Command(alias = "likers", parent = "plot")
    @Permission(Permissions.PLOT_LIKERS)
    @Description("List users that 'like' a plot")
    public void likers(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Supplier<Optional<List<UUID>>> find = () -> PlotActions.findPlotLikes(world.database(), plotId);
            Consumer<Optional<List<UUID>>> likes = likers(player, plotId);
            Plots.executor().async(find, likes);
        }
    }

    static Consumer<Optional<List<UUID>>> likers(Player player, PlotId plotId) {
        return optional -> {
            if (optional.isPresent() && !optional.get().isEmpty()) {
                FormattedListBuilder list = FMT.listBuilder();

                list.linesPerPage(9);
                list.title().stress("Plot %s's Likes", plotId);
                optional.get().stream()
                        .map(Sponge.getServiceManager().provideUnchecked(UserStorageService.class)::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(User::getName)
                        .forEach(list.line().info(" - ")::stress);

                list.build().sendTo(player);
            } else {
                FMT.error("Plot ").stress(plotId).error(" doesn't have any likes").tell(player);
            }
        };
    }
}
