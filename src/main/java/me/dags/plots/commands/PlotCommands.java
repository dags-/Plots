package me.dags.plots.commands;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.CommandBus;
import me.dags.commandbus.Format;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.plots.Plots;
import me.dags.plots.database.Queries;
import me.dags.plots.database.statment.Delete;
import me.dags.plots.database.statment.Select;
import me.dags.plots.plot.*;
import org.spongepowered.api.entity.living.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class PlotCommands {

    private static final Format format = CommandBus.newFormatBuilder().build();

    @Command(aliases = "info", parent = "plot", perm = "plot.command.info")
    public void info(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            format.info("Plot: ").stress(plotId).tell(player);
        }));
    }

    @Command(aliases = "claim", parent = "plot", perm = "plot.command.claim")
    public void claim(@Caller Player player) {
        processLocation(player, (plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            Select<Boolean> claimed = Queries.isClaimed(plotWorld.getWorld(), plotId).build();
            Plots.getDatabase().select(claimed, result -> {
                if (!result) {
                    PlotUser updated = plotUser.toBuilder()
                            .world(plotWorld.getWorld())
                            .uuid(player.getUniqueId())
                            .plot(plotId, PlotMeta.builder().owner(true).build())
                            .build();

                    plotWorld.updateUser(updated, plotId);
                    format.info("Claimed plot ").stress(plotId).tell(player);
                } else {
                    format.error("This plot has already been claimed").tell(player);
                }
            });
        });
    }

    @Command(aliases = "unclaim", parent = "plot", perm = "plot.command.unclaim.self")
    public void unclaim(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId)) {
                PlotUser updated = plotUser.toBuilder().removePlot(plotId).build();
                plotWorld.updateUser(updated, plotId);
                plotWorld.resetPlot(plotId);
                format.info("Unclaimed plot ").stress(plotId).tell(player);
            } else if (player.hasPermission("plot.command.unclaim.other")){
                Delete delete = Queries.deletePlot(plotWorld.getWorld(), plotId).build();
                Plots.getDatabase().update(delete, result -> {
                    if (result) {
                        format.info("Unclaimed plot ").stress(plotId).tell(player);
                        plotWorld.resetPlot(plotId);
                    } else {
                        format.error("Unable to un-claim plot ").stress(plotId).tell(player);
                    }
                });
            } else {
                format.error("You do not own this plot").tell(player);
            }
        }));
    }

    @Command(aliases = "reset", parent = "plot", perm = "plot.command.reset.self")
    public void reset(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission("plot.command.reset.other")) {
                format.info("Resetting plot ").stress(plotId).info("...").tell(player);
                plotWorld.resetPlot(plotId);
            } else {
                format.error("You do not own this plot").tell(player);
            }
        }));
    }

    @Command(aliases = "name", parent = "plot", perm = "plot.command.name")
    public void setName(@Caller Player player, @One("name") String name) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser user = plotWorld.getUser(player.getUniqueId());
            if (user.isWhitelisted(plotId)) {
                PlotMeta meta = user.getMeta(plotId).toBuilder().name(name).build();
                PlotUser updated = user.toBuilder()
                        .world(plotWorld.getWorld())
                        .uuid(player.getUniqueId())
                        .plot(plotId, meta)
                        .build();
                plotWorld.updateUser(updated, plotId);
                format.info("Set custom name of ").stress(plotId).info(" to ").stress(name).tell(player);
            } else {
                format.error("You are not whitelisted on this plot!").tell(player);
            }
        }));
    }

    @Command(aliases = "warp", parent = "plot", perm = "plot.command.warp")
    public void warp(@Caller Player player, @One("name") String name) {
        Select<PlotId> select = Queries.selectPlotByName(player.getUniqueId(), player.getWorld().getName(), name).build();
        Plots.getDatabase().select(select, plotId -> {
            if (plotId.isPresent()) {
                processPlotWorld(player, plotWorld -> plotWorld.teleportToPlot(player, plotId));
                format.info("Teleporting to ").stress(name).stress( " [" + plotId + "]").tell(player);
            } else {
                format.error("Unable to find plot ").stress(name).tell(player);
            }
        });
    }

    private static void processPlotWorld(Player player, Consumer<PlotWorld> consumer) {
        PlotWorld plotWorld = Plots.getApi().getPlotWorld(player.getWorld().getName());
        if (plotWorld == null) {
            return;
        }
        consumer.accept(plotWorld);
    }

    private static void processLocation(Player player, BiConsumer<PlotWorld, PlotId> onSuccess) {
        processPlotWorld(player, plotWorld -> {
            Vector3i position = player.getLocation().getBlockPosition();
            PlotId plotId = plotWorld.getPlotId(position);
            PlotBounds bounds = plotWorld.getPlotBounds(plotId);
            if (bounds.contains(position)) {
                onSuccess.accept(plotWorld, plotId);
            } else {
                format.error("You are not inside a plot!").tell(player);
            }
        });
    }
}
