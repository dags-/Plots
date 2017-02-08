package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.utils.Format;
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
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class Whitelist {

    @Command(aliases = "whitelist", parent = "plot", desc = "List all whitelisted users", perm = @Permission(Permissions.PLOT_WHITELIST))
    public void whitelist(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getPlot(player);
        if (plot.present()) {
            PlotId plotId = plot.second();
            WorldDatabase database = plot.first().database();
            Supplier<List<UUID>> search = () -> UserActions.getWhitelisted(database, plotId);
            Consumer<List<UUID>> whitelist = list(player, plotId, Cmd.FMT());
            Plots.executor().async(search, whitelist);
        }
    }

    static Consumer<List<UUID>> list(Player player, PlotId plotId, Format format) {
        return list -> {
            Iterable<Text> lines = list.stream()
                    .distinct()
                    .map(Sponge.getServiceManager().provideUnchecked(UserStorageService.class)::get)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(user -> format.info(" - {}", user.getName()).build())
                    .collect(Collectors.toList());

            PaginationList.builder()
                    .title(format.stress("Users Whitelisted On {}", plotId).build())
                    .linesPerPage(9)
                    .contents(lines)
                    .build()
                    .sendTo(player);
        };
    }
}
