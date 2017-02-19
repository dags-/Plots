package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.commandbus.format.Format;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.database.WorldDatabase;
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

    @Command(alias = "info", parent = "plot")
    @Permission(Permissions.PLOT_INFO)
    @Description("Get info about a plot")
    public void info(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            WorldDatabase database = plot.first().database();
            PlotId plotId = plot.second();
            Format format = FMT.copy();
            Supplier<Text> find = () -> PlotActions.plotInfo(database, plotId, format);
            Consumer<Text> info = player::sendMessage;
            Plots.executor().async(find, info);
        }
    }
}
