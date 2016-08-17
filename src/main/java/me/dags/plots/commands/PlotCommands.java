package me.dags.plots.commands;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.CommandBus;
import me.dags.commandbus.Format;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.plots.Plots;
import me.dags.plots.database.Queries;
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

    @Command(aliases = "info", parent = "plotTransformer", perm = "plotTransformer.command.info")
    public void info(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            format.info("Plot: ").stress(plotId).tell(player);
        }));
    }

    @Command(aliases = "claim", parent = "plotTransformer", perm = "plotTransformer.command.claim")
    public void claim(@Caller Player player) {
        processLocation(player, (plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            PlotUser updated = plotUser.toBuilder().plot(plotId, PlotMeta.builder().owner(true).build()).build();
            plotWorld.updateUser(updated, plotId);
            format.info("Claimed plotTransformer ").stress(plotId).tell(player);
        });
    }

    @Command(aliases = "unclaim", parent = "plotTransformer", perm = "plotTransformer.command.claim")
    public void unclaim(@Caller Player player) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser plotUser = plotWorld.getUser(player.getUniqueId());
            PlotUser updated = plotUser.toBuilder().removePlot(plotId).build();
            plotWorld.updateUser(updated, plotId);
            format.info("Unclaimed plotTransformer ").stress(plotId).tell(player);
        }));
    }

    @Command(aliases = "name", parent = "plotTransformer", perm = "plotTransformer.command.name")
    public void setName(@Caller Player player, @One("name") String name) {
        processLocation(player, ((plotWorld, plotId) -> {
            PlotUser user = plotWorld.getUser(player.getUniqueId());
            if (user.isWhitelisted(plotId)) {
                PlotMeta meta = user.getMeta(plotId).toBuilder().name(name).build();
                PlotUser updated = user.toBuilder().plot(plotId, meta).build();
                plotWorld.updateUser(updated, plotId);
                format.info("Set custom name of ").stress(plotId).info(" to ").stress(name).tell(player);
            } else {
                format.error("You are not whitelisted on this plotTransformer!").tell(player);
            }
        }));
    }

    @Command(aliases = "warp", parent = "plotTransformer", perm = "plotTransformer.command.warp")
    public void warp(@Caller Player player, @One("name") String name) {
        Select<PlotId> select = Queries.selectPlotByName(player.getUniqueId(), player.getWorld().getName(), name).build();
        Plots.getDatabase().select(select, plotId -> {
            if (plotId.isPresent()) {
                processPlotWorld(player, plotWorld -> plotWorld.teleportToPlot(player, plotId));
                format.info("Teleporting to ").stress(name).stress( "(" + plotId + ")").tell(player);
            } else {
                format.error("Unable to find plotTransformer ").stress(name).tell(player);
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
                format.error("You are not inside a plotTransformer!").tell(player);
            }
        });
    }
}
