package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public class CopyBlockOperation extends AbstractBlockOperation {

    private final BlockVolume from;
    private final MutableBlockVolume to;

    private final Vector3i fromMin, toMin;

    public CopyBlockOperation(BlockVolume from, MutableBlockVolume to) {
        super(from.getBlockMin(), from.getBlockMax());
        if (!from.getBlockSize().equals(to.getBlockSize())) {
            throw new UnsupportedOperationException("Volumes must be equal sizes!");
        }
        this.from = from;
        this.to = to;
        this.fromMin = from.getBlockMin();
        this.toMin = to.getBlockMin();
    }

    @Override
    void processAt(int x, int y, int z) {
        BlockState state = from.getBlock(fromMin.getX() + x, fromMin.getY() + y, fromMin.getZ() + z);
        to.setBlock(toMin.getX() + x, toMin.getY() + y, toMin.getZ() + z, state);
    }
}
