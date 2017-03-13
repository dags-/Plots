package me.dags.plots.support.voxelsniper;

import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeAction;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.brush.IBrush;
import me.dags.plots.plot.PlotMask;
import me.dags.plots.support.MaskedWorld;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class MaskedBrush implements IBrush {

    private IBrush brush;
    private PlotMask mask = PlotMask.ANYWHERE;

    public IBrush getWrapped() {
        return brush;
    }

    public void wrap(IBrush brush) {
        this.brush = brush;
    }

    public void setMask(PlotMask mask) {
        this.mask = mask;
    }

    @Override
    public void info(Message vm) {
        brush.info(vm);
    }

    @Override
    public void parameters(String[] par, SnipeData v) {
        brush.parameters(par, v);
    }

    @Override
    public void perform(SnipeAction action, SnipeData data, Location<World> targetBlock, Location<World> lastBlock) {
        if (brush != null) {
            MaskedWorld world = new MaskedWorld(targetBlock.getExtent(), mask);
            targetBlock = new Location<>(world, targetBlock.getPosition());
            lastBlock = new Location<>(world, lastBlock.getPosition());
            brush.perform(action, data, targetBlock, lastBlock);
        }
    }

    @Override
    public String getName() {
        return brush.getName();
    }

    @Override
    public void setName(String name) {
        brush.setName(name);
    }

    @Override
    public String getBrushCategory() {
        return brush.getBrushCategory();
    }

    @Override
    public String getPermissionNode() {
        return brush.getPermissionNode();
    }
}
