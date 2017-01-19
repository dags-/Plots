package me.dags.plots.support.voxelsniper;

import com.thevoxelbox.voxelsniper.Sniper;
import com.thevoxelbox.voxelsniper.SniperManager;
import com.thevoxelbox.voxelsniper.brush.Brush;
import com.thevoxelbox.voxelsniper.brush.IBrush;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.support.voxelsniper.mask.IMaskable;
import me.dags.plots.support.voxelsniper.mask.Mask;
import me.dags.plots.util.Support;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SniperListener implements Support.Hook {

    @Override
    public void init() {
        BrushLoader.load();
        Plots.log("IMaskable Brush loaded: {}", IMaskable.class.isAssignableFrom(Brush.class));

        Sponge.getPluginManager().getPlugin("plots").flatMap(PluginContainer::getInstance).ifPresent(plugin -> {
            Sponge.getEventManager().registerListeners(plugin, this);
        });
    }

    @Listener(order = Order.PRE)
    public void interact(InteractItemEvent.Secondary.MainHand event, @Root Player player) {
        Sniper sniper = SniperManager.get().getSniperForPlayer(player);
        if (sniper != null) {
            Sniper.SniperTool tool = sniper.getSniperTool(sniper.getCurrentToolId());
            if (tool == null) {
                return;
            }

            IBrush brush = tool.getCurrentBrush();
            if (brush == null || !IMaskable.class.isInstance(brush)) {
                return;
            }

            Mask mask = Mask.ALL;
            Optional<PlotWorld> plotWorld = Plots.core().plotWorld(player.getWorld().getName());
            if (plotWorld.isPresent()) {
                PlotUser user = plotWorld.get().user(player.getUniqueId());
                mask = new SniperMask(user.plotMask());
            }

            IMaskable.class.cast(brush).setMask(mask);
        }
    }
}
