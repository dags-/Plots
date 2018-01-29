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

    @Command("plot top")
    @Permission(Permissions.PLOT_TOP)
    @Description("List the most popular plots")
    public void top(@Src Player player) {
        top(player, 10);
    }

    @Command("plot top <size>")
    @Permission(Permissions.PLOT_TOP)
    @Description("List the most popular plots")
    public void top(@Src Player player, int size) {
        Optional<PlotWorld> plotWorld = Cmd.getWorld(player);
        if (plotWorld.isPresent()) {
            if (size < 1 || size > 100) {
                Fmt.error("Please specify a value between 1 & 100").tell(player);
                return;
            }
            WorldDatabase database = plotWorld.get().database();
            Format format = Fmt.copy();
            Supplier<PaginationList> get = () -> PlotActions.topPlots(database, size, format);
            Consumer<PaginationList> top = list -> list.sendTo(player);
            Plots.executor().async(get, top);
        }
    }
}
