package me.dags.plots.support.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.MaskingExtent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotMask;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Support;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class WESessionListener implements Support.Hook {

    @Override
    public void init() {
        WorldEdit.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onSessionEvent(EditSessionEvent event) {
        World world = event.getWorld();
        Actor actor = event.getActor();
        if ((world != null) && (actor != null)) {
            Optional<PlotWorld> plotWorld = Plots.core().plotWorldExact(world.getName());
            if (plotWorld.isPresent()) {
                PlotUser user = plotWorld.get().user(actor.getUniqueId());
                PlotMask mask = user.plotMask();
                event.setExtent(new MaskingExtent(event.getExtent(), new WEMask(mask)));
            }
        }
    }
}
