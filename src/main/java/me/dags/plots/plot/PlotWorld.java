package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.UserActions;
import me.dags.plots.database.WorldDatabase;
import me.dags.plots.operation.FillBiomeOperation;
import me.dags.plots.operation.ResetOperation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.MutableBiomeVolume;

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

    public UUID worldId() {
        return worldId;
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
        if (users.getIfPresent(uuid) != null) {
            users.invalidate(uuid);
            users.getUnchecked(uuid);
            Sponge.getServiceManager().provideUnchecked(UserStorageService.class)
                    .get(uuid)
                    .flatMap(User::getPlayer)
                    .ifPresent(Cmd.FMT().info("Your plot data has been refreshed")::tell);
        }
    }

    public void setUser(PlotUser plotUser) {
        users.put(plotUser.uuid(), plotUser);
    }

    public void resetPlot(PlotId plotId, Runnable callback) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds bounds = plotSchema.sectionBounds(plotId);
            ResetOperation reset = new ResetOperation(world, bounds);
            reset.onComplete(callback);
            Plots.core().dispatcher().queueOperation(reset);
        });
    }

    public void setBiome(PlotId plotId, BiomeType type) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds bounds = plotSchema.plotBounds(plotId);
            MutableBiomeVolume volume = world.getBiomeView(bounds.getBlockMin(), bounds.getBlockMax());
            FillBiomeOperation fill = new FillBiomeOperation(world.getName(), volume, type);
            Plots.core().dispatcher().queueOperation(fill);
        });
    }

    public void copyPlot(PlotId fromId, PlotId toId, Runnable callback) {
        Sponge.getServer().getWorld(worldId).ifPresent(world -> {
            PlotBounds from = plotSchema().plotBounds(fromId);
            PlotBounds to = plotSchema().plotBounds(toId);

            Location<World> target = world.getLocation(to.getBlockMin());

            ArchetypeVolume volume = world.createArchetypeVolume(from.getBlockMin(), from.getBlockMax(), from.getBlockMin());
            volume.apply(target, BlockChangeFlag.NONE, Plots.PLOTS_CAUSE());
            callback.run();
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
