package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import org.spongepowered.api.command.CommandSource;

/**
 * @author dags <dags@dags.me>
 */
public class GenReload {

    @Command(alias = "reload", parent = "gen")
    @Permission(Permissions.GEN_EDIT)
    public void create(@Caller CommandSource source) {
        FMT.info("Reloading generators...").tell(source);
        Plots.core().reloadGenerators();
    }
}
