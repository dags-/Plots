package me.dags.plots.worldedit;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.util.eventbus.EventHandler;
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

    @Subscribe (priority = EventHandler.Priority.VERY_EARLY)
    public void onCreateSession(EditSessionEvent event) {
        World world = event.getWorld();
        Actor actor = event.getActor();
        if (world != null && actor != null) {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);
            Mask mask = session.getMask();

            Optional<PlotWorld> plotWorld = Plots.getApi().getPlotWorld(world.getName());
            if (plotWorld.isPresent()) {
                PlotUser plotUser = plotWorld.get().getUser(actor.getUniqueId());
                PlotMask plotMask = plotUser.getMask();
                mask = new WEPlotMask(plotMask);
            } else if (mask instanceof WEPlotMask) {
                mask = null;
            }

            session.setMask(mask);
        }
    }

    static void register() {
        WorldEdit.getInstance().getEventBus().register(new WESessionListener());
    }
}
