package me.dags.plots.operation;

import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.generator.Layer;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.plot.PlotBounds;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class ResetOperation implements Operation {

    private static final Predicate<Entity> NOT_PLAYER = entity -> !Player.class.isInstance(entity);

    private final String world;
    private final PlotGenerator plotGenerator;
    private final MutableBlockVolume blockView;
    private final MutableBiomeVolume biomeView;
    private final Collection<WeakReference<Entity>> entityView;
    private final int maxX, maxZ, layersHeight;
    private final Vector3i min;

    private int x = 0, y = 255, z = 0;
    private boolean complete = false;
    private Runnable callback = null;

    public ResetOperation(World world, PlotBounds bounds) {
        GenerationPopulator populator = world.getWorldGenerator().getBaseGenerationPopulator();
        Vector3i min = bounds.getBlockMin();
        Vector3i max = bounds.getBlockMax();
        this.world = world.getName();
        this.plotGenerator = (PlotGenerator) populator;
        this.blockView = world.getBlockView(min, max);
        this.biomeView = world.getBiomeView(bounds.getBlockMin().mul(1, 0, 1), bounds.getBlockMax().mul(1, 0, 1));
        this.layersHeight = plotGenerator.plotSchema().surfaceHeight();
        this.maxX = max.getX() - min.getX();
        this.maxZ = max.getZ() - min.getZ();
        this.min = min;
        this.entityView = world.getExtentView(min, max).getEntities().stream()
                .filter(NOT_PLAYER)
                .map(WeakReference::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public int process(int blocksToProcess) {
        for (; y > min.getY(); y--) {
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
        entityView.stream().map(Reference::get).filter(Objects::nonNull).forEach(Entity::remove);

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
        BlockState state = BlockTypes.AIR.getDefaultState();

        if (y < layersHeight) {
            Layer layer = plotGenerator.layerAtHeight(y);
            state = layer.getBlockAt(x, z);
        }

        blockView.setBlock(min.getX() + x, min.getY() + y, min.getZ() + z, state);
    }
}
