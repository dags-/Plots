package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.Format;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.database.Queries;
import me.dags.plots.database.statment.Select;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class PlotWorldListener {

    private final PlotWorld plotWorld;

    PlotWorldListener(PlotWorld plotWorld) {
        this.plotWorld = plotWorld;
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        World world = event.getTargetEntity().getWorld();
        if (plotWorld.thisWorld(world)) {
            Player player = event.getTargetEntity();
            Plots.getDatabase().loadUser(plotWorld.getWorld(), player.getUniqueId(), plotWorld::addUser);
            Task.Builder builder = Task.builder().execute(() -> {
                onPlayerMove(player, world.getSpawnLocation().getBlockPosition(), player.getLocation().getBlockPosition());
            });
            Plots.submitTask(builder);
        }
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        if (plotWorld.thisWorld(event.getTargetEntity().getWorld())) {
            PlotUser plotUser = plotWorld.getUser(event.getTargetEntity().getUniqueId());
            if (plotUser.isPresent()) {
                plotWorld.removeUser(plotUser);
            }
        }
    }

    @Listener(order = Order.PRE)
    public void onBlockChange(ChangeBlockEvent event) {
        if (plotWorld.thisWorld(event.getTargetWorld())) {
            // prevent blocks 'leaking' outside of a plot (trees growing, water flowing etc)
            event.filter(loc -> plotWorld.withinPlot(loc.getBlockPosition()));
        }
    }

    @Listener(order = Order.PRE)
    public void onExplosion(ExplosionEvent.Pre event) {
        if (plotWorld.thisWorld(event.getTargetWorld())) {
            // none of these
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.PRE)
    public void onInteractPrimary(InteractBlockEvent.Primary event, @First Player player) {
        if (event.getTargetBlock().getWorldUniqueId() == plotWorld.getWorldId()) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            if (!plotWorld.hasPermission(player, Permissions.ACTION_MODIFY) || !plotWorld.canEdit(player, event.getTargetBlock().getPosition())) {
                // no reason for players to be able to left-click blocks if not allowed to build here
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.PRE)
    public void onInteractSecondary(InteractBlockEvent.Secondary event, @First Player player) {
        if (event.getTargetBlock().getWorldUniqueId() == plotWorld.getWorldId()) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            if (!player.hasPermission(Permissions.ACTION_MODIFY) || !plotWorld.canEdit(player, event.getTargetBlock().getPosition())) {
                // don't allow use/placement of items/blocks, but still allow interactions with doors etc
                event.setUseItemResult(Tristate.FALSE);
            }
        }
    }

    // might not detect entities fired into the plot
    @Listener(order = Order.PRE)
    public void onInteractEntity(InteractEntityEvent event, @First Player player) {
        if (plotWorld.thisWorld(event.getTargetEntity().getWorld())) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            if (!plotWorld.hasPermission(player, Permissions.ACTION_INTERACT_ENTITY) || !plotWorld.canEdit(player, event.getTargetEntity().getLocation().getBlockPosition())) {
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onDamage(DamageEntityEvent event, @First EntityDamageSource damageSource) {
        if (plotWorld.thisWorld(event.getTargetEntity().getWorld())) {
            Entity source = damageSource.getSource(), cause = source;

            if (damageSource instanceof IndirectEntityDamageSource) {
                cause = ((IndirectEntityDamageSource) damageSource).getIndirectSource();
            }

            if (cause instanceof Player) {
                Player player = (Player) cause;
                if (plotWorld.hasPermission(player, Permissions.ACTION_BYPASS)) {
                    return;
                }
                if (!plotWorld.hasPermission(player, Permissions.ACTION_DAMAGE) || !plotWorld.canEdit(player, event.getTargetEntity().getLocation().getBlockPosition())) {
                    event.setCancelled(true);
                    if (source != cause) {
                        // damaging entity (source) has been spawned (or w/e) by player (cause); remove it from the world
                        source.remove();
                    }
                }
            } else {
                // some other entity has caused damage, don't want this.
                // doesn't remove the entity
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.PRE)
    public void onUse(UseItemStackEvent.Start event, @First Player player) {
        if (plotWorld.thisWorld(player.getWorld())) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            if (!plotWorld.hasPermission(player, Permissions.ACTION_USE) || !plotWorld.canEdit(player, player.getLocation().getBlockPosition())) {
                event.setCancelled(true);
            }
        }
    }

    // DropItemEvent is dumb
    @Listener(order = Order.PRE)
    public void drop(DropItemEvent event, @First EntitySpawnCause cause) {
        EntitySnapshot snapshot = cause.getEntity();
        if (plotWorld.getWorldId() == cause.getEntity().getWorldUniqueId()) {
            if (snapshot.getType() != EntityTypes.PLAYER) {
                return;
            }
            // wut
            snapshot.restore().map(Player.class::cast).ifPresent(player -> {
                if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                    return;
                }
                if (!plotWorld.hasPermission(player, Permissions.ACTION_DROP) || !plotWorld.canEdit(player, player.getLocation().getBlockPosition())) {
                    event.setCancelled(true);
                }
            });
        }
    }

    @Listener(order = Order.PRE)
    public void onSpawn(SpawnEntityEvent event, @First Player player) {
        if (plotWorld.thisWorld(event.getTargetWorld())) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            boolean spawnInanimate = plotWorld.hasPermission(player, Permissions.ACTION_SPAWN_INANIMATE);
            boolean spawnLiving = plotWorld.hasPermission(player, Permissions.ACTION_SPAWN_LIVING);
            event.filterEntities(entity -> {
                if (entity instanceof Living && !(entity instanceof ArmorStand)) {
                    return spawnLiving && plotWorld.canEdit(player, entity.getLocation().getBlockPosition());
                }
                return spawnInanimate && plotWorld.canEdit(player, entity.getLocation().getBlockPosition());
            });
        }
    }

    @Listener
    public void onTeleport(DisplaceEntityEvent.Teleport.TargetPlayer event) {
        World from = event.getFromTransform().getExtent();
        World to = event.getToTransform().getExtent();
        if (from != to) {
            if (plotWorld.thisWorld(from)) {
                Plots.log("Dropping plotUser: {}", event.getTargetEntity().getName());
                PlotUser plotUser = plotWorld.getUser(event.getTargetEntity().getUniqueId());
                plotWorld.removeUser(plotUser);
            } else if (plotWorld.thisWorld(to)) {
                Plots.log("Getting plotUser: {}", event.getTargetEntity().getName());
                Plots.getDatabase().loadUser(plotWorld.getWorld(), event.getTargetEntity().getUniqueId(), plotWorld::addUser);
            }
        }
    }

    @Listener
    public void onEntityMove(DisplaceEntityEvent.Move event) {
        if (plotWorld.thisWorld(event.getToTransform().getExtent())) {
            Vector3i from = event.getFromTransform().getLocation().getBlockPosition();
            Vector3i to = event.getToTransform().getLocation().getBlockPosition();
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                if (event.getTargetEntity() instanceof Player) {
                    onPlayerMove((Player) event.getTargetEntity(), from, to);
                } else {
                    onEntityMove(event, event.getTargetEntity(), from, to);
                }
            }
        }
    }

    private void onEntityMove(Cancellable event, Entity entity, Vector3i from, Vector3i to) {
        if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
            PlotId fromId = plotWorld.getPlotId(from);
            PlotBounds bounds = plotWorld.getPlotBounds(fromId);

            if (!bounds.contains(from)) {
                // entity is already outside of a plot so remove it
                entity.remove();
            } else if (!bounds.contains(to)) {
                // entity attempted to leave the bounds of a plot so prevent it
                event.setCancelled(true);
            }
        }
    }

    private void onPlayerMove(Player player, Vector3i from, Vector3i to) {
        PlotId fromId = plotWorld.getPlotId(from);
        if (plotWorld.getPlotBounds(fromId).contains(from)) {
            // player already inside a plot
            return;
        }
        PlotId toId = plotWorld.getPlotId(to);
        if (plotWorld.getPlotBounds(toId).contains(to)) {
            // player entered the bounds of a new plot
            Select<Optional<User>> owner = Queries.selectPlotOwner(plotWorld.getWorld(), toId).build();
            Plots.getDatabase().select(owner, user -> {
                Format.MessageBuilder message = PlotWorld.FORMAT.info("Plot: ").stress(toId);
                if (user.isPresent()) {
                    message.info(", Owner: ").stress(user.get().getName());
                }
                player.sendMessage(ChatTypes.ACTION_BAR, message.build());
            });
        }
    }
}