package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import org.spongepowered.api.command.CommandSource;

/**
 * @author dags <dags@dags.me>
 */
public class GenHelp {

    @Command(aliases = {"help", "?"}, parent = "gen", perm = @Permission(Permissions.GEN_EDIT))
    public void help(@Caller CommandSource source) {
        FMT.info("/gen create <name>").tell(source);
        FMT.info("/gen biome <biome>").tell(source);
        FMT.info("/gen rule <gamerule> <value>").tell(source);
        FMT.info("/gen dims <plot x width> <plot z width> <path width> <wall width>").tell(source);
        FMT.info("/gen layer <plot material> <path material> <wall material> <layer thickness>").tell(source);
        FMT.info("/gen reload").tell(source);
        FMT.info("/gen save").tell(source);
    }
}
