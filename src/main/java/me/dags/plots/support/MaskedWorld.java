package me.dags.plots.support;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.plot.PlotMask;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.*;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.extent.*;
import org.spongepowered.api.world.extent.worker.MutableBiomeVolumeWorker;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.weather.Weather;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class MaskedWorld implements World {

    private final World world;
    private final PlotMask mask;

    public MaskedWorld(World world, PlotMask mask) {
        this.world = world;
        this.mask = mask;
    }

    @Override
    public Collection<Player> getPlayers() {
        return world.getPlayers();
    }

    @Override
    public Location<World> getLocation(Vector3i position) {
        return world.getLocation(position);
    }

    @Override
    public Location<World> getLocation(int x, int y, int z) {
        return world.getLocation(x, y, z);
    }

    @Override
    public Location<World> getLocation(Vector3d position) {
        return world.getLocation(position);
    }

    @Override
    public Location<World> getLocation(double x, double y, double z) {
        return world.getLocation(x, y, z);
    }

    @Override
    public LocatableBlock getLocatableBlock(Vector3i position) {
        return world.getLocatableBlock(position);
    }

    @Override
    public LocatableBlock getLocatableBlock(int x, int y, int z) {
        return world.getLocatableBlock(x, y, z);
    }

    @Override
    public Optional<Chunk> getChunkAtBlock(Vector3i blockPosition) {
        return world.getChunkAtBlock(blockPosition);
    }

    @Override
    public Optional<Chunk> getChunkAtBlock(int bx, int by, int bz) {
        return world.getChunkAtBlock(bx, by, bz);
    }

    @Override
    public Optional<Chunk> getChunk(Vector3i chunkPosition) {
        return world.getChunk(chunkPosition);
    }

    @Override
    public Optional<Chunk> getChunk(int cx, int cy, int cz) {
        return world.getChunk(cx, cy, cz);
    }

    @Override
    public Optional<Chunk> loadChunk(Vector3i chunkPosition, boolean shouldGenerate) {
        return world.loadChunk(chunkPosition, shouldGenerate);
    }

    @Override
    public Optional<Chunk> loadChunk(int cx, int cy, int cz, boolean shouldGenerate) {
        return world.loadChunk(cx, cy, cz, shouldGenerate);
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        return world.unloadChunk(chunk);
    }

    @Override
    public Iterable<Chunk> getLoadedChunks() {
        return world.getLoadedChunks();
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        return world.getEntity(uuid);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return world.getWorldBorder();
    }

    @Override
    public WorldBorder.ChunkPreGenerate newChunkPreGenerate(Vector3d center, double diameter) {
        return world.newChunkPreGenerate(center, diameter);
    }

    @Override
    public Dimension getDimension() {
        return world.getDimension();
    }

    @Override
    public WorldGenerator getWorldGenerator() {
        return world.getWorldGenerator();
    }

    @Override
    public WorldProperties getProperties() {
        return world.getProperties();
    }

    @Override
    public Path getDirectory() {
        return world.getDirectory();
    }

    @Override
    public UUID getUniqueId() {
        return world.getUniqueId();
    }

    @Override
    public String getName() {
        return world.getName();
    }

    @Override
    public Difficulty getDifficulty() {
        return world.getDifficulty();
    }

    @Override
    public Optional<String> getGameRule(String gameRule) {
        return world.getGameRule(gameRule);
    }

    @Override
    public Map<String, String> getGameRules() {
        return world.getGameRules();
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return world.doesKeepSpawnLoaded();
    }

    @Override
    public void setKeepSpawnLoaded(boolean keepLoaded) {
        world.setKeepSpawnLoaded(keepLoaded);
    }

    @Override
    public Location<World> getSpawnLocation() {
        return world.getSpawnLocation();
    }

    @Override
    public SerializationBehavior getSerializationBehavior() {
        return world.getSerializationBehavior();
    }

    @Override
    public void setSerializationBehavior(SerializationBehavior behavior) {
        world.setSerializationBehavior(behavior);
    }

    @Override
    public WorldStorage getWorldStorage() {
        return world.getWorldStorage();
    }

    @Override
    public void triggerExplosion(Explosion explosion, Cause cause) {
        world.triggerExplosion(explosion, cause);
    }

    @Override
    public PortalAgent getPortalAgent() {
        return world.getPortalAgent();
    }

    @Override
    public MutableBiomeVolumeWorker<World> getBiomeWorker() {
        return world.getBiomeWorker();
    }

    @Override
    public MutableBlockVolumeWorker<World> getBlockWorker(Cause cause) {
        return world.getBlockWorker(cause);
    }

    @Override
    public boolean save() throws IOException {
        return world.save();
    }

    @Override
    public boolean setBlock(Vector3i position, BlockState blockState, BlockChangeFlag flag, Cause cause) {
        return mask.contains(position) && world.setBlock(position, blockState, flag, cause);
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState blockState, BlockChangeFlag flag, Cause cause) {
        return mask.contains(x, y, z) && world.setBlock(x, y, z, blockState, flag, cause);
    }

    @Override
    public boolean setBlockType(Vector3i position, BlockType type, BlockChangeFlag flag, Cause cause) {
        return mask.contains(position) && world.setBlockType(position, type, flag, cause);
    }

    @Override
    public boolean setBlockType(int x, int y, int z, BlockType type, BlockChangeFlag flag, Cause cause) {
        return mask.contains(x, y, z) && world.setBlockType(x, y, z, type, flag, cause);
    }

    @Override
    public BlockSnapshot createSnapshot(Vector3i position) {
        return world.createSnapshot(position);
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        return world.createSnapshot(x, y, z);
    }

    @Override
    public boolean restoreSnapshot(BlockSnapshot snapshot, boolean force, BlockChangeFlag flag, Cause cause) {
        return world.restoreSnapshot(snapshot, force, flag, cause);
    }

    @Override
    public boolean restoreSnapshot(Vector3i position, BlockSnapshot snapshot, boolean force, BlockChangeFlag flag, Cause cause) {
        return world.restoreSnapshot(position, snapshot, force, flag, cause);
    }

    @Override
    public boolean restoreSnapshot(int x, int y, int z, BlockSnapshot snapshot, boolean force, BlockChangeFlag flag, Cause cause) {
        return world.restoreSnapshot(x, y, z, snapshot, force, flag, cause);
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(Vector3i position) {
        return world.getScheduledUpdates(position);
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(int x, int y, int z) {
        return world.getScheduledUpdates(x, y, z);
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(Vector3i position, int priority, int ticks) {
        return world.addScheduledUpdate(position, priority, ticks);
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        return world.addScheduledUpdate(x, y, z, priority, ticks);
    }

    @Override
    public void removeScheduledUpdate(Vector3i position, ScheduledBlockUpdate update) {
        world.removeScheduledUpdate(position, update);
    }

    @Override
    public void removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
        world.removeScheduledUpdate(x, y, z, update);
    }

    @Override
    public boolean isLoaded() {
        return world.isLoaded();
    }

    @Override
    public Extent getExtentView(Vector3i newMin, Vector3i newMax) {
        return world.getExtentView(newMin, newMax);
    }

    @Override
    public Optional<UUID> getCreator(Vector3i pos) {
        return world.getCreator(pos);
    }

    @Override
    public Optional<UUID> getCreator(int x, int y, int z) {
        return world.getCreator(x, y, z);
    }

    @Override
    public Optional<UUID> getNotifier(Vector3i pos) {
        return world.getNotifier(pos);
    }

    @Override
    public Optional<UUID> getNotifier(int x, int y, int z) {
        return world.getNotifier(x, y, z);
    }

    @Override
    public void setCreator(Vector3i pos, @Nullable UUID uuid) {
        world.setCreator(pos, uuid);
    }

    @Override
    public void setCreator(int x, int y, int z, @Nullable UUID uuid) {
        world.setCreator(x, y, z, uuid);
    }

    @Override
    public void setNotifier(Vector3i pos, @Nullable UUID uuid) {
        world.setNotifier(pos, uuid);
    }

    @Override
    public void setNotifier(int x, int y, int z, @Nullable UUID uuid) {
        world.setNotifier(x, y, z, uuid);
    }

    @Override
    public Optional<AABB> getBlockSelectionBox(Vector3i pos) {
        return world.getBlockSelectionBox(pos);
    }

    @Override
    public Optional<AABB> getBlockSelectionBox(int x, int y, int z) {
        return world.getBlockSelectionBox(x, y, z);
    }

    @Override
    public Set<AABB> getIntersectingBlockCollisionBoxes(AABB box) {
        return world.getIntersectingBlockCollisionBoxes(box);
    }

    @Override
    public Set<AABB> getIntersectingCollisionBoxes(Entity owner) {
        return world.getIntersectingCollisionBoxes(owner);
    }

    @Override
    public Set<AABB> getIntersectingCollisionBoxes(Entity owner, AABB box) {
        return world.getIntersectingCollisionBoxes(owner, box);
    }

    @Override
    public ArchetypeVolume createArchetypeVolume(Vector3i min, Vector3i max, Vector3i origin) {
        return world.createArchetypeVolume(min, max, origin);
    }

    @Override
    public Collection<Entity> getEntities() {
        return world.getEntities();
    }

    @Override
    public Collection<Entity> getEntities(Predicate<Entity> filter) {
        return world.getEntities(filter);
    }

    @Override
    public Entity createEntity(EntityType type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return world.createEntity(type, position);
    }

    @Override
    public Entity createEntity(EntityType type, Vector3i position) throws IllegalArgumentException, IllegalStateException {
        return world.createEntity(type, position);
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer) {
        return world.createEntity(entityContainer);
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer, Vector3d position) {
        return world.createEntity(entityContainer, position);
    }

    @Override
    public Optional<Entity> restoreSnapshot(EntitySnapshot snapshot, Vector3d position) {
        return world.restoreSnapshot(snapshot, position);
    }

    @Override
    public boolean spawnEntity(Entity entity, Cause cause) {
        return world.spawnEntity(entity, cause);
    }

    @Override
    public boolean spawnEntities(Iterable<? extends Entity> entities, Cause cause) {
        return world.spawnEntities(entities, cause);
    }

    @Override
    public Set<Entity> getIntersectingEntities(AABB box) {
        return world.getIntersectingEntities(box);
    }

    @Override
    public Set<Entity> getIntersectingEntities(AABB box, Predicate<Entity> filter) {
        return world.getIntersectingEntities(box, filter);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d end) {
        return world.getIntersectingEntities(start, end);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d end, Predicate<EntityHit> filter) {
        return world.getIntersectingEntities(start, end, filter);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Entity looker, double distance) {
        return world.getIntersectingEntities(looker, distance);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Entity looker, double distance, Predicate<EntityHit> filter) {
        return world.getIntersectingEntities(looker, distance, filter);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d direction, double distance) {
        return world.getIntersectingEntities(start, direction, distance);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d direction, double distance, Predicate<EntityHit> filter) {
        return world.getIntersectingEntities(start, direction, distance, filter);
    }

    @Override
    public Collection<TileEntity> getTileEntities() {
        return world.getTileEntities();
    }

    @Override
    public Collection<TileEntity> getTileEntities(Predicate<TileEntity> filter) {
        return world.getTileEntities(filter);
    }

    @Override
    public Optional<TileEntity> getTileEntity(Vector3i position) {
        return world.getTileEntity(position);
    }

    @Override
    public Optional<TileEntity> getTileEntity(int x, int y, int z) {
        return world.getTileEntity(x, y, z);
    }

    @Override
    public boolean setBlock(Vector3i position, BlockState block, Cause cause) {
        return mask.contains(position) && world.setBlock(position, block, cause);
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block, Cause cause) {
        return mask.contains(x, y, z) && world.setBlock(x, y, z, block, cause);
    }

    @Override
    public boolean setBlockType(Vector3i position, BlockType type, Cause cause) {
        return mask.contains(position) && world.setBlockType(position, type, cause);
    }

    @Override
    public boolean setBlockType(int x, int y, int z, BlockType type, Cause cause) {
        return mask.contains(x, y, z) && world.setBlockType(x, y, z, type, cause);
    }

    @Override
    public MutableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        return world.getBlockView(newMin, newMax);
    }

    @Override
    public MutableBlockVolume getBlockView(DiscreteTransform3 transform) {
        return world.getBlockView(transform);
    }

    @Override
    public MutableBlockVolume getRelativeBlockView() {
        return world.getRelativeBlockView();
    }

    @Override
    public Vector3i getBlockMin() {
        return world.getBlockMin();
    }

    @Override
    public Vector3i getBlockMax() {
        return world.getBlockMax();
    }

    @Override
    public Vector3i getBlockSize() {
        return world.getBlockSize();
    }

    @Override
    public boolean containsBlock(Vector3i position) {
        return world.containsBlock(position);
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return world.containsBlock(x, y, z);
    }

    @Override
    public BlockState getBlock(Vector3i position) {
        return world.getBlock(position);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return world.getBlock(x, y, z);
    }

    @Override
    public BlockType getBlockType(Vector3i position) {
        return world.getBlockType(position);
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        return world.getBlockType(x, y, z);
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        return world.getUnmodifiableBlockView();
    }

    @Override
    public MutableBlockVolume getBlockCopy() {
        return world.getBlockCopy();
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        return world.getBlockCopy(type);
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return world.getImmutableBlockCopy();
    }

    @Override
    public boolean hitBlock(Vector3i position, Direction side, Cause cause) {
        return mask.contains(position) && world.hitBlock(position, side, cause);
    }

    @Override
    public boolean hitBlock(int x, int y, int z, Direction side, Cause cause) {
        return mask.contains(x, y, z) && world.hitBlock(x, y, z, side, cause);
    }

    @Override
    public boolean interactBlock(Vector3i position, Direction side, Cause cause) {
        return mask.contains(position) && world.interactBlock(position, side, cause);
    }

    @Override
    public boolean interactBlock(int x, int y, int z, Direction side, Cause cause) {
        return mask.contains(x, y, z) && world.interactBlock(x, y, z, side, cause);
    }

    @Override
    public boolean interactBlockWith(Vector3i position, ItemStack itemStack, Direction side, Cause cause) {
        return mask.contains(position) && world.interactBlockWith(position, itemStack, side, cause);
    }

    @Override
    public boolean interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side, Cause cause) {
        return mask.contains(x, y, z) && world.interactBlockWith(x, y, z, itemStack, side, cause);
    }

    @Override
    public boolean placeBlock(Vector3i position, BlockState block, Direction side, Cause cause) {
        return mask.contains(position) && world.placeBlock(position, block, side, cause);
    }

    @Override
    public boolean placeBlock(int x, int y, int z, BlockState block, Direction side, Cause cause) {
        return mask.contains(x, y, z) && world.placeBlock(x, y, z, block, side, cause);
    }

    @Override
    public boolean digBlock(Vector3i position, Cause cause) {
        return mask.contains(position) && world.digBlock(position, cause);
    }

    @Override
    public boolean digBlock(int x, int y, int z, Cause cause) {
        return mask.contains(x, y, z) && world.digBlock(x, y, z, cause);
    }

    @Override
    public boolean digBlockWith(Vector3i position, ItemStack itemStack, Cause cause) {
        return mask.contains(position) && world.digBlockWith(position, itemStack, cause);
    }

    @Override
    public boolean digBlockWith(int x, int y, int z, ItemStack itemStack, Cause cause) {
        return mask.contains(x, y, z) && world.digBlockWith(x, y, z, itemStack, cause);
    }

    @Override
    public int getBlockDigTimeWith(Vector3i position, ItemStack itemStack, Cause cause) {
        return world.getBlockDigTimeWith(position, itemStack, cause);
    }

    @Override
    public int getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack, Cause cause) {
        return world.getBlockDigTimeWith(x, y, z, itemStack, cause);
    }

    @Override
    public void setBiome(Vector3i position, BiomeType biome) {
        if (mask.contains(position)) {
            world.setBiome(position, biome);
        }
    }

    @Override
    public void setBiome(int x, int y, int z, BiomeType biome) {
        if (mask.contains(x, y, z)) {
            world.setBiome(x, y, z, biome);
        }
    }

    @Override
    public MutableBiomeVolume getBiomeView(Vector3i newMin, Vector3i newMax) {
        return world.getBiomeView(newMin, newMax);
    }

    @Override
    public MutableBiomeVolume getBiomeView(DiscreteTransform3 transform) {
        return world.getBiomeView(transform);
    }

    @Override
    public MutableBiomeVolume getRelativeBiomeView() {
        return world.getRelativeBiomeView();
    }

    @Override
    public Vector3i getBiomeMin() {
        return world.getBiomeMin();
    }

    @Override
    public Vector3i getBiomeMax() {
        return world.getBiomeMax();
    }

    @Override
    public Vector3i getBiomeSize() {
        return world.getBiomeSize();
    }

    @Override
    public boolean containsBiome(Vector3i position) {
        return world.containsBiome(position);
    }

    @Override
    public boolean containsBiome(int x, int y, int z) {
        return world.containsBiome(x, y, z);
    }

    @Override
    public BiomeType getBiome(Vector3i position) {
        return world.getBiome(position);
    }

    @Override
    public BiomeType getBiome(int x, int y, int z) {
        return world.getBiome(x, y, z);
    }

    @Override
    public UnmodifiableBiomeVolume getUnmodifiableBiomeView() {
        return world.getUnmodifiableBiomeView();
    }

    @Override
    public MutableBiomeVolume getBiomeCopy() {
        return world.getBiomeCopy();
    }

    @Override
    public MutableBiomeVolume getBiomeCopy(StorageType type) {
        return world.getBiomeCopy(type);
    }

    @Override
    public ImmutableBiomeVolume getImmutableBiomeCopy() {
        return world.getImmutableBiomeCopy();
    }

    @Override
    public <E> Optional<E> get(Vector3i coordinates, Key<? extends BaseValue<E>> key) {
        return world.get(coordinates, key);
    }

    @Override
    public <E> Optional<E> get(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        return world.get(x, y, z, key);
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Vector3i coordinates, Class<T> manipulatorClass) {
        return world.get(coordinates, manipulatorClass);
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(int x, int y, int z, Class<T> manipulatorClass) {
        return world.get(x, y, z, manipulatorClass);
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Vector3i coordinates, Class<T> manipulatorClass) {
        return world.getOrCreate(coordinates, manipulatorClass);
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(int x, int y, int z, Class<T> manipulatorClass) {
        return world.getOrCreate(x, y, z, manipulatorClass);
    }

    @Override
    @Nullable
    public <E> E getOrNull(Vector3i coordinates, Key<? extends BaseValue<E>> key) {
        return world.getOrNull(coordinates, key);
    }

    @Override
    @Nullable
    public <E> E getOrNull(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        return world.getOrNull(x, y, z, key);
    }

    @Override
    public <E> E getOrElse(Vector3i coordinates, Key<? extends BaseValue<E>> key, E defaultValue) {
        return world.getOrElse(coordinates, key, defaultValue);
    }

    @Override
    public <E> E getOrElse(int x, int y, int z, Key<? extends BaseValue<E>> key, E defaultValue) {
        return world.getOrElse(x, y, z, key, defaultValue);
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Vector3i coordinates, Key<V> key) {
        return world.getValue(coordinates, key);
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(int x, int y, int z, Key<V> key) {
        return world.getValue(x, y, z, key);
    }

    @Override
    public boolean supports(Vector3i coordinates, Key<?> key) {
        return world.supports(coordinates, key);
    }

    @Override
    public boolean supports(int x, int y, int z, Key<?> key) {
        return world.supports(x, y, z, key);
    }

    @Override
    public boolean supports(Vector3i coordinates, BaseValue<?> value) {
        return world.supports(coordinates, value);
    }

    @Override
    public boolean supports(int x, int y, int z, BaseValue<?> value) {
        return world.supports(x, y, z, value);
    }

    @Override
    public boolean supports(Vector3i coordinates, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return world.supports(coordinates, manipulatorClass);
    }

    @Override
    public boolean supports(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return world.supports(x, y, z, manipulatorClass);
    }

    @Override
    public boolean supports(Vector3i coordinates, DataManipulator<?, ?> manipulator) {
        return world.supports(coordinates, manipulator);
    }

    @Override
    public boolean supports(int x, int y, int z, DataManipulator<?, ?> manipulator) {
        return world.supports(x, y, z, manipulator);
    }

    @Override
    public Set<Key<?>> getKeys(Vector3i coordinates) {
        return world.getKeys(coordinates);
    }

    @Override
    public Set<Key<?>> getKeys(int x, int y, int z) {
        return world.getKeys(x, y, z);
    }

    @Override
    public Set<ImmutableValue<?>> getValues(Vector3i coordinates) {
        return world.getValues(coordinates);
    }

    @Override
    public Set<ImmutableValue<?>> getValues(int x, int y, int z) {
        return world.getValues(x, y, z);
    }

    @Override
    public <E> DataTransactionResult transform(Vector3i coordinates, Key<? extends BaseValue<E>> key, Function<E, E> function) {
        return world.transform(coordinates, key, function);
    }

    @Override
    public <E> DataTransactionResult transform(int x, int y, int z, Key<? extends BaseValue<E>> key, Function<E, E> function) {
        return world.transform(x, y, z, key, function);
    }

    @Override
    public <E> DataTransactionResult offer(Vector3i coordinates, Key<? extends BaseValue<E>> key, E value) {
        return world.offer(coordinates, key, value);
    }

    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, Key<? extends BaseValue<E>> key, E value) {
        return world.offer(x, y, z, key, value);
    }

    @Override
    public <E> DataTransactionResult offer(Vector3i coordinates, Key<? extends BaseValue<E>> key, E value, Cause cause) {
        return world.offer(coordinates, key, value, cause);
    }

    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, Key<? extends BaseValue<E>> key, E value, Cause cause) {
        return world.offer(x, y, z, key, value, cause);
    }

    @Override
    public <E> DataTransactionResult offer(Vector3i coordinates, BaseValue<E> value) {
        return world.offer(coordinates, value);
    }

    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, BaseValue<E> value) {
        return world.offer(x, y, z, value);
    }

    @Override
    public <E> DataTransactionResult offer(Vector3i coordinates, BaseValue<E> value, Cause cause) {
        return world.offer(coordinates, value, cause);
    }

    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, BaseValue<E> value, Cause cause) {
        return world.offer(x, y, z, value, cause);
    }

    @Override
    public DataTransactionResult offer(Vector3i coordinates, DataManipulator<?, ?> manipulator) {
        return world.offer(coordinates, manipulator);
    }

    @Override
    public DataTransactionResult offer(int x, int y, int z, DataManipulator<?, ?> manipulator) {
        return world.offer(x, y, z, manipulator);
    }

    @Override
    public DataTransactionResult offer(Vector3i coordinates, DataManipulator<?, ?> manipulator, Cause cause) {
        return world.offer(coordinates, manipulator, cause);
    }

    @Override
    public DataTransactionResult offer(int x, int y, int z, DataManipulator<?, ?> manipulator, Cause cause) {
        return world.offer(x, y, z, manipulator, cause);
    }

    @Override
    public DataTransactionResult offer(Vector3i coordinates, DataManipulator<?, ?> manipulator, MergeFunction function) {
        return world.offer(coordinates, manipulator, function);
    }

    @Override
    public DataTransactionResult offer(int x, int y, int z, DataManipulator<?, ?> manipulator, MergeFunction function) {
        return world.offer(x, y, z, manipulator, function);
    }

    @Override
    public DataTransactionResult offer(Vector3i coordinates, DataManipulator<?, ?> manipulator, MergeFunction function, Cause cause) {
        return world.offer(coordinates, manipulator, function, cause);
    }

    @Override
    public DataTransactionResult offer(int x, int y, int z, DataManipulator<?, ?> manipulator, MergeFunction function, Cause cause) {
        return world.offer(x, y, z, manipulator, function, cause);
    }

    @Override
    public DataTransactionResult offer(Vector3i coordinates, Iterable<DataManipulator<?, ?>> manipulators) {
        return world.offer(coordinates, manipulators);
    }

    @Override
    public DataTransactionResult offer(int x, int y, int z, Iterable<DataManipulator<?, ?>> manipulators) {
        return world.offer(x, y, z, manipulators);
    }

    @Override
    public DataTransactionResult offer(Vector3i blockPosition, Iterable<DataManipulator<?, ?>> values, MergeFunction function) {
        return world.offer(blockPosition, values, function);
    }

    @Override
    public DataTransactionResult remove(Vector3i coordinates, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return world.remove(coordinates, manipulatorClass);
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return world.remove(x, y, z, manipulatorClass);
    }

    @Override
    public DataTransactionResult remove(Vector3i coordinates, Key<?> key) {
        return world.remove(coordinates, key);
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Key<?> key) {
        return world.remove(x, y, z, key);
    }

    @Override
    public DataTransactionResult undo(Vector3i coordinates, DataTransactionResult result) {
        return world.undo(coordinates, result);
    }

    @Override
    public DataTransactionResult undo(int x, int y, int z, DataTransactionResult result) {
        return world.undo(x, y, z, result);
    }

    @Override
    public DataTransactionResult copyFrom(Vector3i to, DataHolder from) {
        return world.copyFrom(to, from);
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from) {
        return world.copyFrom(xTo, yTo, zTo, from);
    }

    @Override
    public DataTransactionResult copyFrom(Vector3i coordinatesTo, Vector3i coordinatesFrom) {
        return world.copyFrom(coordinatesTo, coordinatesFrom);
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom) {
        return world.copyFrom(xTo, yTo, zTo, xFrom, yFrom, zFrom);
    }

    @Override
    public DataTransactionResult copyFrom(Vector3i to, DataHolder from, MergeFunction function) {
        return world.copyFrom(to, from, function);
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from, MergeFunction function) {
        return world.copyFrom(xTo, yTo, zTo, from, function);
    }

    @Override
    public DataTransactionResult copyFrom(Vector3i coordinatesTo, Vector3i coordinatesFrom, MergeFunction function) {
        return world.copyFrom(coordinatesTo, coordinatesFrom, function);
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom, MergeFunction function) {
        return world.copyFrom(xTo, yTo, zTo, xFrom, yFrom, zFrom, function);
    }

    @Override
    public Collection<DataManipulator<?, ?>> getManipulators(Vector3i coordinates) {
        return world.getManipulators(coordinates);
    }

    @Override
    public Collection<DataManipulator<?, ?>> getManipulators(int x, int y, int z) {
        return world.getManipulators(x, y, z);
    }

    @Override
    public boolean validateRawData(Vector3i position, DataView container) {
        return world.validateRawData(position, container);
    }

    @Override
    public boolean validateRawData(int x, int y, int z, DataView container) {
        return world.validateRawData(x, y, z, container);
    }

    @Override
    public void setRawData(Vector3i position, DataView container) throws InvalidDataException {
        world.setRawData(position, container);
    }

    @Override
    public void setRawData(int x, int y, int z, DataView container) throws InvalidDataException {
        world.setRawData(x, y, z, container);
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Vector3i coords, Class<T> propertyClass) {
        return world.getProperty(coords, propertyClass);
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(int x, int y, int z, Class<T> propertyClass) {
        return world.getProperty(x, y, z, propertyClass);
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Vector3i coords, Direction direction, Class<T> propertyClass) {
        return world.getProperty(coords, direction, propertyClass);
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(int x, int y, int z, Direction direction, Class<T> propertyClass) {
        return world.getProperty(x, y, z, direction, propertyClass);
    }

    @Override
    public Collection<Property<?, ?>> getProperties(Vector3i coords) {
        return world.getProperties(coords);
    }

    @Override
    public Collection<Property<?, ?>> getProperties(int x, int y, int z) {
        return world.getProperties(x, y, z);
    }

    @Override
    public Collection<Direction> getFacesWithProperty(Vector3i coords, Class<? extends Property<?, ?>> propertyClass) {
        return world.getFacesWithProperty(coords, propertyClass);
    }

    @Override
    public Collection<Direction> getFacesWithProperty(int x, int y, int z, Class<? extends Property<?, ?>> propertyClass) {
        return world.getFacesWithProperty(x, y, z, propertyClass);
    }

    @Override
    public Weather getWeather() {
        return world.getWeather();
    }

    @Override
    public long getRemainingDuration() {
        return world.getRemainingDuration();
    }

    @Override
    public long getRunningDuration() {
        return world.getRunningDuration();
    }

    @Override
    public void setWeather(Weather weather) {
        world.setWeather(weather);
    }

    @Override
    public void setWeather(Weather weather, long duration) {
        world.setWeather(weather, duration);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        world.spawnParticles(particleEffect, position);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        world.spawnParticles(particleEffect, position, radius);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume) {
        world.playSound(sound, position, volume);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume) {
        world.playSound(sound, category, position, volume);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch) {
        world.playSound(sound, position, volume, pitch);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch) {
        world.playSound(sound, category, position, volume, pitch);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch, double minVolume) {
        world.playSound(sound, position, volume, pitch, minVolume);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch, double minVolume) {
        world.playSound(sound, category, position, volume, pitch, minVolume);
    }

    @Override
    public void sendTitle(Title title) {
        world.sendTitle(title);
    }

    @Override
    public void resetTitle() {
        world.resetTitle();
    }

    @Override
    public void clearTitle() {
        world.clearTitle();
    }

    @Override
    public void sendBookView(BookView bookView) {
        world.sendBookView(bookView);
    }

    @Override
    public void sendBlockChange(Vector3i vec, BlockState state) {
        world.sendBlockChange(vec, state);
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
        world.sendBlockChange(x, y, z, state);
    }

    @Override
    public void resetBlockChange(Vector3i vec) {
        world.resetBlockChange(vec);
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
        world.resetBlockChange(x, y, z);
    }

    @Override
    public Context getContext() {
        return world.getContext();
    }

    @Override
    public void sendMessage(Text message) {
        world.sendMessage(message);
    }

    @Override
    public void sendMessage(TextTemplate template) {
        world.sendMessage(template);
    }

    @Override
    public void sendMessage(TextTemplate template, Map<String, TextElement> parameters) {
        world.sendMessage(template, parameters);
    }

    @Override
    public void sendMessages(Text... messages) {
        world.sendMessages(messages);
    }

    @Override
    public void sendMessages(Iterable<Text> messages) {
        world.sendMessages(messages);
    }

    @Override
    public MessageChannel getMessageChannel() {
        return world.getMessageChannel();
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        world.setMessageChannel(channel);
    }

    @Override
    public void sendMessage(ChatType type, Text message) {
        world.sendMessage(type, message);
    }

    @Override
    public void sendMessage(ChatType type, TextTemplate template) {
        world.sendMessage(type, template);
    }

    @Override
    public void sendMessage(ChatType type, TextTemplate template, Map<String, TextElement> parameters) {
        world.sendMessage(type, template, parameters);
    }

    @Override
    public void sendMessages(ChatType type, Text... messages) {
        world.sendMessages(type, messages);
    }

    @Override
    public void sendMessages(ChatType type, Iterable<Text> messages) {
        world.sendMessages(type, messages);
    }
}
