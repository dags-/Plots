package me.dags.plots.support.voxelsniper;

import com.thevoxelbox.voxelsniper.Sniper;
import com.thevoxelbox.voxelsniper.SniperManager;
import com.thevoxelbox.voxelsniper.brush.IBrush;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotMask;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.plot.PlotWorld;
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
        Sponge.getPluginManager().getPlugin("plots").flatMap(PluginContainer::getInstance).ifPresent(plugin -> {
            Sponge.getEventManager().registerListeners(plugin, SniperListener.this);
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

            IBrush current = tool.getCurrentBrush();
            if (current == null) {
                return;
            }

            MaskedBrush masked = null;

            if (MaskedBrush.class.isInstance(current)) {
                masked = MaskedBrush.class.cast(current);
            } else {
                IBrush brush = tool.setCurrentBrush(MaskedBrush.class);

                if (brush != null) {
                    masked = MaskedBrush.class.cast(brush);
                    masked.wrap(current);
                }
            }

            if (masked != null) {
                Optional<PlotWorld> plotWorld = Plots.core().plotWorld(player.getWorld().getName());
                if (plotWorld.isPresent()) {
                    PlotUser user = plotWorld.get().user(player.getUniqueId());
                    PlotMask mask = user.plotMask();
                    masked.setMask(mask);
                } else {
                    tool.setCurrentBrush(masked.getWrapped().getClass());
                }
            }
        }
    }
}
