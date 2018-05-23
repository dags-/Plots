package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Format;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.database.PlotActions;
import me.dags.plots.database.WorldDatabase;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.weather.Weathers;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class PlotWorldListener {

    private final PlotWorld plotWorld;

    PlotWorldListener(PlotWorld plotWorld) {
        this.plotWorld = plotWorld;
    }

    private PlotMask getMask(Player player) {
        return plotWorld.user(player.getUniqueId()).plotMask();
    }

    private boolean canEdit(Player player, Vector3i position) {
        return getMask(player).contains(position);
    }

    @Listener(order = Order.PRE)
    public void onWeatherChange(ChangeWorldWeatherEvent event) {
        if (event.getCause().root() instanceof Player) {
            return;
        }

        if (plotWorld.equalsWorld(event.getTargetWorld()) && event.getWeather() != Weathers.CLEAR) {
            event.getTargetWorld().setWeather(Weathers.CLEAR);
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.PRE)
    public void onNotify(NotifyNeighborBlockEvent event, @Root BlockSnapshot root) {
        if (!plotWorld.equalsWorld(root.getWorldUniqueId())) {
            return;
        }

        if (root.supports(Keys.EXTENDED)) {
            Vector3i position = root.getPosition();
            boolean cancel = event.getNeighbors().keySet().stream()
                    .map(direction -> position.add(direction.asBlockOffset()))
                    .anyMatch(pos -> !plotWorld.plotSchema().containingPlot(pos).present());

            if (cancel) {
                event.setCancelled(true);
                root.getLocation().ifPresent(location -> location.setBlockType(BlockTypes.AIR));
            }
        } else if (root.supports(Keys.POWER) || root.supports(Keys.POWERED)) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.AFTER_PRE)
    public void onBlockPlace(ChangeBlockEvent.Place event) {
        filter(event);
    }

    @Listener(order = Order.AFTER_PRE)
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        filter(event);

    }

    @Listener(order = Order.AFTER_PRE)
    public void onBlockModify(ChangeBlockEvent.Modify event) {
        filter(event);
    }

    private void filter(ChangeBlockEvent event) {
        if (event.getSource() instanceof Player) {
            return;
        }
        event.filter(l -> !plotWorld.equalsWorld(l.getExtent()) || !plotWorld.plotSchema().containingPlot(l.getBiomePosition()).present());
    }

    @Listener(order = Order.PRE)
    public void onExplosion(ExplosionEvent.Pre event) {
        if (plotWorld.equalsWorld(event.getTargetWorld())) {
            // none of these
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.PRE)
    public void onInteractPrimary(InteractBlockEvent.Primary event, @First Player player) {
        if (!plotWorld.equalsWorld(event.getTargetBlock().getWorldUniqueId())) {
            return;
        }

        if (!player.hasPermission(Permissions.ACTION_MODIFY) || !getMask(player).contains(event.getTargetBlock().getPosition())) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.PRE)
    public void onBlockPlace(ChangeBlockEvent.Place event, @First Player player) {
        if (!plotWorld.equalsWorld(player.getWorld())) {
            return;
        }

        if (!player.hasPermission(Permissions.ACTION_MODIFY)) {
            event.filterAll();
            return;
        }

        PlotMask mask = getMask(player);
        event.filter(location -> mask.contains(location.getBlockPosition()));
    }

    @Listener(order = Order.PRE)
    public void onInteractSecondary(InteractBlockEvent.Secondary event, @First Player player) {
        if (!plotWorld.equalsWorld(event.getTargetBlock().getWorldUniqueId())) {
            return;
        }

        if (!player.hasPermission(Permissions.ACTION_MODIFY) || !canEdit(player, event.getTargetBlock().getPosition())) {
            event.setUseItemResult(Tristate.FALSE);
        }
    }

    // might not detect entities fired into the plot
    @Listener(order = Order.PRE)
    public void onInteractEntity(InteractEntityEvent event, @First Player player) {
        if (!plotWorld.equalsWorld(event.getTargetEntity().getWorld())) {
            return;
        }

        if (!player.hasPermission(Permissions.ACTION_INTERACT_ENTITY) || !canEdit(player, event.getTargetEntity().getLocation().getBlockPosition())) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onDamage(DamageEntityEvent event, @First EntityDamageSource damageSource) {
        if (!plotWorld.equalsWorld(event.getTargetEntity().getWorld())) {
            return;
        }

        Entity source = damageSource.getSource(), cause = source;
        if (damageSource instanceof IndirectEntityDamageSource) {
            cause = ((IndirectEntityDamageSource) damageSource).getIndirectSource();
        }

        if (cause instanceof Player) {
            Player player = (Player) cause;
            if (!player.hasPermission(Permissions.ACTION_DAMAGE) || !canEdit(player, event.getTargetEntity().getLocation().getBlockPosition())) {
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

    @Listener(order = Order.PRE)
    public void onUse(UseItemStackEvent.Start event, @First Player player) {
        if (!plotWorld.equalsWorld(player.getWorld())) {
            return;
        }

        if (!player.hasPermission(Permissions.ACTION_USE) || !canEdit(player, player.getLocation().getBlockPosition())) {
            event.setCancelled(true);
        }
    }

    // DropItemEvent is dumb
    @Listener(order = Order.PRE)
    public void drop(DropItemEvent event) {
        if (!(event.getSource() instanceof Entity)) {
            return;
        }

        Entity entity = (Entity) event.getSource();
        if (!plotWorld.equalsWorld(entity.getWorld())) {
            return;
        }

        if (entity.getType() != EntityTypes.PLAYER) {
            return;
        }

        Player player = (Player) entity;
        if (!player.hasPermission(Permissions.ACTION_DROP) || !canEdit(player, player.getLocation().getBlockPosition())) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.PRE)
    public void onSpawn(SpawnEntityEvent event, @First Player player) {
        if (!plotWorld.equalsWorld(player.getWorld())) {
            return;
        }

        boolean spawnInanimate = player.hasPermission(Permissions.ACTION_SPAWN_INANIMATE);
        boolean spawnLiving = player.hasPermission(Permissions.ACTION_SPAWN_LIVING);
        PlotMask mask = getMask(player);
        event.filterEntities(entity -> {
            if (entity instanceof Living && !(entity instanceof ArmorStand)) {
                return spawnLiving && mask.contains(entity.getLocation().getBlockPosition());
            }
            return spawnInanimate && mask.contains(entity.getLocation().getBlockPosition());
        });
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @Root Player player) {
        if (!plotWorld.equalsWorld(player.getWorld())) {
            return;
        }

        PlotId plotId = plotWorld.plotSchema().containingPlot(player.getLocation().getBlockPosition());
        if (plotId.present()) {
            Format format = Fmt.copy();
            Supplier<Text> async = () -> PlotActions.plotInfo(plotWorld.database(), plotId, format);
            Consumer<Text> sync = text -> player.sendMessage(ChatTypes.ACTION_BAR, text);
            Plots.executor().async(async, sync);
        }
    }

    @Listener (order = Order.POST)
    public void onTeleport(MoveEntityEvent.Teleport event, @Root Player player) {
        if (!plotWorld.equalsWorld(event.getToTransform().getExtent())) {
            return;
        }

        PlotId plotId = plotWorld.plotSchema().containingPlot(event.getToTransform().getPosition().toInt());
        if (plotId.present()) {
            Format format = Fmt.copy();
            Supplier<Text> async = () -> PlotActions.plotInfo(plotWorld.database(), plotId, format);
            Consumer<Text> sync = text -> player.sendMessage(ChatTypes.ACTION_BAR, text);
            Plots.executor().async(async, sync);
        }
    }

    @Listener
    public void onEntityMove(MoveEntityEvent event) {
        if (!plotWorld.equalsWorld(event.getToTransform().getExtent())) {
            return;
        }

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

    private void onEntityMove(Cancellable event, Entity entity, Vector3i from, Vector3i to) {
        if (from.getX() == to.getX() && from.getZ() == to.getZ()) {
            return;
        }

        PlotId fromId = plotWorld.plotSchema().plotId(from);
        PlotBounds bounds = plotWorld.plotSchema().plotBounds(fromId);
        if (!bounds.contains(from)) {
            // entity is already outside of a plot so remove it
            entity.remove();
        } else if (!bounds.contains(to)) {
            // entity attempted to leave the bounds of a plot so prevent it
            event.setCancelled(true);
        }
    }

    private void onPlayerMove(Player player, Vector3i from, Vector3i to) {
        PlotId fromId = plotWorld.plotSchema().plotId(from);
        if (plotWorld.plotSchema().plotBounds(fromId).contains(from)) {
            // player already inside a plot
            return;
        }

        PlotId toId = plotWorld.plotSchema().plotId(to);
        if (plotWorld.plotSchema().plotBounds(toId).contains(to)) {
            // player entered the bounds of a new plot
            Format format = Fmt.copy();
            WorldDatabase database = plotWorld.database();
            Supplier<Text> info = () -> PlotActions.plotInfo(database, toId, format);
            Consumer<Text> send = text -> player.sendMessage(ChatTypes.ACTION_BAR, text);
            Plots.executor().async(info, send);
        }
    }
}
