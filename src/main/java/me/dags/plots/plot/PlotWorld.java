package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.Format;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.database.Queries;
import me.dags.plots.database.statment.Select;
import me.dags.plots.operation.CopyBiomeOperation;
import me.dags.plots.operation.CopyBlockOperation;
import me.dags.plots.operation.FillBiomeOperation;
import me.dags.plots.operation.ResetOperation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
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
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.weather.Weathers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class PlotWorld {

    private static final Format FORMAT = Plots.getConfig().getMessageFormat();

    private final String world;
    private final UUID worldId;
    private final PlotProvider plotProvider;
    private final Map<UUID, PlotUser> plotUsers = new HashMap<>();
    private final Map<PlotId, PlotBounds> boundsCache = new HashMap<>();

    public PlotWorld(World world, PlotProvider plotProvider) {
        world.setWeather(Weathers.CLEAR);
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

    @Listener(order = Order.PRE)
    public void onBlockChange(ChangeBlockEvent event) {
        if (thisWorld(event.getTargetWorld())) {
            // prevent blocks 'leaking' outside of a plot (trees growing, water flowing etc)
            event.filter(loc -> withinPlot(loc.getBlockPosition()));
        }
    }

    @Listener(order = Order.PRE)
    public void onExplosion(ExplosionEvent.Pre event) {
        if (thisWorld(event.getTargetWorld())) {
            // none of these
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.PRE)
    public void onInteractPrimary(InteractBlockEvent.Primary event, @First Player player) {
        if (event.getTargetBlock().getWorldUniqueId() == worldId) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            if (!hasPermission(player, Permissions.ACTION_MODIFY) || !canEdit(player, event.getTargetBlock().getPosition())) {
                // no reason for players to be able to left-click blocks if not allowed to build here
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.PRE)
    public void onInteractSecondary(InteractBlockEvent.Secondary event, @First Player player) {
        if (event.getTargetBlock().getWorldUniqueId() == worldId) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            if (!player.hasPermission(Permissions.ACTION_MODIFY) || !canEdit(player, event.getTargetBlock().getPosition())) {
                // don't allow use/placement of items/blocks, but still allow interactions with doors etc
                event.setUseItemResult(Tristate.FALSE);
            }
        }
    }

    // might not detect entities fired into the plot
    @Listener(order = Order.PRE)
    public void onInteractEntity(InteractEntityEvent event, @First Player player) {
        if (thisWorld(event.getTargetEntity().getWorld())) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            if (!hasPermission(player, Permissions.ACTION_INTERACT_ENTITY) || !canEdit(player, event.getTargetEntity().getLocation().getBlockPosition())) {
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onDamage(DamageEntityEvent event, @First EntityDamageSource damageSource) {
        if (thisWorld(event.getTargetEntity().getWorld())) {
            Entity source = damageSource.getSource(), cause = source;

            if (damageSource instanceof IndirectEntityDamageSource) {
                cause = ((IndirectEntityDamageSource) damageSource).getIndirectSource();
            }

            if (cause instanceof Player) {
                Player player = (Player) cause;
                if (hasPermission(player, Permissions.ACTION_BYPASS)) {
                    return;
                }
                if (!hasPermission(player, Permissions.ACTION_DAMAGE) || !canEdit(player, event.getTargetEntity().getLocation().getBlockPosition())) {
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
        if (thisWorld(player.getWorld())) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            if (!hasPermission(player, Permissions.ACTION_USE) || !canEdit(player, player.getLocation().getBlockPosition())) {
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.PRE)
    public void onDrop(DropItemEvent.Dispense event, @First Player player) {
        if (thisWorld(player.getWorld())) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            boolean permission = hasPermission(player, Permissions.ACTION_DROP);
            event.filterEntityLocations(loc -> permission && !canEdit(player, loc.getBlockPosition()));
        }
    }

    @Listener(order = Order.PRE)
    public void onSpawn(SpawnEntityEvent event, @First Player player) {
        if (thisWorld(event.getTargetWorld())) {
            if (player.hasPermission(Permissions.ACTION_BYPASS)) {
                return;
            }
            boolean spawnInanimate = hasPermission(player, Permissions.ACTION_SPAWN_INANIMATE);
            boolean spawnLiving = hasPermission(player, Permissions.ACTION_SPAWN_LIVING);
            event.filterEntities(entity -> {
                if (entity instanceof Living && !(entity instanceof ArmorStand)) {
                    return spawnLiving && canEdit(player, entity.getLocation().getBlockPosition());
                }
                return spawnInanimate && canEdit(player, entity.getLocation().getBlockPosition());
            });
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
    public void onEntityMove(DisplaceEntityEvent.Move.TargetLiving event) {
        if (thisWorld(event.getToTransform().getExtent())) {
            Vector3i from = event.getFromTransform().getLocation().getBlockPosition();
            Vector3i to = event.getToTransform().getLocation().getBlockPosition();
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                if (event.getTargetEntity() instanceof Player) {
                    onPlayerMove((Player) event.getTargetEntity(), from, to);
                } else {
                    onLivingMove(event, event.getTargetEntity(), from, to);
                }
            }
        }
    }

    private void onLivingMove(Cancellable event, Living living, Vector3i from, Vector3i to) {
        if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
            PlotId fromId = getPlotId(from);
            PlotBounds bounds = getPlotBounds(fromId);

            if (!bounds.contains(from)) {
                // entity is already outside of a plot so remove it
                living.remove();
            } else if (!bounds.contains(to)) {
                // entity attempted to leave the bounds of a plot so prevent it
                event.setCancelled(true);
            }
        }
    }

    private void onPlayerMove(Player player, Vector3i from, Vector3i to) {
        PlotId fromId = getPlotId(from);
        if (getPlotBounds(fromId).contains(from)) {
            // player already inside a plot
            return;
        }
        PlotId toId = getPlotId(to);
        if (getPlotBounds(toId).contains(to)) {
            // player entered the bounds of a new plot
            Select<Optional<User>> owner = Queries.selectPlotOwner(world, toId).build();
            Plots.getDatabase().select(owner, user -> {
                Format.MessageBuilder message = FORMAT.info("Plot: ").stress(toId);
                if (user.isPresent()) {
                    message.info(", Owner: ").stress(user.get().getName());
                }
                player.sendMessage(ChatTypes.ACTION_BAR, message.build());
            });
        }
    }

    public void setBiome(PlotId plotId, BiomeType type) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds bounds = getPlotBounds(plotId);
            MutableBiomeArea area = world.getBiomeView(bounds.getMin(), bounds.getMax());
            FillBiomeOperation fill = new FillBiomeOperation(world.getName(), area, type);
            Plots.getApi().getDispatcher().queueOperation(fill);
        });
    }

    public void resetPlot(PlotId plotId) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds bounds = getPlotBounds(plotId);
            ResetOperation reset = new ResetOperation(world, bounds);
            Plots.getApi().getDispatcher().queueOperation(reset);
        });
    }

    public void copyPlot(PlotId fromId, PlotId toId) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds from = getPlotBounds(fromId);
            PlotBounds to = getPlotBounds(toId);
            MutableBlockVolume volFrom = world.getBlockView(from.getBlockMin(), from.getBlockMax());
            MutableBlockVolume volTo = world.getBlockView(to.getBlockMin(), to.getBlockMax());
            CopyBlockOperation copyBlocks = new CopyBlockOperation(getWorld(), volFrom, volTo);
            copyBlocks.onComplete(() -> {
                MutableBiomeArea fromBiome = world.getBiomeView(from.getMin(), from.getMax());
                MutableBiomeArea toBiome = world.getBiomeView(to.getMin(), to.getMax());
                Plots.getApi().getDispatcher().queueOperation(new CopyBiomeOperation(getWorld(), fromBiome, toBiome));
            });
            Plots.getApi().getDispatcher().queueOperation(copyBlocks);
        });
    }

    public void teleportToPlot(Player player, PlotId plotId) {
        Vector3i position = plotProvider.plotWarp(getPlotBounds(plotId));
        Location<World> location = new Location<>(player.getWorld(), position);
        player.setLocationAndRotation(location, new Vector3d(0, -45, 0));
        FORMAT.info("Teleporting to ").stress(getWorld() + ": ").stress(plotId).tell(player);
    }

    public String getWorld() {
        return world;
    }

    public UUID getWorldId() {
        return worldId;
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
        if (user == null) {
            throw new UnsupportedOperationException("User for " + uuid + " + should not be null!");
        }
        return user;
    }

    private boolean hasPermission(Subject subject, String permission) {
        return permission.isEmpty() || subject.hasPermission(permission);
    }

    public boolean canEdit(Player player, Vector3i position) {
        PlotId plotId = plotProvider.plotId(position);
        return getPlotBounds(plotId).contains(position) && getUser(player.getUniqueId()).isWhitelisted(plotId);
    }

    public boolean withinPlot(Vector3i vector3i) {
        PlotId plotId = plotProvider.plotId(vector3i);
        return getPlotBounds(plotId).contains(vector3i);
    }

    public void refreshUser(UUID uuid) {
        if (plotUsers.containsKey(uuid)) {
            Plots.getDatabase().loadUser(world, uuid, user -> {
                plotUsers.put(uuid, user);
            });
        }
    }

    public void addUser(PlotUser user) {
        if (user.isPresent()) {
            Plots.log("Adding user {}", user.getUUID());
            plotUsers.put(user.getUUID(), user);
        } else {
            throw new UnsupportedOperationException("Attempted to add EMPTY PlotUser to PlotWord " + world);
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
