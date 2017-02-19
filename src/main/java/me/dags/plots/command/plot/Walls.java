package me.dags.plots.command.plot;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.*;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.operation.WallsOperation;
import me.dags.plots.plot.*;
import me.dags.plots.util.Pair;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.extent.MutableBlockVolume;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Walls {

    @Command(alias = "walls", parent = "plot")
    @Permission(Permissions.PLOT_WALLS)
    @Description("Change the wall material around the plot")
    public void walls(@Caller Player player) {
        walls(player, 1);
    }

    @Command(alias = "walls", parent = "plot")
    @Permission(Permissions.PLOT_WALLS)
    @Description("Change the wall material around the plot")
    public void walls(@Caller Player player, @One("depth") int depth) {
        Optional<BlockState> blockState = player.getItemInHand(HandTypes.MAIN_HAND)
                .flatMap(itemStack -> itemStack.get(Keys.ITEM_BLOCKSTATE));

        if (!blockState.isPresent()) {
            FMT.error("Cannot set your held item as the wall material").tell(player);
            return;
        }

        BlockState state = blockState.get();
        Pair<PlotWorld, PlotId> plot = Cmd.getPlot(player);
        if (plot.present()) {
            setWalls(player, plot.first(), plot.second(), state, depth);
        }
    }

    static void setWalls(Player player, PlotWorld world, PlotId plotId, BlockState state, int depth) {
        PlotUser user = world.user(player.getUniqueId());
        if (user.plotMask().contains(plotId)) {
            FMT.info("Setting wall material to ").stress(state).tell(player);
            PlotSchema schema = world.plotSchema();
            PlotBounds bounds = user.plotMask().plots().get(plotId);
            Vector3i min = bounds.getBlockMin().sub(schema.wallWidth(), 0, schema.wallWidth());
            Vector3i max = bounds.getBlockMax().add(schema.wallWidth(), 0, schema.wallWidth());
            MutableBlockVolume volume = player.getWorld().getBlockView(min, max);
            WallsOperation operation = new WallsOperation(world.world(), volume, depth, schema, state);
            operation.onComplete(() -> FMT.info("Finished setting wall material for ").stress(plotId).tell(player));
            Plots.core().dispatcher().queueOperation(operation);
        }
    }
}
