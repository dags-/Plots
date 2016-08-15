package me.dags.plots.commands;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.plots.Plots;
import me.dags.plots.plot.PlotBounds;
import me.dags.plots.plot.PlotId;
import me.dags.plots.PlotWorld;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public class PlotCommands {

    @Command(aliases = "info", parent = "plot")
    public void info(@Caller Player player) {
        PlotWorld plotWorld = Plots.getApi().getPlotWorld(player.getWorld().getName());
        if (plotWorld == null) {
            return;
        }
        Vector3i position = player.getLocation().getBlockPosition();
        PlotId plotId = plotWorld.getPlotId(position);
        PlotBounds bounds = plotWorld.getPlotBounds(plotId);
        if (bounds.contains(position)) {
            player.sendMessage(Text.of("PlotID: ", plotId));
        } else {
            player.sendMessage(Text.of("You are not within a Plot!"));
        }
    }
}
