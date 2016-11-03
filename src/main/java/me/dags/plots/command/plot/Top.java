package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.database.WorldDatabase;
import me.dags.plots.plot.PlotWorld;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Top {

    @Command(aliases = "top", parent = "plot", desc = "List the most popular plots", perm = @Permission(Permissions.PLOT_TOP))
    public void top(@Caller Player player) {
        top(player, 10);
    }

    @Command(aliases = "top", parent = "plot", desc = "List the most popular plots", perm = @Permission(Permissions.PLOT_TOP))
    public void top(@Caller Player player, @One("size") int size) {
        Optional<PlotWorld> plotWorld = Cmd.getWorld(player);
        if (plotWorld.isPresent()) {
            WorldDatabase database = plotWorld.get().database();
            Supplier<PaginationList> get = () -> PlotActions.topPlots(database, size, Cmd.FMT());
            Consumer<PaginationList> top = list -> list.sendTo(player);
            Plots.executor().async(get, top);
        }
    }
}