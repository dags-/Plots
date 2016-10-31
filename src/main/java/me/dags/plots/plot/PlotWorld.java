package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.dags.plots.Plots;
import me.dags.plots.database.UserActions;
import me.dags.plots.database.WorldDatabase;
import me.dags.plots.operation.CopyBiomeOperation;
import me.dags.plots.operation.CopyBlockOperation;
import me.dags.plots.operation.FillBiomeOperation;
import me.dags.plots.operation.ResetOperation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class PlotWorld {

    private final String world;
    private final UUID worldId;
    private final PlotSchema plotSchema;
    private final WorldDatabase database;
    private final LoadingCache<UUID, PlotUser> users;
    private final PlotWorldListener plotListener = new PlotWorldListener(this);

    public PlotWorld(World world, WorldDatabase database, PlotSchema plotSchema) {
        this.world = world.getName();
        this.worldId = world.getUniqueId();
        this.database = database;
        this.plotSchema = plotSchema;
        this.users = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new UserLoaderSaver());
    }

    public String world() {
        return world;
    }

    public PlotSchema plotSchema() {
        return plotSchema;
    }

    public PlotUser user(UUID uuid) {
        return users.getUnchecked(uuid);
    }

    public PlotUser loadUser(UUID uuid) {
        return UserActions.loadPlotUser(database, plotSchema(), uuid);
    }

    public WorldDatabase database() {
        return database;
    }

    public boolean equalsWorld(World world) {
        return equalsWorld(world.getUniqueId());
    }

    public boolean equalsWorld(UUID worldId) {
        return this.worldId.equals(worldId);
    }

    public void register(Plots plots) {
        Sponge.getEventManager().registerListeners(plots, plotListener);
    }

    public void unregister() {
        Sponge.getEventManager().unregisterListeners(plotListener);
    }

    public void refreshUser(UUID uuid) {
        users.invalidate(uuid);
        users.getUnchecked(uuid);
    }

    public void resetPlot(PlotId plotId, Runnable callback) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds bounds = plotSchema.plotBounds(plotId);
            ResetOperation reset = new ResetOperation(world, bounds);
            reset.onComplete(callback);
            Plots.API().dispatcher().queueOperation(reset);
        });
    }

    public void setBiome(PlotId plotId, BiomeType type) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds bounds = plotSchema.plotBounds(plotId);
            MutableBiomeArea area = world.getBiomeView(bounds.getMin(), bounds.getMax());
            FillBiomeOperation fill = new FillBiomeOperation(world.getName(), area, type);
            Plots.API().dispatcher().queueOperation(fill);
        });
    }

    public void copyPlot(PlotId fromId, PlotId toId, Runnable callback) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds from = plotSchema().plotBounds(fromId);
            PlotBounds to = plotSchema().plotBounds(toId);
            MutableBlockVolume volFrom = world.getBlockView(from.getBlockMin(), from.getBlockMax());
            MutableBlockVolume volTo = world.getBlockView(to.getBlockMin(), to.getBlockMax());
            CopyBlockOperation copyBlocks = new CopyBlockOperation(world(), volFrom, volTo);
            copyBlocks.onComplete(() -> {
                MutableBiomeArea fromBiome = world.getBiomeView(from.getMin(), from.getMax());
                MutableBiomeArea toBiome = world.getBiomeView(to.getMin(), to.getMax());
                CopyBiomeOperation copyBiome = new CopyBiomeOperation(world(), fromBiome, toBiome);
                copyBiome.onComplete(callback);
                Plots.API().dispatcher().queueOperation(copyBiome);
            });
            Plots.API().dispatcher().queueOperation(copyBlocks);
        });
    }

    public void teleport(Player player, PlotId plotId) {
        PlotBounds plotBounds = plotSchema.plotBounds(plotId);
        int x = plotBounds.getMin().getX() - plotSchema.wallWidth();
        int y = plotSchema.surfaceHeight();
        int z = plotBounds.getMin().getY() - plotSchema.wallWidth();
        Vector3d position = new Vector3d(x, y, z);
        Vector3d rotation = new Vector3d(0, -45, 0);
        Location<World> location = new Location<>(player.getWorld(), position);
        player.setLocationAndRotation(location, rotation);
    }

    private class UserLoaderSaver extends CacheLoader<UUID, PlotUser> {
        @Override
        public PlotUser load(UUID key) throws Exception {
            return loadUser(key);
        }
    }
}
