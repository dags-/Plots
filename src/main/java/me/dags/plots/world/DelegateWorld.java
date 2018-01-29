package me.dags.plots.world;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.*;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.extent.*;
import org.spongepowered.api.world.extent.worker.MutableBiomeVolumeWorker;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.weather.Weather;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public abstract class DelegateWorld implements World {

    private final World delegate;

    public DelegateWorld(World delegate) {
        this.delegate = delegate;
    }

    @Override
    public Collection<Player> getPlayers() {
        return delegate.getPlayers();
    }

    @Override
    public Optional<Chunk> getChunk(int cx, int cy, int cz) {
        return delegate.getChunk(cx, cy, cz);
    }

    @Override
    public Optional<Chunk> loadChunk(int cx, int cy, int cz, boolean shouldGenerate) {
        return delegate.loadChunk(cx, cy, cz, shouldGenerate);
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        return delegate.unloadChunk(chunk);
    }

    @Override
    public Iterable<Chunk> getLoadedChunks() {
        return delegate.getLoadedChunks();
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        return delegate.getEntity(uuid);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public ChunkPreGenerate.Builder newChunkPreGenerate(Vector3d center, double diameter) {
        return delegate.newChunkPreGenerate(center, diameter);
    }

    @Override
    public Dimension getDimension() {
        return delegate.getDimension();
    }

    @Override
    public WorldGenerator getWorldGenerator() {
        return delegate.getWorldGenerator();
    }

    @Override
    public WorldProperties getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Path getDirectory() {
        return delegate.getDirectory();
    }

    @Override
    public WorldStorage getWorldStorage() {
        return delegate.getWorldStorage();
    }

    @Override
    public void triggerExplosion(Explosion explosion) {
        delegate.triggerExplosion(explosion);
    }

    @Override
    public PortalAgent getPortalAgent() {
        return delegate.getPortalAgent();
    }

    @Override
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public MutableBiomeVolumeWorker<World> getBiomeWorker() {
        return delegate.getBiomeWorker();
    }

    @Override
    public MutableBlockVolumeWorker<World> getBlockWorker() {
        return delegate.getBlockWorker();
    }

    @Override
    public boolean save() throws IOException {
        return delegate.save();
    }

    @Override
    public int getViewDistance() {
        return delegate.getViewDistance();
    }

    @Override
    public void setViewDistance(int viewDistance) {
        delegate.setViewDistance(viewDistance);
    }

    @Override
    public void resetViewDistance() {
        delegate.resetViewDistance();
    }

    @Override
    public int getHighestYAt(int x, int z) {
        return delegate.getHighestYAt(x, z);
    }

    @Override
    public int getPrecipitationLevelAt(int x, int z) {
        return delegate.getPrecipitationLevelAt(x, z);
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState blockState, BlockChangeFlag flag) {
        return delegate.setBlock(x, y, z, blockState, flag);
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        return delegate.createSnapshot(x, y, z);
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(int x, int y, int z) {
        return delegate.getScheduledUpdates(x, y, z);
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        return delegate.addScheduledUpdate(x, y, z, priority, ticks);
    }

    @Override
    public void removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
        delegate.removeScheduledUpdate(x, y, z, update);
    }

    @Override
    public boolean isLoaded() {
        return delegate.isLoaded();
    }

    @Override
    public Extent getExtentView(Vector3i newMin, Vector3i newMax) {
        return delegate.getExtentView(newMin, newMax);
    }

    @Override
    public Optional<UUID> getCreator(int x, int y, int z) {
        return delegate.getCreator(x, y, z);
    }

    @Override
    public Optional<UUID> getNotifier(int x, int y, int z) {
        return delegate.getNotifier(x, y, z);
    }

    @Override
    public Optional<AABB> getBlockSelectionBox(int x, int y, int z) {
        return delegate.getBlockSelectionBox(x, y, z);
    }

    @Override
    public Set<AABB> getIntersectingBlockCollisionBoxes(AABB box) {
        return delegate.getIntersectingBlockCollisionBoxes(box);
    }

    @Override
    public Set<AABB> getIntersectingCollisionBoxes(Entity owner, AABB box) {
        return delegate.getIntersectingCollisionBoxes(owner, box);
    }

    @Override
    public ArchetypeVolume createArchetypeVolume(Vector3i min, Vector3i max, Vector3i origin) {
        return delegate.createArchetypeVolume(min, max, origin);
    }

    @Override
    public Collection<Entity> getEntities() {
        return delegate.getEntities();
    }

    @Override
    public Collection<Entity> getEntities(Predicate<Entity> filter) {
        return delegate.getEntities(filter);
    }

    @Override
    public Set<Entity> getIntersectingEntities(AABB box, Predicate<Entity> filter) {
        return delegate.getIntersectingEntities(box, filter);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d end, Predicate<EntityHit> filter) {
        return delegate.getIntersectingEntities(start, end, filter);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d direction, double distance, Predicate<EntityHit> filter) {
        return delegate.getIntersectingEntities(start, direction, distance, filter);
    }

    @Override
    public Collection<TileEntity> getTileEntities() {
        return delegate.getTileEntities();
    }

    @Override
    public Collection<TileEntity> getTileEntities(Predicate<TileEntity> filter) {
        return delegate.getTileEntities(filter);
    }

    @Override
    public Optional<TileEntity> getTileEntity(int x, int y, int z) {
        return delegate.getTileEntity(x, y, z);
    }

    @Override
    public MutableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        return delegate.getBlockView(newMin, newMax);
    }

    @Override
    public MutableBlockVolume getBlockView(DiscreteTransform3 transform) {
        return delegate.getBlockView(transform);
    }

    @Override
    public Vector3i getBlockMin() {
        return delegate.getBlockMin();
    }

    @Override
    public Vector3i getBlockMax() {
        return delegate.getBlockMax();
    }

    @Override
    public Vector3i getBlockSize() {
        return delegate.getBlockSize();
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return delegate.containsBlock(x, y, z);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return delegate.getBlock(x, y, z);
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        return delegate.getBlockType(x, y, z);
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        return delegate.getUnmodifiableBlockView();
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return delegate.getImmutableBlockCopy();
    }


    @Override
    public int getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return delegate.getBlockDigTimeWith(x, y, z, itemStack, profile);
    }

    @Override
    public Vector3i getBiomeMin() {
        return delegate.getBiomeMin();
    }

    @Override
    public Vector3i getBiomeMax() {
        return delegate.getBiomeMax();
    }

    @Override
    public Vector3i getBiomeSize() {
        return delegate.getBiomeSize();
    }

    @Override
    public boolean containsBiome(int x, int y, int z) {
        return delegate.containsBiome(x, y, z);
    }

    @Override
    public BiomeType getBiome(int x, int y, int z) {
        return delegate.getBiome(x, y, z);
    }

    @Override
    public UnmodifiableBiomeVolume getUnmodifiableBiomeView() {
        return delegate.getUnmodifiableBiomeView();
    }

    @Override
    public MutableBiomeVolume getBiomeCopy(StorageType type) {
        return delegate.getBiomeCopy(type);
    }

    @Override
    public ImmutableBiomeVolume getImmutableBiomeCopy() {
        return delegate.getImmutableBiomeCopy();
    }

    @Override
    public <E> Optional<E> get(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        return delegate.get(x, y, z, key);
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(int x, int y, int z, Class<T> manipulatorClass) {
        return delegate.get(x, y, z, manipulatorClass);
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(int x, int y, int z, Key<V> key) {
        return delegate.getValue(x, y, z, key);
    }

    @Override
    public boolean supports(int x, int y, int z, Key<?> key) {
        return delegate.supports(x, y, z, key);
    }

    @Override
    public boolean supports(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return delegate.supports(x, y, z, manipulatorClass);
    }

    @Override
    public Set<Key<?>> getKeys(int x, int y, int z) {
        return delegate.getKeys(x, y, z);
    }

    @Override
    public Set<ImmutableValue<?>> getValues(int x, int y, int z) {
        return delegate.getValues(x, y, z);
    }

    @Override
    public Collection<DataManipulator<?, ?>> getManipulators(int x, int y, int z) {
        return delegate.getManipulators(x, y, z);
    }
    @Override
    public boolean validateRawData(int x, int y, int z, DataView container) {
        return delegate.validateRawData(x, y, z, container);
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(int x, int y, int z, Class<T> propertyClass) {
        return delegate.getProperty(x, y, z, propertyClass);
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(int x, int y, int z, Direction direction, Class<T> propertyClass) {
        return delegate.getProperty(x, y, z, direction, propertyClass);
    }

    @Override
    public Collection<Property<?, ?>> getProperties(int x, int y, int z) {
        return delegate.getProperties(x, y, z);
    }

    @Override
    public Collection<Direction> getFacesWithProperty(int x, int y, int z, Class<? extends Property<?, ?>> propertyClass) {
        return delegate.getFacesWithProperty(x, y, z, propertyClass);
    }

    @Override
    public Weather getWeather() {
        return delegate.getWeather();
    }

    @Override
    public long getRemainingDuration() {
        return delegate.getRemainingDuration();
    }

    @Override
    public long getRunningDuration() {
        return delegate.getRunningDuration();
    }

    @Override
    public void setWeather(Weather weather) {
        delegate.setWeather(weather);
    }

    @Override
    public void setWeather(Weather weather, long duration) {
        delegate.setWeather(weather, duration);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        delegate.spawnParticles(particleEffect, position);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        delegate.spawnParticles(particleEffect, position, radius);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume) {
        delegate.playSound(sound, category, position, volume);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch) {
        delegate.playSound(sound, category, position, volume, pitch);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch, double minVolume) {
        delegate.playSound(sound, category, position, volume, pitch, minVolume);
    }

    @Override
    public void stopSounds() {
        delegate.stopSounds();
    }

    @Override
    public void stopSounds(SoundType sound) {
        delegate.stopSounds(sound);
    }

    @Override
    public void stopSounds(SoundCategory category) {
        delegate.stopSounds(category);
    }

    @Override
    public void stopSounds(SoundType sound, SoundCategory category) {
        delegate.stopSounds(sound, category);
    }

    @Override
    public void playRecord(Vector3i position, RecordType recordType) {
        delegate.playRecord(position, recordType);
    }

    @Override
    public void stopRecord(Vector3i position) {
        delegate.stopRecord(position);
    }

    @Override
    public void sendTitle(Title title) {
        delegate.sendTitle(title);
    }

    @Override
    public void sendBookView(BookView bookView) {
        delegate.sendBookView(bookView);
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
        delegate.sendBlockChange(x, y, z, state);
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
        delegate.resetBlockChange(x, y, z);
    }

    @Override
    public Context getContext() {
        return delegate.getContext();
    }

    @Override
    public void sendMessage(Text message) {
        delegate.sendMessage(message);
    }

    @Override
    public MessageChannel getMessageChannel() {
        return delegate.getMessageChannel();
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        delegate.setMessageChannel(channel);
    }

    @Override
    public void sendMessage(ChatType type, Text message) {
        delegate.sendMessage(type, message);
    }
}
