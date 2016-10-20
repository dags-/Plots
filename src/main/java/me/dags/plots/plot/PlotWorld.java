package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.Format;
import me.dags.plots.Plots;
import me.dags.plots.operation.CopyBiomeOperation;
import me.dags.plots.operation.CopyBlockOperation;
import me.dags.plots.operation.FillBiomeOperation;
import me.dags.plots.operation.ResetOperation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.weather.Weathers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class PlotWorld {

    static final Format FORMAT = Plots.getConfig().getMessageFormat();

    private final String world;
    private final UUID worldId;
    private final PlotProvider plotProvider;
    private final Map<UUID, PlotUser> plotUsers = new HashMap<>();
    private final PlotWorldListener listener = new PlotWorldListener(this);

    public PlotWorld(World world, PlotProvider plotProvider) {
        world.setWeather(Weathers.CLEAR);
        this.world = world.getName();
        this.worldId = world.getUniqueId();
        this.plotProvider = plotProvider;
    }

    public void register(Object plugin) {
        Sponge.getEventManager().registerListeners(plugin, listener);
    }

    public void unregister() {
        Sponge.getEventManager().unregisterListeners(listener);
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
        return plotProvider.plotBounds(plotId);
    }

    public PlotUser getUser(UUID uuid) {
        PlotUser user = plotUsers.get(uuid);
        if (user == null) {
            Plots.log("User for {} should not be null!", uuid);
            return PlotUser.EMPTY;
        }
        return user;
    }

    public void refreshUser(UUID uuid) {
        if (plotUsers.containsKey(uuid)) {
            Plots.getDatabase().loadUser(world, uuid, user -> {
                plotUsers.put(uuid, user);
            });
        }
    }

    boolean hasPermission(Subject subject, String permission) {
        return permission.isEmpty() || subject.hasPermission(permission);
    }

    boolean canEdit(Player player, Vector3i position) {
        PlotUser user = getUser(player.getUniqueId());
        return user.getMask().contains(position.getX(), position.getY(), position.getZ());
    }

    boolean withinPlot(Vector3i vector3i) {
        PlotId plotId = plotProvider.plotId(vector3i);
        return getPlotBounds(plotId).contains(vector3i);
    }

    void addUser(PlotUser user) {
        if (user.isPresent()) {
            Plots.log("Adding user {}", user.getUUID());
            plotUsers.put(user.getUUID(), user);
        } else {
            throw new UnsupportedOperationException("Attempted to add EMPTY PlotUser to PlotWord " + world);
        }
    }

    void removeUser(PlotUser user) {
        if (user.isPresent()) {
            plotUsers.remove(user.getUUID());
            Plots.getDatabase().saveUser(user);
        }
    }

    boolean thisWorld(World world) {
        return world.getUniqueId() == worldId;
    }
}
