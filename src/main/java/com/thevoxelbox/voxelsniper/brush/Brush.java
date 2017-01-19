package com.thevoxelbox.voxelsniper.brush;

import com.thevoxelbox.voxelsniper.*;
import me.dags.plots.support.voxelsniper.mask.IMaskable;
import me.dags.plots.support.voxelsniper.mask.Mask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public abstract class Brush implements IBrush, IMaskable {

    private Mask mask = Mask.ALL;

    @Override
    public Mask getMask() {
        return mask;
    }

    @Override
    public void setMask(Mask mask) {
        this.mask = mask;
    }

    protected static int WORLD_HEIGHT = (Sponge.getServer().getChunkLayout().getSpaceMax().getY() + 1) * Sponge.getServer().getChunkLayout().getChunkSize().getY();

    public static Location<World> clampY(World world, int x, int y, int z) {
        if (y < 0) {
            y = 0;
        } else if (y > WORLD_HEIGHT) {
            y = WORLD_HEIGHT;
        }

        return new Location<>(world, x, y, z);
    }

    protected World world;
    protected Location<World> targetBlock;
    protected Location<World> lastBlock;
    protected Cause cause;
    protected Undo undo;
    private String name = "Undefined";

    @Override
    public void perform(SnipeAction action, SnipeData data, Location<World> targetBlock, Location<World> lastBlock) {
        this.world = targetBlock.getExtent();
        this.targetBlock = targetBlock;
        this.lastBlock = lastBlock;
        this.cause = VoxelSniper.plugin_cause.with(NamedCause.source(data.owner().getPlayer()));
        switch (action) {
            case ARROW:
                this.arrow(data);
                break;
            case GUNPOWDER:
                this.powder(data);
                break;
            default:
        }
        this.cause = null;
        this.world = null;
        this.targetBlock = null;
        this.lastBlock = null;
    }

    /**
     * The arrow action. Executed when a player RightClicks with an Arrow
     *
     * @param v Sniper caller
     */
    protected void arrow(final SnipeData v) {
    }

    /**
     * The powder action. Executed when a player RightClicks with Gunpowder
     *
     * @param v Sniper caller
     */
    protected void powder(final SnipeData v) {
    }

    @Override
    public abstract void info(Message vm);

    @Override
    public void parameters(final String[] par, final SnipeData v) {
        // @Usability support a --no-undo parameter flag
        v.sendMessage(TextColors.RED, "This brush does not accept additional parameters.");
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getBrushCategory() {
        return "General";
    }

    protected void setBlockType(int x, int y, int z, BlockType type) {
        setBlockType(x, y, z, type, BlockChangeFlag.ALL);
    }

    protected void setBlockType(int x, int y, int z, BlockType type, BlockChangeFlag flag) {
        if (!getMask().contains(x, y, z)) {
            return;
        }

        // Don't store undos if we aren't changing the block
        if (this.world.getBlockType(x, y, z) == type) {
            return;
        }
        if (this.undo != null) {
            this.undo.put(new Location<World>(this.world, x, y, z));
        }
        this.world.setBlockType(x, y, z, type, flag, this.cause);
    }

    protected void setBlockState(int x, int y, int z, BlockState type) {
        setBlockState(x, y, z, type, BlockChangeFlag.ALL);
    }

    protected void setBlockState(int x, int y, int z, BlockState type, BlockChangeFlag flag) {
        if (!getMask().contains(x, y, z)) {
            return;
        }

        // Don't store undos if we aren't changing the block
        if (this.world.getBlock(x, y, z) == type) {
            return;
        }
        if (this.undo != null) {
            this.undo.put(new Location<World>(this.world, x, y, z));
        }
        this.world.setBlock(x, y, z, type, flag, this.cause);
    }
}
