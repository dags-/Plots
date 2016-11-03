package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Teleport {

    @Command(aliases = "tp", parent = "plot", desc = "Teleport to a plot", perm = @Permission(Permissions.PLOT_TP))
    public void tp(@Caller Player player, @One("plotId | alias") String plot) {
        tp(player, player.getWorld().getName(), plot);
    }

    @Command(aliases = "tp", parent = "plot", desc = "Teleport to a plot", perm = @Permission(Permissions.PLOT_TP))
    public void tp(@Caller Player player, @One("world") String world, @One("plotId | alias") String plot) {
        Optional<PlotWorld> plotWorld = Cmd.getWorld(player, world);
        if (plotWorld.isPresent()) {
            if (PlotId.isValid(plot)) {
                PlotId plotId = PlotId.parse(plot);
                Cmd.FMT().info("Teleporting to ").stress(plotId).tell(player);
                plotWorld.get().teleport(player, plotId);
            } else {
                Supplier<PlotId> findPlot = () -> PlotActions.plotFromAlias(plotWorld.get().database(), plot);
                Consumer<PlotId> teleport = teleport(player, plotWorld.get(), plot);
                Plots.executor().async(findPlot, teleport);
            }
        }
    }

    static Consumer<PlotId> teleport(Player player, PlotWorld world, String alias) {
        return plotId -> {
            if (plotId.present()) {
                Cmd.FMT().info("Teleporting to ").stress(alias).tell(player);
                world.teleport(player, plotId);
            } else {
                Cmd.FMT().info("Could not find a plot with the alias ").stress(alias).info(" in this world").tell(player);
            }
        };
    }
}
