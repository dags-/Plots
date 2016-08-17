package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.Plots;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
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
    private final Map<UUID, PlotUser> plotUsers = new HashMap<>();
    private final Map<PlotId, PlotBounds> boundsCache = new HashMap<>();

    public PlotWorld(World world, PlotProvider plotProvider) {
        this.world = world.getName();
        this.worldId = world.getUniqueId();
        this.plotProvider = plotProvider;
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        if (thisWorld(event.getTargetEntity().getWorld())) {
            Plots.getDatabase().loadUser(world, event.getTargetEntity().getUniqueId(), this::addUser);
        }
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        PlotUser plotUser = getUser(event.getTargetEntity().getUniqueId());
        removeUser(plotUser);
    }

    @Listener (order = Order.PRE)
    public void onBlockChange(ChangeBlockEvent event, @First Player player) {
        if (thisWorld(event.getTargetWorld())) {
            event.filter(loc -> canBuild(player.getUniqueId(), loc.getBlockPosition()));
        }
    }

    @Listener (order = Order.PRE)
    public void onInteract(InteractEntityEvent event, @First Player player) {
        if (thisWorld(event.getTargetEntity().getWorld())) {
            if (!canBuild(player.getUniqueId(), event.getTargetEntity().getLocation().getBlockPosition())) {
                event.setCancelled(true);
            }
        }
    }

    @Listener (order = Order.PRE)
    public void onUse(UseItemStackEvent.Start event, @First Player player) {
        if (thisWorld(player.getWorld()) && !canBuild(player.getUniqueId(), player.getLocation().getBlockPosition())) {
            event.setCancelled(true);
        }
    }

    @Listener (order = Order.PRE)
    public void onSpawn(SpawnEntityEvent event, @First Player player) {
        if (thisWorld(event.getTargetWorld())) {
            event.filterEntityLocations(loc -> canBuild(player.getUniqueId(), loc.getBlockPosition()));
        }
    }

    @Listener
    public void onTeleport(DisplaceEntityEvent.Teleport.TargetPlayer event) {
        World from = event.getFromTransform().getExtent();
        World to = event.getToTransform().getExtent();
        if (from != to) {
            if (thisWorld(from)) {
                Plots.log("Dropping plotUser: {}", event.getTargetEntity().getName());
                PlotUser plotUser = getUser(event.getTargetEntity().getUniqueId());
                removeUser(plotUser);
            } else if (thisWorld(to)) {
                Plots.log("Getting plotUser: {}", event.getTargetEntity().getName());
                Plots.getDatabase().loadUser(world, event.getTargetEntity().getUniqueId(), this::addUser);
            }
        }
    }

    @Listener
    public void onMove(DisplaceEntityEvent.Move.TargetPlayer event) {
        if (thisWorld(event.getToTransform().getExtent())) {
            Vector3i from = event.getFromTransform().getPosition().toInt();
            Vector3i to = event.getToTransform().getPosition().toInt();
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                PlotId fromId = plotProvider.plotId(from);
                PlotId toId = plotProvider.plotId(to);
                if (fromId.equals(toId)) {
                    if (!getPlotBounds(fromId).contains(from) && getPlotBounds(toId).contains(to)) {
                        event.getTargetEntity().sendMessage(ChatTypes.ACTION_BAR, Text.of("Plot: ", toId));
                    }
                }
            }
        }
    }

    public void teleportToPlot(Player player, PlotId plotId) {
        Vector3i position = plotProvider.plotWarp(getPlotBounds(plotId));
        Location<World> location = new Location<>(player.getWorld(), position);
        player.setLocationAndRotation(location, player.getRotation());
    }

    public String getWorld() {
        return world;
    }

    public PlotId getPlotId(Vector3i vector3i) {
        return plotProvider.plotId(vector3i);
    }

    public PlotBounds getPlotBounds(PlotId plotId) {
        PlotBounds bounds = boundsCache.get(plotId);
        if (bounds == null) {
            boundsCache.put(plotId, bounds = plotProvider.plotBounds(plotId));
        }
        return bounds;
    }

    public PlotUser getUser(UUID uuid) {
        PlotUser user = plotUsers.get(uuid);
        return user != null ? user : PlotUser.EMPTY;
    }

    public boolean canBuild(UUID uuid, Vector3i vector3i) {
        PlotId plotId = plotProvider.plotId(vector3i);
        return getPlotBounds(plotId).contains(vector3i) && getUser(uuid).isWhitelisted(plotId);
    }

    public void updateUser(PlotUser user, PlotId plotId) {
        if (user.isPresent()) {
            addUser(user);
            Plots.getDatabase().updateUser(user, plotId);
        }
    }

    public void addUser(PlotUser user) {
        if (user.isPresent()) {
            plotUsers.put(user.getUUID(), user);
        }
    }

    public void removeUser(PlotUser user) {
        if (user.isPresent()) {
            plotUsers.remove(user.getUUID());
            for (Map.Entry<PlotId, PlotMeta> entry : user.getPlots()) {
                boundsCache.remove(entry.getKey());
            }
            Plots.getDatabase().saveUser(user);
        }
    }

    private boolean thisWorld(World world) {
        return world.getUniqueId() == worldId;
    }
}
