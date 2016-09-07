package me.dags.plots.commands;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.Format;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.database.Queries;
import me.dags.plots.database.statment.Insert;
import me.dags.plots.database.statment.Select;
import me.dags.plots.plot.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.biome.BiomeType;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class PlotCommands {

    private static final Format FORMAT = Plots.getConfig().getMessageFormat();

    @Command(aliases = "auto", parent = "plot", perm = Permissions.PLOT_AUTO)
    public void auto(@Caller Player player) {
        processPlotWorld(player, plotWorld -> {
            PlotId closest = plotWorld.getPlotId(player.getLocation().getBlockPosition());
            Plots.getDatabase().findFreePlot(plotWorld.getWorld(), closest, plotId -> {
                if (plotId.isPresent()) {
                    tp(player, plotWorld.getWorld(), plotId.toString());
                    claim(player, plotWorld, plotId);
                } else {
                    FORMAT.error("Unable to find a free plot. Sorry about that...").tell(player);
                }
            });
        });
    }

    @Command(aliases = "claim", parent = "plot", perm = Permissions.PLOT_CLAIM, desc = "Claim an empty plot to build on")
    public void claim(@Caller Player player) {
        processLocation(player, (plotWorld, plotId) -> claim(player, plotWorld, plotId));
    }

    @Command(aliases = "unclaim", parent = "plot", perm = Permissions.PLOT_UNCLAIM, desc = "Unclaim a plot and reset it")
    public void unclaim(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission(Permissions.PLOT_UNCLAIM_OTHER)) {
                FORMAT.warn("Unclaiming a plot will remove all whitelisted users including the owner!").append(Text.NEW_LINE)
                        .warn("To confirm, use either:").append(Text.NEW_LINE)
                        .stress(" /plot unclaim true").warn(" - to unclaim and reset the plot").append(Text.NEW_LINE)
                        .stress(" /plot unclaim false").warn(" - to unclaim and not reset the plot").tell(player);
            }
        }));
    }

    @Command(aliases = "unclaim", parent = "plot", perm = Permissions.PLOT_UNCLAIM)
    public void unclaim(@Caller Player player, @One("reset") boolean reset) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());

            if (!reset && !player.hasPermission(Permissions.PLOT_APPROVAL_BYPASS) && !plotUser.isApproved()) {
                FORMAT.error("Your plot will be reset if you wish to unclaim it. Use '")
                        .stress("/plot unclaim true")
                        .error("' if you wish to proceed")
                        .tell(player);
                return;
            }

            if (plotUser.isOwner(plotId) || player.hasPermission(Permissions.PLOT_UNCLAIM_OTHER)) {
                Plots.getDatabase().deletePlot(plotWorld.getWorld(), plotId, evicted -> {
                    if (evicted.size() > 0) {
                        FORMAT.info("Unclaimed plot ").stress(plotId).tell(player);
                        evicted.forEach(plotWorld::refreshUser);
                    } else {
                        FORMAT.error("Plot ").stress(plotId).error(" does not appear to have been claimed").tell(player);
                    }
                });
            } else {
                FORMAT.error("You do not own this plot").tell(player);
            }
        }));
    }

    @Command(aliases = "approve", parent = "plot", perm = Permissions.PLOT_APPROVE)
    public void approve(@Caller Player player) {
        processLocation(player, (plotWorld, plotId) -> {
            Select<Optional<User>> selectOwner = Queries.selectPlotOwner(plotWorld.getWorld(), plotId).build();
            Plots.getDatabase().select(selectOwner, user -> {
                if (user.isPresent()) {
                    Select<PlotUser> selectUser = Queries.selectUser(plotWorld.getWorld(), user.get().getUniqueId()).build();
                    Plots.getDatabase().select(selectUser, plotUser -> {
                        if (plotUser.isPresent()) {
                            PlotMeta updatedMeta = plotUser.getMeta(plotId).toBuilder().approved(true).build();
                            PlotUser updatedUser = plotUser.toBuilder().plot(plotId, updatedMeta).build();
                            Plots.getDatabase().saveUser(updatedUser);
                            plotWorld.refreshUser(plotUser.getUUID());
                            FORMAT.error("Successfully approved plot ").stress(plotId).tell(player);
                        } else {
                            FORMAT.error("Failed to approve plot ").stress(plotId).tell(player);
                        }
                    });
                } else {
                    FORMAT.error("Did not find an owner for plot ").stress(plotId).tell(player);
                }
            });
        });
    }


    @Command(aliases = "info", parent = "plot", perm = Permissions.PLOT_INFO)
    public void info(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            Select<Text> info = Queries.selectPlotInfo(plotWorld.getWorld(), plotId, FORMAT).build();
            Plots.getDatabase().select(info, message -> FORMAT.info("Plot: ").stress(plotId).append(message).tell(player));
        }));
    }

    @Command(aliases = "list", parent = "plot", perm = Permissions.PLOT_LIST)
    public void list(@Caller Player player) {
        processPlotWorld(player, plotWorld -> {
            PlotUser user = plotWorld.getUser(player.getUniqueId());
            user.listPlots().sendTo(player);
        });
    }

    @Command(aliases = "list", parent = "plot", perm = Permissions.PLOT_LIST_OTHER)
    public void list(@Caller Player player, @One("player") User target) {
        processPlotWorld(player, plotWorld -> {
            Select<PlotUser> select = Queries.selectUser(plotWorld.getWorld(), target.getUniqueId()).build();
            Plots.getDatabase().select(select, user -> {
                if (user.isPresent()) {
                    user.listPlots().sendTo(player);
                } else {
                    FORMAT.stress(target.getName()).error(" does not appear to own any plots in this world").tell(player);
                }
            });
        });
    }

    @Command(aliases = "tp", parent = "plot", perm = Permissions.PLOT_TP, desc = "Teleport to the given plotId/alias in your current world")
    public void tp(@Caller Player player, @One("plot | alias") String plot) {
        tp(player, player.getWorld().getName(), plot);
    }

    @Command(aliases = "tp", parent = "plot", perm = Permissions.PLOT_TP, desc = "Teleport to the given plotId/alias in the given world")
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



    @Command(aliases = "alias", parent = "plot", perm = Permissions.PLOT_ALIAS, desc = "Set an alias for the current plot")
    public void alias(@Caller Player player, @One("alias") String alias) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser user = plotWorld.getUser(player.getUniqueId());
            if (user.isWhitelisted(plotId)) {
                PlotMeta meta = user.getMeta(plotId).toBuilder().name(alias).build();
                Insert update = Queries.updateUserPlot(user, plotId, meta).build();
                Plots.getDatabase().update(update, success -> {
                    if (success == Tristate.TRUE) {
                        FORMAT.info("Set alias of ").stress(plotId).info(" to ").stress(alias).tell(player);
                        plotWorld.refreshUser(user.getUUID());
                    } else {
                        FORMAT.info("Unable to set alias ").stress(alias).info(" for ").stress(plotId).tell(player);
                    }
                });
            } else {
                FORMAT.error("You are not whitelisted on this plot!").tell(player);
            }
        }));
    }

    @Command(aliases = "biome", parent = "plot", perm = Permissions.PLOT_BIOME)
    public void biome(@Caller Player player, String biome) {
        processLocation(player, (((plotWorld, plotId) -> {
            PlotUser user = plotWorld.getUser(player.getUniqueId());
            if (user.isOwner(plotId)) {
                Optional<BiomeType> type = Sponge.getRegistry().getType(BiomeType.class, biome);
                if (type.isPresent()) {
                    plotWorld.setBiome(plotId, type.get());
                    FORMAT.info("Setting plot ").stress(plotId).info("'s biome to ").stress(type.get().getName()).tell(player);
                } else {
                    FORMAT.error("Biome ").stress(biome).error(" not recognised").tell(player);
                }
            } else {
                FORMAT.error("You don not own this plot").tell(player);
            }
        })));
    }

    @Command(aliases = "copyto", parent = "plot", perm = Permissions.PLOT_COPY, desc = "Copy the plot at your location to the given plot")
    public void copyTo(@Caller Player player, @One("plot | alias") String plot) {
        processLocation(player, ((plotWorld, fromId) -> {
            PlotUser user = plotWorld.getUser(player.getUniqueId());
            if (user.isWhitelisted(fromId)) {
                if (PlotId.isValid(plot)) {
                    PlotId toId = PlotId.valueOf(plot);
                    if (user.isOwner(toId)) {
                        FORMAT.info("Copying plot ").stress(fromId).info(" to ").stress(toId).tell(player);
                        plotWorld.copyPlot(fromId, toId);
                    } else {
                        FORMAT.error("You do not own the target plot ").stress(plot).tell(player);
                    }
                } else {
                    Select<PlotId> selectNamed = Queries.selectPlotByName(user.getUUID(), plotWorld.getWorld(), plot).build();
                    Plots.getDatabase().select(selectNamed, toId -> {
                        if (toId.isPresent()) {
                            FORMAT.info("Copying plot ").stress(fromId).info(" to ").stress(toId).tell(player);
                            plotWorld.copyPlot(fromId, toId);
                        } else {
                            FORMAT.error("Unable to locate plot ").stress(player).tell(player);
                        }
                    });
                }
            } else {
                FORMAT.error("You are not whitelisted on this plot").tell(player);
            }
        }));
    }

    @Command(aliases = {"maskall", "ma"}, parent = "plot", perm = Permissions.PLOT_MASKALL)
    public void maskall(@Caller Player player) {
        processPlotWorld(player, plotWorld -> {
            PlotUser user = plotWorld.getUser(player.getUniqueId());
            if (user.toggleMaskAll()) {
                FORMAT.info("MaskAll ").stress("enabled").tell(player);
                FORMAT.warn("MaskAll allows tools like WorldEdit to modify ").stress("ANYWHERE").warn(" in the world. Use with care").tell(player);
            } else {
                FORMAT.info("MaskAll ").stress("disabled").tell(player);
            }
        });
    }



    @Command(aliases = "add", parent = "plot", perm = Permissions.PLOT_ADD, desc = "Allow another player to build on your plot")
    public void add(@Caller Player player, @One("player") User user) {
        if (!user.hasPermission(Permissions.WHITELIST_RECIPIENT) && !player.hasPermission(Permissions.WHITELIST_ANY)) {
            FORMAT.error("Target user ").stress(user.getName()).error(" does not have permission to be added to a plot").tell(player);
            return;
        }

        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission(Permissions.WHITELIST_ANY)) {
                Select<PlotUser> select = Queries.selectUser(plotWorld.getWorld(), user.getUniqueId())
                        .andUpdate(target -> Queries.updateUserPlot(target, plotId, target.getMeta(plotId)).build())
                        .build();

                Plots.getDatabase().selectAndUpdate(select, result -> {
                    if (result == Tristate.TRUE) {
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

    @Command(aliases = "remove", parent = "plot", perm = Permissions.PLOT_REMOVE, desc = "Remove a whitelisted player from your plot")
    public void remove(@Caller Player player, @One("player") User user) {
        if (player.getUniqueId().equals(user.getUniqueId())) {
            FORMAT.error("You cannot remove yourself from your own whitelists!").tell(player);
            return;
        }

        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission(Permissions.PLOT_REMOVE_ANY)) {
                Select<PlotUser> select = Queries.selectUser(plotWorld.getWorld(), user.getUniqueId())
                        .andUpdate(target -> Queries.deleteWhitelisted(target, plotId).build())
                        .build();

                Plots.getDatabase().selectAndUpdate(select, result -> {
                    if (result == Tristate.TRUE) {
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



    @Command(aliases = "reset", parent = "plot", perm = Permissions.PLOT_RESET, desc = "Reset the entire plot to it's default state")
    public void reset(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            FORMAT.warn("Resetting a plot will delete everything inside it. Use '")
                    .stress("/plot reset true")
                    .warn("' if you wish to proceed").tell(player);
        }));
    }

    @Command(aliases = "reset", parent = "plot", perm = Permissions.PLOT_RESET)
    public void reset(@Caller Player player, @One("confirm") boolean confirm) {
        if (!confirm) {
            reset(player);
            return;
        }

        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            if (plotUser.isOwner(plotId) || player.hasPermission(Permissions.PLOT_RESET_OTHER)) {
                FORMAT.info("Resetting plot ").stress(plotId).tell(player);
                plotWorld.resetPlot(plotId);
            } else {
                FORMAT.error("You do not own this plot").tell(player);
            }
        }));
    }



    private void claim(Player player, PlotWorld plotWorld, PlotId plotId) {
        PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
        if (plotUser.hasPlot() && !player.hasPermission(Permissions.PLOT_APPROVAL_BYPASS) && !plotUser.isApproved()) {
            FORMAT.error("You must have one of your plots approved before claiming a new one").tell(player);
            return;
        }

        Select<Boolean> claim = Queries.isClaimed(plotWorld.getWorld(), plotId)
                .andUpdate(owned -> owned ? null : Queries.updateUserPlot(plotUser, plotId, PlotMeta.builder().owner(true).build()).build())
                .build();

        Plots.getDatabase().selectAndUpdate(claim, result -> {
            if (result == Tristate.TRUE) {
                FORMAT.info("Claimed plot ").stress(plotId).tell(player);
                plotWorld.refreshUser(plotUser.getUUID());
            } else if (result == Tristate.UNDEFINED) {
                FORMAT.error("Plot ").stress(plotId).error(" has already been claimed").tell(player);
            } else {
                FORMAT.error("Unable to claim plot ").stress(plotId).tell(player);
            }
        });
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

    private static void processPlotWorld(Player player, Consumer<PlotWorld> consumer) {
        Plots.getApi().getPlotWorld(player.getWorld().getName()).ifPresent(consumer);
    }
}
