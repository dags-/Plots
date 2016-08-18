package me.dags.plots.commands;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.Format;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.plots.Plots;
import me.dags.plots.database.Queries;
import me.dags.plots.database.statment.Delete;
import me.dags.plots.database.statment.Insert;
import me.dags.plots.database.statment.Select;
import me.dags.plots.plot.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class PlotCommands {

    private static final Format FORMAT = Plots.getConfig().getMessageFormat();

    @Command(aliases = "info", parent = "plot", perm = "plot.command.info")
    public void info(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            Select<Text> info = Queries.selectPlotInfo(plotWorld.getWorld(), plotId, FORMAT).build();
            Plots.getDatabase().select(info, message -> {
                FORMAT.info("Plot: ").stress(plotId).append(message).tell(player);
            });
        }));
    }

    @Command(aliases = "claim", parent = "plot", perm = "plot.command.claim", desc = "Claim an empty plot to build on")
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
                    FORMAT.info("Claimed plot ").stress(plotId).tell(player);
                } else {
                    FORMAT.error("This plot has already been claimed").tell(player);
                }
            });
        });
    }

    @Command(aliases = "add", parent = "plot", perm = "plot.command.whitelist.add", desc = "Allow another player to build on your plot")
    public void add(@Caller Player player, @One("player") User user) {
        if (player.getUniqueId().equals(user.getUniqueId())) {
            FORMAT.error("You cannot add yourself to your own whitelists!").tell(player);
            return;
        }

        processLocation(player, ((plotWorld, plotId) -> {

            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission("plot.command.whitelist.force.add")) {
                PlotUser other = plotWorld.getUser(user.getUniqueId());
                if (!other.isPresent()) {
                    other = PlotUser.builder()
                            .world(plotWorld.getWorld())
                            .uuid(user.getUniqueId())
                            .plot(plotId, PlotMeta.EMPTY)
                            .build();
                }

                Insert insert = Queries.updateUserPlot(other, plotId, other.getMeta(plotId)).build();
                Plots.getDatabase().update(insert, result -> {
                    if (result) {
                        FORMAT.info("User ").stress(user.getName()).info(" is now whitelisted on your plot!").tell(player);
                        user.getPlayer().ifPresent(FORMAT.stress(player.getName()).info(" added you to their plot ").stress(plotId)::tell);
                        plotWorld.refreshUser(user.getUniqueId());
                    } else {
                        FORMAT.error("Unable to add ").stress(user.getName()).info(" to plot ").stress(plotId).tell(player);
                    }
                });

            } else {
                FORMAT.error("You do not own this plot!").tell(player);
            }
        }));
    }

    @Command(aliases = "remove", parent = "plot", perm = "plot.command.whitelist.remove", desc = "Remove a whitelisted player from your plot")
    public void remove(@Caller Player player, @One("player") User user) {
        if (player.getUniqueId().equals(user.getUniqueId())) {
            FORMAT.error("You cannot remove yourself from your own whitelists!").tell(player);
            return;
        }

        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission("plot.command.whitelist.force.remove")) {
                PlotUser other = plotWorld.getUser(user.getUniqueId())
                        .toBuilder()
                        .uuid(user.getUniqueId())
                        .world(plotWorld.getWorld())
                        .build();

                Delete delete = Queries.deleteUserPlot(other, plotId).build();
                Plots.getDatabase().update(delete, result -> {
                    if (result) {
                        FORMAT.info("Successfully removed ").stress(user.getName()).info(" from plot ").stress(plotId).tell(player);
                        user.getPlayer().ifPresent(FORMAT.subdued("You have been removed from plot ").stress(plotId)::tell);
                        plotWorld.refreshUser(user.getUniqueId());
                    } else {
                        FORMAT.error("Unable to remove ").stress(user.getName()).info(" from plot ").stress(plotId).tell(player);
                    }
                });
            } else {
                FORMAT.error("You do not own this plot!").tell(player);
            }
        }));
    }

    @Command(aliases = "unclaim", parent = "plot", perm = "plot.command.unclaim.self", desc = "Unclaim a plot and reset it")
    public void unclaim(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission("plot.command.unclaim.other")) {
                FORMAT.warn("Unclaiming a plot will delete everything inside it. Use '")
                        .stress("/plot unclaim true")
                        .warn("' if you wish to proceed").tell(player);
            }
        }));
    }

    @Command(aliases = "unclaim", parent = "plot", perm = "plot.command.unclaim.self")
    public void unclaim(@Caller Player player, @One("confirm") boolean confirm) {
        if (!confirm) {
            unclaim(player);
            return;
        }
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission("plot.command.unclaim.other")) {
                Plots.getDatabase().deletePlot(plotWorld.getWorld(), plotId, evicted -> {
                    if (evicted.size() > 0) {
                        FORMAT.info("Unclaimed plot ").stress(plotId).tell(player);
                        evicted.forEach(plotWorld::refreshUser);
                    } else {
                        FORMAT.error("Plot ").stress(plotId).error(" does not appear to have been claimed").tell(player);
                    }
                });
                plotWorld.resetPlot(plotId);
            } else {
                FORMAT.error("You do not own this plot").tell(player);
            }
        }));
    }

    @Command(aliases = "reset", parent = "plot", perm = "plot.command.reset.self", desc = "Reset the entire plot to it's default state")
    public void reset(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission("plot.command.unclaim.other")) {
                FORMAT.warn("Resetting a plot will delete everything inside it. Use '")
                        .stress("/plot reset true")
                        .warn("' if you wish to proceed").tell(player);
            }
        }));
    }

    @Command(aliases = "reset", parent = "plot", perm = "plot.command.reset.self")
    public void reset(@Caller Player player, @One("confirm") boolean confirm) {
        if (!confirm) {
            reset(player);
            return;
        }

        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission("plot.command.reset.other")) {
                FORMAT.info("Resetting plot ").stress(plotId).info("...").tell(player);
                plotWorld.resetPlot(plotId);
            } else {
                FORMAT.error("You do not own this plot").tell(player);
            }
        }));
    }

    @Command(aliases = "alias", parent = "plot", perm = "plot.command.name", desc = "Set an alias for the current plot")
    public void alias(@Caller Player player, @One("alias") String alias) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser user = plotWorld.getUser(player.getUniqueId());
            if (user.isWhitelisted(plotId)) {
                PlotMeta meta = user.getMeta(plotId).toBuilder().name(alias).build();
                PlotUser updated = user.toBuilder()
                        .world(plotWorld.getWorld())
                        .uuid(player.getUniqueId())
                        .plot(plotId, meta)
                        .build();
                plotWorld.updateUser(updated, plotId);
                FORMAT.info("Set custom name of ").stress(plotId).info(" to ").stress(alias).tell(player);
            } else {
                FORMAT.error("You are not whitelisted on this plot!").tell(player);
            }
        }));
    }

    @Command(aliases = "tp", parent = "plot", perm = "plot.command.tp", desc = "Teleport to the given plotId/alias in your current world")
    public void tp(@Caller Player player, @One("plot|alias") String plot) {
        tp(player, player.getWorld().getName(), plot);
    }

    @Command(aliases = "tp", parent = "plot", perm = "plot.command.tp", desc = "Teleport to the given plotId/alias in the given world")
    public void tp(@Caller Player player, @One("world") String world, @One("plot|alias") String plot) {
        Optional<PlotWorld> optional = Plots.getApi().getPlotWorld(world);
        if (optional.isPresent()) {
            final PlotWorld plotWorld = optional.get();
            if (PlotId.isValid(plot)) {
                PlotId plotId = PlotId.valueOf(plot);
                plotWorld.teleportToPlot(player, plotId);
            } else {
                Select<PlotId> select = Queries.selectPlotByName(player.getUniqueId(), world, plot).build();
                Plots.getDatabase().select(select, plotId -> {
                    if (plotId.isPresent()) {
                        plotWorld.teleportToPlot(player, plotId);
                    } else {
                        FORMAT.error("Unable to find plot ").stress(plot).tell(player);
                    }
                });
            }
        } else {
            FORMAT.error("Target world ").stress(world).error(" is not recognised as a PlotWorld!").tell(player);
        }
    }

    private static void processPlotWorld(Player player, Consumer<PlotWorld> consumer) {
        Plots.getApi().getPlotWorld(player.getWorld().getName()).ifPresent(consumer);
    }

    private void processLocation(Player player, BiConsumer<PlotWorld, PlotId> onSuccess) {
        processPlotWorld(player, plotWorld -> {
            Vector3i position = player.getLocation().getBlockPosition();
            PlotId plotId = plotWorld.getPlotId(position);
            PlotBounds bounds = plotWorld.getPlotBounds(plotId);
            if (bounds.contains(position)) {
                onSuccess.accept(plotWorld, plotId);
            } else {
                FORMAT.error("You are not inside a plot!").tell(player);
            }
        });
    }
}
