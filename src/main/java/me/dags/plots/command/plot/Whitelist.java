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
import me.dags.plots.database.UserActions;
import me.dags.plots.database.WorldDatabase;
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
public class Whitelist {

    @Command(alias = "whitelist")
    @Permission(Permissions.PLOT_WHITELIST)
    @Description("List all whitelisted users")
    public void whitelist(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getPlot(player);
        if (plot.present()) {
            PlotId plotId = plot.second();
            WorldDatabase database = plot.first().database();
            Supplier<List<UUID>> search = () -> UserActions.getWhitelisted(database, plotId);
            Consumer<List<UUID>> whitelist = list(player, plotId);
            Plots.executor().async(search, whitelist);
        }
    }

    static Consumer<List<UUID>> list(Player player, PlotId plotId) {
        return list -> {
            FormattedListBuilder builder = FMT.listBuilder();
            builder.linesPerPage(9);
            builder.title().stress("Users Whitelisted On %s", plotId);

            list.stream()
                    .distinct()
                    .map(Sponge.getServiceManager().provideUnchecked(UserStorageService.class)::get)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(User::getName)
                    .forEach(name -> builder.line().info(" - %s", name));

            builder.build().sendTo(player);
        };
    }
}
