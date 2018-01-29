package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.plots.Permissions;
import me.dags.plots.command.Cmd;
import me.dags.plots.plot.PlotMask;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.plot.PlotWorld;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Maskall {

    @Command("plot maskall")
    @Permission(Permissions.PLOT_MASKALL)
    @Description("Toggle building protections")
    public void maskall(@Src Player player) {
        Optional<PlotWorld> world = Cmd.getWorld(player);
        if (world.isPresent()) {
            PlotUser user = world.get().user(player.getUniqueId());
            if (user.plotMask() == PlotMask.ANYWHERE) {
                world.get().refreshUser(user.uuid());
                Fmt.info("Plot mask reset").tell(player);
            } else {
                user = user.edit().mask(PlotMask.ANYWHERE).build();
                world.get().setUser(user);
                Fmt.info("Plot mask set to Anywhere").tell(player);
            }
        }
    }
}
