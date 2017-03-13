package me.dags.plots.generator;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

/**
 * @author dags <dags@dags.me>
 */
public class LayerProperties {

    private final BlockState body;
    private final BlockState wall;
    private final BlockState path;
    private final int thickness;

    public LayerProperties() {
        this(BlockTypes.BEDROCK, BlockTypes.BEDROCK, BlockTypes.BEDROCK, 1);
    }

    public LayerProperties(BlockType body, BlockType wall, BlockType path, int thickness) {
        this.body = body.getDefaultState();
        this.wall = wall.getDefaultState();
        this.path = path.getDefaultState();
        this.thickness = thickness;
    }

    public LayerProperties(BlockState body, BlockState wall, BlockState path, int thickness) {
        this.body = body;
        this.wall = wall;
        this.path = path;
        this.thickness = thickness;
    }

    public BlockState body() {
        return body;
    }

    public BlockState wall() {
        return wall;
    }

    public BlockState path() {
        return path;
    }

    public int thickness() {
        return thickness;
    }
}
