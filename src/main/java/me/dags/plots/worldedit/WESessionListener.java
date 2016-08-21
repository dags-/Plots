package me.dags.plots.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotMask;
import me.dags.plots.plot.PlotUser;

/**
 * @author dags <dags@dags.me>
 */
public class WESessionListener {

    @Subscribe
    public void onCreateSession(EditSessionEvent event) {
        World world = event.getWorld();
        Actor actor = event.getActor();
        if (world != null && actor != null) {
            Plots.getApi().getPlotWorld(world.getName()).ifPresent(plotWorld -> {
                PlotUser user = plotWorld.getOrCreateUser(actor.getUniqueId());
                PlotMask mask = user.getMask();
                WorldEdit.getInstance().getSessionManager().get(actor).setMask(new WEPlotMask(mask));
            });
        }
    }

    static void register() {
        WorldEdit.getInstance().getEventBus().register(new WESessionListener());
    }
}
