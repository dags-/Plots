package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.extent.MutableBlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public class FillBlockOperation extends AbstractBlockOperation {

    private final MutableBlockVolume to;
    private final Vector3i toMin;
    private final BlockState state;

    public FillBlockOperation(String world, MutableBlockVolume to, BlockState state) {
        super(world, to.getBlockMin(), to.getBlockMax());
        this.to = to;
        this.toMin = to.getBlockMin();
        this.state = state;
    }

    @Override
    void processAt(int x, int y, int z) {
        to.setBlock(toMin.getX() + x, toMin.getY() + y, toMin.getZ() + z, state);
    }
}
