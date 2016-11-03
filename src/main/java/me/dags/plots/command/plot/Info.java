package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Info {

    @Command(aliases = "info", parent = "plot", desc = "Get info about a plot", perm = @Permission(Permissions.PLOT_INFO))
    public void info(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            Supplier<Text> find = () -> PlotActions.plotInfo(plot.first().database(), plot.second(), Cmd.FMT());
            Consumer<Text> info = player::sendMessage;
            Plots.executor().async(find, info);
        }
    }
}