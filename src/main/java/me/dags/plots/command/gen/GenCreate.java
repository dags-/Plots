package me.dags.plots.command.gen;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.plots.Permissions;
import me.dags.plots.command.Cmd;
import me.dags.plots.generator.GeneratorProperties;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public class GenCreate {

    @Command("gen create <name>")
    @Permission(value = Permissions.GEN_EDIT)
    public void create(@Src CommandSource source, String name) {
        Cmd.genBuilders().add(source, GeneratorProperties.builder().name(name));
        Fmt.info("Building new Generator ").stress(name).append(Text.NEW_LINE)
                .subdued("See ").stress("/gen help").subdued(" for a list of available commands").append(Text.NEW_LINE)
                .subdued("Changes will be discarded if you are inactive for +" + 3 + " minutes")
                .tell(source);
    }
}
