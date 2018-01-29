package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
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

    @Command("plot tp <plot>")
    @Permission(Permissions.PLOT_TP)
    @Description("Teleport to a plot")
    public void tp(@Src Player player, String plot) {
        tp(player, player.getWorld().getName(), plot);
    }

    @Command("plot tp <world> <plot>")
    @Permission(Permissions.PLOT_TP)
    @Description("Teleport to a plot")
    public void tp(@Src Player player, String world, String plot) {
        Optional<PlotWorld> plotWorld = Cmd.getWorld(player, world);
        if (plotWorld.isPresent()) {
            if (PlotId.isValid(plot)) {
                PlotId plotId = PlotId.parse(plot);
                Fmt.info("Teleporting to ").stress(plotId).tell(player);
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
                Fmt.info("Teleporting to ").stress(alias).tell(player);
                world.teleport(player, plotId);
            } else {
                Fmt.info("Could not find a plot with the alias ").stress(alias).info(" in this world").tell(player);
            }
        };
    }
}
