package me.dags.plots.generator;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

/**
 * @author dags <dags@dags.me>
 */
public class LayerProperties {

    private BlockType body = BlockTypes.BEDROCK;
    private BlockType wall = BlockTypes.BEDROCK;
    private BlockType path = BlockTypes.BEDROCK;
    private int thickness = 1;

    public LayerProperties() {}

    public LayerProperties(BlockType body, BlockType wall, BlockType path, int thickness) {
        this.body = body;
        this.wall = wall;
        this.path = path;
        this.thickness = thickness;
    }

    public BlockType body() {
        return body;
    }

    public BlockType wall() {
        return wall;
    }

    public BlockType path() {
        return path;
    }

    public int thickness() {
        return thickness;
    }
}
