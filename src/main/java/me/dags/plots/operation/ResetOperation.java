package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.generator.Layer;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.plot.PlotBounds;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;

/**
 * @author dags <dags@dags.me>
 */
public class ResetOperation implements Operation {

    private final String world;
    private final PlotGenerator plotGenerator;
    private final MutableBlockVolume blockView;
    private final MutableBiomeArea biomeView;
    private final int maxX, maxY, maxZ, layersHeight;
    private final Vector3i min;

    private int x = 0, y = 0, z = 0;
    private boolean complete = false;
    private Runnable callback = null;

    public ResetOperation(World world, PlotBounds bounds) {
        GenerationPopulator populator = world.getWorldGenerator().getBaseGenerationPopulator();
        Vector3i min = bounds.getBlockMin();
        Vector3i max = bounds.getBlockMax();
        this.world = world.getName();
        this.plotGenerator = (PlotGenerator) populator;
        this.blockView = world.getBlockView(min, max);
        this.biomeView = world.getBiomeView(bounds.getMin(), bounds.getMax());
        this.layersHeight = plotGenerator.plotSchema().surfaceHeight();
        this.maxX = max.getX() - min.getX();
        this.maxY = max.getY() - min.getY();
        this.maxZ = max.getZ() - min.getZ();
        this.min = min;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public int process(int blocksToProcess) {
        for (; y <= maxY; y++) {
            for (; x <= maxX; x++) {
                for (; z <= maxZ; z++) {
                    processAt(x, y, z);

                    if (blocksToProcess-- <= 0) {
                        return 0;
                    }
                }
                this.z = 0;
            }
            this.x = 0;
        }
        complete = true;
        plotGenerator.generateBiomes(biomeView);
        if (callback != null) {
            callback.run();
        }
        return blocksToProcess;
    }

    @Override
    public boolean complete() {
        return complete;
    }

    @Override
    public void onComplete(Runnable callback) {
        this.callback = callback;
    }

    private void processAt(int x, int y, int z) {
        BlockType blockType = BlockTypes.AIR;

        if (y < layersHeight) {
            Layer layer = plotGenerator.layerAtHeight(y);
            layer.populate(blockView, y);
            blockType = layer.getBlockAt(x, z);
        }

        blockView.setBlockType(min.getX() + x, min.getY() + y, min.getZ() + z, blockType);
    }
}
