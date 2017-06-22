package me.dags.plots.support.voxelsniper;

import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeAction;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.Sniper;
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
    private Sniper.SniperTool tool;
    private PlotMask mask = PlotMask.ANYWHERE;

    public void wrap(IBrush brush) {
        this.brush = brush;
    }

    public void setMask(PlotMask mask) {
        this.mask = mask;
    }

    public void setTool(Sniper.SniperTool tool) {
        this.tool = tool;
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
            // wrap world and locations
            MaskedWorld maskedWorld = new MaskedWorld(targetBlock.getExtent(), mask);
            Location<World> maskedTarget = new Location<>(maskedWorld, targetBlock.getPosition());
            Location<World> maskedLast = new Location<>(maskedWorld, lastBlock.getPosition());

            // perform wrapped brush with masked locations
            brush.perform(action, data, maskedTarget, maskedLast);

            // unwrap brush
            tool.setCurrentBrush(brush.getClass());
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
