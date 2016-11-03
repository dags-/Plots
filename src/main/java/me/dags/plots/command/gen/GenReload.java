package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import org.spongepowered.api.command.CommandSource;

/**
 * @author dags <dags@dags.me>
 */
public class GenReload {

    @Command(aliases = "reload", parent = "gen", perm = @Permission(Permissions.GEN_EDIT))
    public void create(@Caller CommandSource source) {
        Cmd.FMT().info("Reloading generators...").tell(source);
        Plots.API().reloadGenerators();
    }
}
