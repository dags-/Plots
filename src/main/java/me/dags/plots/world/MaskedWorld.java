package me.dags.plots.world;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.plot.PlotMask;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class MaskedWorld extends DelegateWorld {

    private final World world;
    private final PlotMask mask;

    public MaskedWorld(World world, PlotMask mask) {
        super(world);
        this.world = world;
        this.mask = mask;
    }

    private boolean contains(int x, int y, int z) {
        return mask.contains(x, y, z);
    }

    private boolean contains(Number x, Number y, Number z) {
        return contains(x.intValue(), y.intValue(), z.intValue());
    }

    private boolean contains(Vector3i pos) {
        return mask.contains(pos);
    }

    private boolean contains(Vector3d pos) {
        return mask.contains(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ());
    }

    @Override
    public boolean restoreSnapshot(BlockSnapshot snapshot, boolean force, BlockChangeFlag flag) {
        return contains(snapshot.getPosition()) && world.restoreSnapshot(snapshot, force, flag);
    }

    @Override
    public boolean restoreSnapshot(int x, int y, int z, BlockSnapshot snapshot, boolean force, BlockChangeFlag flag) {
        return contains(x, y, z) && world.restoreSnapshot(x, y, z, snapshot, force, flag);
    }

    @Override
    public void setCreator(int x, int y, int z, @Nullable UUID uuid) {
        if (contains(x, y, z)) {
            world.setCreator(x, y, z, uuid);
        }
    }

    @Override
    public void setNotifier(int x, int y, int z, @Nullable UUID uuid) {
        if (contains(x, y, z)) {
            world.setNotifier(x, y, z, uuid);
        }
    }

    @Override
    public Entity createEntity(EntityType type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        if (contains(position)) {
            return world.createEntity(type, position);
        }
        throw new IllegalArgumentException("Cannot create entity at: " + position);
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer) {
        Optional<Number> x = entityContainer.get(Queries.POSITION_X).map(o -> (Number) o);
        Optional<Number> y = entityContainer.get(Queries.POSITION_Y).map(o -> (Number) o);
        Optional<Number> z = entityContainer.get(Queries.POSITION_Z).map(o -> (Number) o);
        if (x.isPresent() && y.isPresent() && z.isPresent() && contains(x.get(), y.get(), x.get())) {
            return world.createEntity(entityContainer);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer, Vector3d position) {
        if (contains(position)) {
            return world.createEntity(entityContainer, position);
        }
        return Optional.empty();
    }

    @Override
    public Entity createEntityNaturally(EntityType type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        if (contains(position)) {
            return world.createEntityNaturally(type, position);
        }
        throw new IllegalArgumentException("Cannot create entity at: " + position);
    }

    @Override
    public Optional<Entity> restoreSnapshot(EntitySnapshot snapshot, Vector3d position) {
        if (contains(position)) {
            return world.restoreSnapshot(snapshot, position);
        }
        return Optional.empty();
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        if (contains(entity.getLocation().getPosition())) {
            return world.spawnEntity(entity);
        }
        return false;
    }

    @Override
    public Collection<Entity> spawnEntities(Iterable<? extends Entity> entities) {
        List<Entity> spawnable = Collections.emptyList();
        for (Entity entity : entities) {
            if (contains(entity.getLocation().getPosition())) {
                if (spawnable.isEmpty()) {
                    spawnable = new LinkedList<>();
                }
                spawnable.add(entity);
            }
        }
        return world.spawnEntities(spawnable);
    }

    @Override
    public boolean hitBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return contains(x, y, z) && world.hitBlock(x, y, z, side, profile);
    }

    @Override
    public boolean interactBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return contains(x, y, z) && world.interactBlock(x, y, z, side, profile);
    }

    @Override
    public boolean interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side, GameProfile profile) {
        return contains(x, y, z) && world.interactBlockWith(x, y, z, itemStack, side, profile);
    }

    @Override
    public boolean placeBlock(int x, int y, int z, BlockState block, Direction side, GameProfile profile) {
        return contains(x, y, z) && world.placeBlock(x, y, z, block, side, profile);
    }

    @Override
    public boolean digBlock(int x, int y, int z, GameProfile profile) {
        return contains(x, y, z) && world.digBlock(x, y, z, profile);
    }

    @Override
    public boolean digBlockWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return contains(x, y, z) && world.digBlockWith(x, y, z, itemStack, profile);
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        return world.getBlockCopy(type);
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(int x, int y, int z, Class<T> manipulatorClass) {
        if (contains(x, y, z)) {
            return world.getOrCreate(x, y, z, manipulatorClass);
        }
        return get(x, y, z, manipulatorClass);
    }

    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, Key<? extends BaseValue<E>> key, E value) {
        if (contains(x, y, z)) {
            return world.offer(x, y, z, key, value);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult offer(int x, int y, int z, DataManipulator<?, ?> manipulator, MergeFunction function) {
        if (contains(x, y, z)) {
            return world.offer(x, y, z, manipulator, function);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        if (contains(x, y, z)) {
            return world.remove(x, y, z, manipulatorClass);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Key<?> key) {
        if (contains(x, y, z)) {
            return world.remove(x, y, z, key);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult undo(int x, int y, int z, DataTransactionResult result) {
        if (contains(x, y, z)) {
            return world.undo(x, y, z, result);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from) {
        if (contains(xTo, yTo, zTo)) {
            return world.copyFrom(xTo, yTo, zTo, from);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from, MergeFunction function) {
        if (contains(xTo, yTo, zTo)) {
            return world.copyFrom(xTo, yTo, zTo, from, function);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom, MergeFunction function) {
        if (contains(xTo, yTo, zTo)) {
            return world.copyFrom(xTo, yTo, zTo, xFrom, yFrom, zFrom, function);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block) {
        return contains(x, y, z) && world.setBlock(x, y, z, block);
    }

    @Override
    public void setRawData(int x, int y, int z, DataView container) throws InvalidDataException {
        if (contains(x, y, z)) {
            world.setRawData(x, y, z, container);
        }
    }

    @Override
    public void setBiome(int x, int y, int z, BiomeType biome) {
        if (contains(x, y, z)) {
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
}
