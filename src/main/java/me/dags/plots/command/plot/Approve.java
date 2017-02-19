package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.database.UserActions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Approve {

    @Command(alias = "approve", parent = "plot")
    @Permission(Permissions.PLOT_APPROVE)
    @Description("Approve a user's plot")
    public void approve(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Supplier<Optional<UUID>> owner = () -> PlotActions.findPlotOwner(world.database(), plotId);
            Consumer<Optional<UUID>> approve = approve(player, world, plotId);
            Plots.executor().async(owner, approve);
        }
    }

    static Consumer<Optional<UUID>> approve(Player player, PlotWorld plotWorld, PlotId plotId) {
        return uuid -> {
            if (!uuid.isPresent()) {
                FMT.error("Plot ").stress(plotId).error(" is not owned").tell(player);
            } else {
                approve(player, plotWorld, plotId, uuid.get());
            }
        };
    }

    static void approve(Player player, PlotWorld plotWorld, PlotId plotId, UUID uuid) {
        FMT.info("Approving plot ").stress(plotId).info("...").tell(player);

        Runnable async = () -> UserActions.setApproved(plotWorld.database(), uuid, true);
        Runnable callback = () -> {
            Optional<User> user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid);
            if (user.isPresent()) {
                FMT.info("Approved ").stress(user.get().getName()).tell(player);
                user.get().getPlayer().ifPresent(FMT.info("Your plot ").stress(plotId).info(" has been approved")::tell);
                plotWorld.refreshUser(uuid);
            }
        };

        Plots.executor().async(async, callback);
    }
}
