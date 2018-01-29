package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import org.spongepowered.api.command.CommandSource;

/**
 * @author dags <dags@dags.me>
 */
public class GenReload {

    @Command("gen reload")
    @Permission(Permissions.GEN_EDIT)
    public void reload(@Src CommandSource source) {
        reloadGenerators(source);
    }

    public static void reloadGenerators(CommandSource source) {
        Fmt.info("Reloading generators...").tell(source);
        Plots.core().reloadGenerators();
    }
}
