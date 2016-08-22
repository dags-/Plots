package me.dags.plots.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotMask;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.plot.PlotWorld;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class WESessionListener {

    @Subscribe
    public void onSessionEvent(EditSessionEvent event) {
        World world = event.getWorld();
        Actor actor = event.getActor();
        if (world != null && actor != null) {
            Optional<PlotWorld> plotWorld = Plots.getApi().getPlotWorld(world.getName());
            if (plotWorld.isPresent()) {
                PlotUser user = plotWorld.get().getUser(actor.getUniqueId());
                PlotMask mask = user.getMask();
                WEMaskedExtent maskedExtent = new WEMaskedExtent(event.getExtent(), mask);
                event.setExtent(maskedExtent);
            }
        }
    }

    static void register() {
        WorldEdit.getInstance().getEventBus().register(new WESessionListener());
    }
}
