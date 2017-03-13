package me.dags.plots.support.voxelsniper;

import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeAction;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.brush.IBrush;
import me.dags.plots.Permissions;
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
        if (brush != null) {
            brush.info(vm);
        }
    }

    @Override
    public void parameters(String[] par, SnipeData v) {
        if (brush != null) {
            brush.parameters(par, v);
        }
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
        return brush != null ? brush.getName() : "";
    }

    @Override
    public void setName(String name) {
        if (brush != null) {
            brush.setName(name);
        }
    }

    @Override
    public String getBrushCategory() {
        return brush != null ? brush.getBrushCategory() : "";
    }

    @Override
    public String getPermissionNode() {
        return brush != null ? brush.getPermissionNode() : Permissions.ACTION_SNIPE;
    }
}
