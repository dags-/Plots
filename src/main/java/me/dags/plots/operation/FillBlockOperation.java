package me.dags.plots.operation;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.extent.MutableBlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public class FillBlockOperation extends AbstractBlockOperation {

    private final MutableBlockVolume to;
    private final BlockState state;

    public FillBlockOperation(MutableBlockVolume to, BlockState state) {
        super(to.getBlockMin(), to.getBlockMax());
        this.to = to;
        this.state = state;
    }

    @Override
    void processAt(int x, int y, int z) {
        to.setBlock(x, y, z, state);
    }
}
