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
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Auto {

    @Command(alias = "auto", parent = "plot")
    @Permission(Permissions.PLOT_AUTO)
    @Description("Auto-claim the nearest plot")
    public void auto(@Caller Player player) {
        Pair<PlotWorld, PlotId> plot = Cmd.getPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Supplier<PlotId> find = () -> PlotActions.findNextFreePlot(world.database(), plotId);
            Consumer<PlotId> claim = claim(player, world);
            Plots.executor().async(find, claim);
        }
    }

    static Consumer<PlotId> claim(Player player, PlotWorld world) {
        return plotId -> {
            boolean result = Claim.claim(player, world, plotId);
            if (result) {
                world.teleport(player, plotId);
                FMT.info("Teleported to plot ").stress(plotId).tell(player);
            }
        };
    }
}
