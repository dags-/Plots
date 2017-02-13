package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import me.dags.plots.command.Cmd;
import me.dags.plots.generator.GeneratorProperties;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public class GenCreate {

    @Command(aliases = "create", parent = "gen", perm = @Permission(Permissions.GEN_EDIT))
    public void create(@Caller CommandSource source, @One("name") String name) {
        Cmd.genBuilders().add(source, GeneratorProperties.builder().name(name));
        FMT.info("Building new Generator ").stress(name).append(Text.NEW_LINE)
                .subdued("See ").stress("/gen help").subdued(" for a list of available commands").append(Text.NEW_LINE)
                .subdued("Changes will be discarded if you are inactive for +" + 3 + " minutes")
                .tell(source);
    }
}
