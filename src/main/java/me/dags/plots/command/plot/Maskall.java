package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
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

    @Command(aliases = "maskall", parent = "plot", desc = "Toggle building protections", perm = @Permission(Permissions.PLOT_MASKALL))
    public void maskall(@Caller Player player) {
        Optional<PlotWorld> world = Cmd.getWorld(player);
        if (world.isPresent()) {
            PlotUser user = world.get().user(player.getUniqueId());
            if (user.plotMask() == PlotMask.ANYWHERE) {
                world.get().refreshUser(user.uuid());
                FMT.info("Plot mask reset").tell(player);
            } else {
                user = user.edit().mask(PlotMask.ANYWHERE).build();
                world.get().setUser(user);
                FMT.info("Plot mask set to Anywhere").tell(player);
            }
        }
    }
}
