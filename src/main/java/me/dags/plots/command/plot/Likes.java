package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.utils.Format;
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
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Likes {

    @Command(aliases = "likes", parent = "plot", desc = "List the likers of a plot", perm = @Permission(Permissions.PLOT_LIKES))
    public void likes(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Supplier<Optional<List<UUID>>> find = () -> PlotActions.findPlotLikes(world.database(), plotId);
            Consumer<Optional<List<UUID>>> likes = likes(player, plotId);
            Plots.executor().async(find, likes);
        }
    }

    static Consumer<Optional<List<UUID>>> likes(Player player, PlotId plotId) {
        return optional -> {
            if (optional.isPresent() && !optional.get().isEmpty()) {
                Format.MessageBuilder builder = Cmd.FMT.info("Plot ").stress(plotId).info("'s likes:");
                optional.get().stream()
                        .map(Sponge.getServiceManager().provideUnchecked(UserStorageService.class)::get)
                        .forEach(user -> user
                                .map(User::getName)
                                .ifPresent(builder.append(Text.NEW_LINE).info(" - ")::stress));
                builder.tell(player);
            } else {
                Cmd.FMT.error("Plot ").stress(plotId).error(" doesn't have any likes").tell(player);
            }
        };
    }
}
