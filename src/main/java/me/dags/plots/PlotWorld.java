package me.dags.plots;

import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.plot.Plot;
import me.dags.plots.plot.PlotBounds;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotProvider;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class PlotWorld {

    private final String world;
    private final UUID worldId;
    private final PlotProvider plotProvider;
    private final Map<PlotId, Plot> plots = new HashMap<>();

    public PlotWorld(World world, PlotProvider plotProvider) {
        this.world = world.getName();
        this.worldId = world.getUniqueId();
        this.plotProvider = plotProvider;
    }

    public String getWorld() {
        return world;
    }

    public PlotId getPlotId(Vector3i vector3i) {
        return plotProvider.plotId(vector3i);
    }

    public PlotBounds getPlotBounds(PlotId plotId) {
        return plotProvider.plotBounds(plotId);
    }

    private boolean canBuild(UUID uuid, Vector3i vector3i) {
        PlotId plotId = plotProvider.plotId(vector3i);
        Plot plot = plots.get(plotId);
        return plot != null && plot.getBounds().contains(vector3i) && plot.isWhitelisted(uuid);
    }

    private boolean thisWorld(World world) {
        return world.getUniqueId() == worldId;
    }

    @Listener (order = Order.PRE)
    public void onChangeBlock(ChangeBlockEvent.Pre event, @Root Player player) {
        if (thisWorld(event.getTargetWorld())) {
            for (Location<World> location : event.getLocations()) {
                if (!canBuild(player.getUniqueId(), location.getBlockPosition())) {
                    event.setCancelled(true);
                    System.out.println("CHANGE.PRE");
                    return;
                }
            }
        }
    }

    @Listener (order = Order.AFTER_PRE)
    public void onInteract(InteractEvent event, @Root Player player) {
        if (thisWorld(player.getWorld())) {
            event.getInteractionPoint().ifPresent(position -> {
                if (!canBuild(player.getUniqueId(), position.toInt())) {
                    event.setCancelled(true);
                    System.out.println("INTERACT");
                }
            });
        }
    }
}
