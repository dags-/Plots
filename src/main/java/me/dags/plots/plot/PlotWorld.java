package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.Format;
import me.dags.plots.PlotsPlugin;
import me.dags.plots.database.Queries;
import me.dags.plots.database.statment.Select;
import me.dags.plots.operation.CopyBiomeOperation;
import me.dags.plots.operation.CopyBlockOperation;
import me.dags.plots.operation.FillBiomeOperation;
import me.dags.plots.operation.FillBlockOperation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;
import org.spongepowered.api.text.chat.ChatTypes;
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

    private static final Format FORMAT = PlotsPlugin.getConfig().getMessageFormat();

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
    public void onWeather(ChangeWorldWeatherEvent event) {
        if (thisWorld(event.getTargetWorld())) {
            event.setWeather(Weathers.CLEAR);
            event.setDuration(Integer.MAX_VALUE);
            PlotsPlugin.log("Set weather for world: {}", world);
        }
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        if (thisWorld(event.getTargetEntity().getWorld())) {
            PlotsPlugin.getDatabase().loadUser(world, event.getTargetEntity().getUniqueId(), this::addUser);
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
            Optional<Player> optional = event.getCause().first(Player.class);
            if (optional.isPresent()) {
                Player player = optional.get();
                if (!player.hasPermission("plots.bypass.build")) {
                    event.filter(loc -> canDo(player, loc.getBlockPosition(), "plots.build"));
                }
            } else {
                event.filter(loc -> withinPlot(loc.getBlockPosition()));
            }
        }
    }

    // might not detect entities fired into the plot
    @Listener(order = Order.PRE)
    public void onInteractEntity(InteractEntityEvent event, @First Player player) {
        if (thisWorld(event.getTargetEntity().getWorld())) {
            if (!player.hasPermission("plots.bypass.entity.interact")) {
                if (!canDo(player, event.getTargetEntity().getLocation().getBlockPosition(), "plots.entity.interact")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.PRE)
    public void onUse(UseItemStackEvent.Start event, @First Player player) {
        if (thisWorld(player.getWorld())) {
            if (!player.hasPermission("plots.bypass.item.use")) {
                if (!canDo(player, player.getLocation().getBlockPosition(), "plots.item.use")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.PRE)
    public void onSpawn(SpawnEntityEvent event, @First Player player) {
        if (thisWorld(event.getTargetWorld())) {
            if (!player.hasPermission("plots.bypass.entity.spawn")) {
                event.filterEntityLocations(loc -> canDo(player, loc.getBlockPosition(), "plots.entity.spawn"));
            }
        }
    }

    @Listener
    public void onTeleport(DisplaceEntityEvent.Teleport.TargetPlayer event) {
        World from = event.getFromTransform().getExtent();
        World to = event.getToTransform().getExtent();
        if (from != to) {
            if (thisWorld(from)) {
                PlotsPlugin.log("Dropping plotUser: {}", event.getTargetEntity().getName());
                PlotUser plotUser = getUser(event.getTargetEntity().getUniqueId());
                removeUser(plotUser);
            } else if (thisWorld(to)) {
                PlotsPlugin.log("Getting plotUser: {}", event.getTargetEntity().getName());
                PlotsPlugin.getDatabase().loadUser(world, event.getTargetEntity().getUniqueId(), this::addUser);
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
                if (!getPlotBounds(fromId).contains(from) && getPlotBounds(toId).contains(to)) {
                    final Player player = event.getTargetEntity();

                    Select<Optional<User>> owner = Queries.selectPlotOwner(world, toId).build();
                    PlotsPlugin.getDatabase().select(owner, user -> {
                        Format.MessageBuilder message = FORMAT.info("Plot: ").stress(toId);
                        if (user.isPresent()) {
                            message.info(", Owner: ").stress(user.get().getName());
                        }
                        player.sendMessage(ChatTypes.ACTION_BAR, message.build());
                    });
                }
            }
        }
    }

    public void setBiome(PlotId plotId, BiomeType type) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds bounds = getPlotBounds(plotId);
            MutableBiomeArea area = world.getBiomeView(bounds.getMin(), bounds.getMax());
            FillBiomeOperation fill = new FillBiomeOperation(world.getName(), area, type);
            PlotsPlugin.getPlots().getDispatcher().queueOperation(fill);
        });
    }

    public void resetPlot(PlotId plotId) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds bounds = getPlotBounds(plotId);
            MutableBlockVolume volume = world.getBlockView(bounds.getBlockMin(), bounds.getBlockMax());
            FillBlockOperation fill = new FillBlockOperation(getWorld(), volume, BlockTypes.AIR.getDefaultState());
            fill.onComplete(() -> {
                MutableBiomeArea biomeArea = world.getBiomeView(bounds.getMin(), bounds.getMax());
                world.getWorldGenerator().getBaseGenerationPopulator().populate(world, volume, biomeArea.getImmutableBiomeCopy());
            });
            PlotsPlugin.getPlots().getDispatcher().queueOperation(fill);
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
                PlotsPlugin.getPlots().getDispatcher().queueOperation(new CopyBiomeOperation(getWorld(), fromBiome, toBiome));
            });
            PlotsPlugin.getPlots().getDispatcher().queueOperation(copyBlocks);
        });
    }

    public void teleportToPlot(Player player, PlotId plotId) {
        Vector3i position = plotProvider.plotWarp(getPlotBounds(plotId));
        Location<World> location = new Location<>(player.getWorld(), position);
        player.setLocationAndRotation(location, player.getRotation());
        FORMAT.info("Teleporting to ").stress(getWorld()).info(";").stress(plotId).tell(player);
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

    public PlotUser getOrCreateUser(UUID uuid) {
        PlotUser user = getUser(uuid);
        return user.isPresent() ? user : PlotUser.builder().world(world).uuid(uuid).build();
    }

    public PlotUser getUser(UUID uuid) {
        PlotUser user = plotUsers.get(uuid);
        return user != null ? user : PlotUser.EMPTY;
    }

    public boolean canDo(Player player, Vector3i position, String permission) {
        if (!permission.isEmpty() && player.hasPermission(permission)) {
            return false;
        }
        PlotId plotId = plotProvider.plotId(position);
        return getPlotBounds(plotId).contains(position) && getUser(player.getUniqueId()).isWhitelisted(plotId);
    }

    public boolean withinPlot(Vector3i vector3i) {
        PlotId plotId = plotProvider.plotId(vector3i);
        return getPlotBounds(plotId).contains(vector3i);
    }

    public void refreshUser(UUID uuid) {
        if (plotUsers.containsKey(uuid)) {
            PlotsPlugin.getDatabase().loadUser(world, uuid, user -> {
                plotUsers.put(uuid, user);
            });
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
            PlotsPlugin.getDatabase().saveUser(user);
        }
    }

    private boolean thisWorld(World world) {
        return world.getUniqueId() == worldId;
    }
}
