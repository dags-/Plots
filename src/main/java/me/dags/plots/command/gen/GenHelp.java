package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.command.Cmd;
import org.spongepowered.api.command.CommandSource;

/**
 * @author dags <dags@dags.me>
 */
public class GenHelp {

    @Command(aliases = {"help", "?"}, parent = "gen", perm = @Permission(Permissions.GEN_EDIT))
    public void help(@Caller CommandSource source) {
        Cmd.FMT().info("/gen create <name>").tell(source);
        Cmd.FMT().info("/gen biome <biome>").tell(source);
        Cmd.FMT().info("/gen rule <gamerule> <value>").tell(source);
        Cmd.FMT().info("/gen dims <plot x width> <plot z width> <path width> <wall width>").tell(source);
        Cmd.FMT().info("/gen layer <plot material> <path material> <wall material> <layer thickness>").tell(source);
        Cmd.FMT().info("/gen reload").tell(source);
        Cmd.FMT().info("/gen save").tell(source);
    }
}
