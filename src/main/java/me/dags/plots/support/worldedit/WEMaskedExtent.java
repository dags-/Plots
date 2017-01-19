package me.dags.plots.support.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.biome.BaseBiome;
import me.dags.plots.plot.PlotMask;

/**
 * @author dags <dags@dags.me>
 */
class WEMaskedExtent extends AbstractDelegateExtent {

    private final PlotMask mask;

    WEMaskedExtent(Extent extent, PlotMask mask) {
        super(extent);
        this.mask = mask;
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block)
            throws WorldEditException {
        return (this.mask.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) && (super.setBlock(location, block));
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        return (this.mask.contains(position.getBlockX(), 1, position.getBlockZ())) && (super.setBiome(position, biome));
    }
}
