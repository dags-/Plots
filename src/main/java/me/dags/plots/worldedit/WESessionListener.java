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

    @Subscribe(priority = EventHandler.Priority.VERY_LATE)
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

                if (mask == null) {
                    mask = new WEPlotMask(null, plotMask);
                } else if (mask instanceof WEPlotMask) {
                    WEPlotMask current = (WEPlotMask) mask;

                    // Update WEMask if PlotUser's PlotMask has changed since last event
                    if (current.getMask() != plotMask) {
                        mask = new WEPlotMask(current.getOriginal(), plotMask);
                    }
                }
            } else if (mask != null && mask instanceof WEPlotMask) {
                // User has gone from a PlotWorld to non-PlotWorld
                // - set their mask back to what it was originally (probably null)
                mask = ((WEPlotMask) mask).getOriginal();
            }

            session.setMask(mask);
        }
    }

    static void register() {
        WorldEdit.getInstance().getEventBus().register(new WESessionListener());
    }
}
