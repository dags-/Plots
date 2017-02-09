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
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Merge {

    @Command(aliases = "merge", parent = "plot", desc = "Merge multiple plots into one", perm = @Permission(Permissions.PLOT_MERGE))
    public void merge(@Caller Player player, @One("to") String to) {
        if (!PlotId.isValid(to)) {
            Cmd.FMT().stress(to).error(" is not a valid plot id!").tell(player);
            return;
        }

        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotId fromPlot = plot.second();
            PlotId toPlot = PlotId.parse(to);

            int minX = Math.min(toPlot.plotX(), fromPlot.plotX());
            int minZ = Math.min(toPlot.plotZ(), fromPlot.plotZ());
            int maxX = Math.max(toPlot.plotX(), fromPlot.plotX());
            int maxZ = Math.max(toPlot.plotZ(), fromPlot.plotZ());

            PlotWorld world = plot.first();
            UUID owner = player.getUniqueId();
            PlotId min = PlotId.of(minX, minZ);
            PlotId max = PlotId.of(maxX, maxZ);
            WorldDatabase database = world.database();

            Supplier<Pair<Boolean, Text>> merge = () -> PlotActions.mergePlots(database, Cmd.FMTCopy(), owner, min, max);
            Consumer<Pair<Boolean, Text>> result = merge(player, world, min);

            Plots.executor().async(merge, result);
        }
    }

    private static Consumer<Pair<Boolean, Text>> merge(Player player, PlotWorld world, PlotId min) {
        return result -> {
            if (result.first()) {
                Cmd.FMT().info("Merge Successful: ").append(result.second()).tell(player);
                world.refreshUser(player.getUniqueId());
                Walls.setWalls(player, world, min, Plots.config().getOwnedPlotWall(), 1);
            } else {
                Cmd.FMT().error("Merge failed: ").append(result.second()).tell(player);
            }
        };
    }
}
