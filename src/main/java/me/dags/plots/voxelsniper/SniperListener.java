package me.dags.plots.voxelsniper;

import com.thevoxelbox.voxelsniper.Sniper;
import com.thevoxelbox.voxelsniper.SniperManager;
import com.thevoxelbox.voxelsniper.brush.IBrush;
import com.thevoxelbox.voxelsniper.brush.mask.Mask;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Support;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SniperListener implements Support.Hook {

    @Override
    public void init() {
        Sponge.getPluginManager().getPlugin(Plots.ID).flatMap(PluginContainer::getInstance).ifPresent(instance -> {
            Sponge.getEventManager().registerListeners(instance, this);
        });
    }

    @Listener(order = Order.PRE)
    public void interact(InteractBlockEvent.Secondary event, @Root Player player) {
        Sniper sniper = SniperManager.get().getSniperForPlayer(player);

        if (sniper != null) {
            Sniper.SniperTool tool = sniper.getSniperTool(sniper.getCurrentToolId());
            if (tool == null) {
                return;
            }

            IBrush brush = tool.getCurrentBrush();
            if (brush == null) {
                return;
            }

            Mask mask = Mask.ALL;
            Optional<PlotWorld> plotWorld = Plots.getApi().getPlotWorld(player.getWorld().getName());
            if (plotWorld.isPresent()) {
                PlotUser user = plotWorld.get().getUser(player.getUniqueId());
                if (user.isPresent()) {
                    mask = new SniperMask(user.getMask());
                }
            }
            brush.setMask(mask);
        }
    }
}
