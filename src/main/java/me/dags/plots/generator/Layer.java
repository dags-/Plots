package me.dags.plots.generator;

import com.flowpowered.math.vector.Vector3i;
import me.dags.plots.util.GridMap;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.extent.MutableBlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public class Layer {

    private final int thickness;
    private final GridMap<BlockState> layer;

    private Layer(Builder builder) {
        this.thickness = builder.thickness;
        this.layer = builder.layer();
    }

    public int thickness() {
        return thickness;
    }

    public void populate(MutableBlockVolume buffer, int y) {
        applyLayer(buffer, y);
    }

    public BlockState getBlockAt(int x, int z) {
        return layer.get(x, z);
    }

    protected void applyLayer(MutableBlockVolume buffer, int y) {
        Vector3i min = buffer.getBlockMin(), max = buffer.getBlockMax();
        for (int z = min.getZ(); z <= max.getZ(); z++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                BlockState state = layer.get(x, z);
                buffer.setBlock(x, y, z, state);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private static class Solid extends Layer {

        private final BlockState base;

        private Solid(Builder builder) {
            super(builder);
            this.base = builder.body;
        }

        @Override
        protected void applyLayer(MutableBlockVolume buffer, int y) {
            Vector3i min = buffer.getBlockMin(), max = buffer.getBlockMax();
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int x = min.getX(); x <= max.getX(); x++) {
                    buffer.setBlock(x, y, z, base);
                }
            }
        }

        public BlockState getBlockAt(int x, int z) {
            return base;
        }
    }

    public static class Builder {

        private int plotXWidth = 42;
        private int plotZWidth = 42;
        private int wallWidth = 1;
        private int pathWidth = 5;
        private int thickness = 1;
        private BlockState body = BlockTypes.BEDROCK.getDefaultState();
        private BlockState wall = BlockTypes.BEDROCK.getDefaultState();
        private BlockState path = BlockTypes.BEDROCK.getDefaultState();

        public Builder plotXWidth(int width) {
            this.plotXWidth = width;
            return this;
        }

        public Builder plotZWidth(int width) {
            this.plotZWidth = width;
            return this;
        }

        public Builder wallWidth(int width) {
            this.wallWidth = width;
            return this;
        }

        public Builder pathWidth(int width) {
            this.pathWidth = width;
            return this;
        }

        public Builder thickness(int thickness) {
            this.thickness = thickness;
            return this;
        }

        public Builder body(BlockState state) {
            this.body = state;
            return this;
        }

        public Builder wall(BlockState state) {
            this.wall = state;
            return this;
        }

        public Builder path(BlockState state) {
            this.path = state;
            return this;
        }

        public Builder body(BlockType type) {
            this.body = type.getDefaultState();
            return this;
        }

        public Builder wall(BlockType type) {
            this.wall = type.getDefaultState();
            return this;
        }

        public Builder path(BlockType type) {
            this.path = type.getDefaultState();
            return this;
        }

        public Builder all(BlockType type) {
            this.body = type.getDefaultState();
            this.wall = type.getDefaultState();
            this.path = type.getDefaultState();
            return this;
        }

        public Builder all(BlockState state) {
            this.body = state;
            this.wall = state;
            this.path = state;
            return this;
        }

        private boolean solid() {
            return body == wall && wall == path;
        }

        private GridMap<BlockState> layer() {
            if (solid()) {
                return GridMap.empty();
            }
            int layerXWidth = plotXWidth + (2 * wallWidth) + pathWidth;
            int layerZWidth = plotZWidth + (2 * wallWidth) + pathWidth;
            GridMap<BlockState> layer = new GridMap<>(BlockState[]::new, BlockTypes.AIR.getDefaultState(), layerXWidth, layerZWidth);
            layer.fill(body, wallWidth, plotXWidth + wallWidth, wallWidth, plotZWidth + wallWidth);

            layer.fill(wall, 0, wallWidth, 0, plotZWidth + wallWidth);
            layer.fill(wall, 0, plotXWidth + wallWidth, 0, wallWidth);
            layer.fill(wall, plotXWidth + wallWidth, plotXWidth + wallWidth + wallWidth, 0, plotZWidth + wallWidth);
            layer.fill(wall, 0, plotXWidth + wallWidth + wallWidth, plotZWidth + wallWidth, plotZWidth + wallWidth + wallWidth);

            layer.fill(path, plotXWidth + wallWidth + wallWidth, layerXWidth, 0, layerZWidth);
            layer.fill(path, 0, layerXWidth, plotZWidth + wallWidth + wallWidth, layerZWidth);
            return layer;
        }

        public Layer build() {
            return solid() ? new Solid(this) : new Layer(this);
        }
    }
}
