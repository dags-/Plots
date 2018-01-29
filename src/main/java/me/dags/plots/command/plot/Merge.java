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
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Merge {

    @Command("plot merge")
    @Permission(Permissions.PLOT_MERGE)
    @Description("Merge all plots between the current one and <to>")
    public void merge(@Src Player player, String to) {
        if (!PlotId.isValid(to)) {
            Fmt.stress(to).error(" is not a valid plot id!").tell(player);
            return;
        }

        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            merge(player, plot.second().toString(), to);
        }
    }

    @Command("plot merge")
    @Permission(Permissions.PLOT_MERGE)
    @Description("Merge all plots between plots <from> and <to>")
    public void merge(@Src Player player, String from, String to) {
        if (!PlotId.isValid(from)) {
            Fmt.error("The <from> Plot ID: ").stress(from).error(" is not a valid ID").tell(player);
            return;
        }

        if (!PlotId.isValid(to)) {
            Fmt.error("The <to> Plot ID: ").stress(to).error(" is not a valid ID").tell(player);
            return;
        }

        Optional<PlotWorld> plotWorld = Cmd.getWorld(player);
        if (plotWorld.isPresent()) {
            PlotWorld world = plotWorld.get();
            PlotId fromPlot = PlotId.parse(from);
            PlotId toPlot = PlotId.parse(to);

            int minX = Math.min(toPlot.plotX(), fromPlot.plotX());
            int minZ = Math.min(toPlot.plotZ(), fromPlot.plotZ());
            int maxX = Math.max(toPlot.plotX(), fromPlot.plotX());
            int maxZ = Math.max(toPlot.plotZ(), fromPlot.plotZ());

            PlotUser user = world.user(player.getUniqueId());
            if (user.maxClaimCount() > -1) {
                int mergeSize = (maxX - minX) * (maxZ - minZ);
                int remaining = user.maxClaimCount() - user.plotCount();

                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (user.plotMask().contains(PlotId.of(x, z))) {
                            mergeSize--;
                        }
                    }
                }

                if (remaining < mergeSize) {
                    Fmt.error("You do not have enough remaining free plots to make a claim of this size").tell(player);
                    return;
                }
            }

            UUID owner = player.getUniqueId();
            PlotId min = PlotId.of(minX, minZ);
            PlotId max = PlotId.of(maxX, maxZ);
            WorldDatabase database = world.database();
            Format format = Fmt.copy();

            Supplier<Pair<Boolean, Text>> merge = () -> PlotActions.mergePlots(database, format, owner, min, max);
            Consumer<Pair<Boolean, Text>> result = merge(player, world, min);

            Plots.executor().async(merge, result);
        }
    }

    private static Consumer<Pair<Boolean, Text>> merge(Player player, PlotWorld world, PlotId min) {
        return result -> {
            if (result.first()) {
                Fmt.info("Merge Successful: ").append(result.second()).tell(player);
                world.refreshUser(player.getUniqueId());
                Walls.setWalls(player, world, min, Plots.config().getOwnedPlotWall(), 1);
            } else {
                Fmt.error("Merge failed: ").append(result.second()).tell(player);
            }
        };
    }
}
