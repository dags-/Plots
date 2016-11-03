package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.util.IO;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class GenSave {

    @Command(aliases = "save", parent = "gen", perm = @Permission(Permissions.GEN_EDIT))
    public void create(@Caller CommandSource source, @One("name") String name) {
        Cmd.genBuilders().add(source, GeneratorProperties.builder().name(name));
        Cmd.FMT().info("Building new Generator ").stress(name).append(Text.NEW_LINE)
                .subdued("See ").stress("/gen help").subdued(" for a list of available commands").append(Text.NEW_LINE)
                .subdued("Changes will be discarded if you are inactive for +" + 3 + " minutes")
                .tell(source);

        Optional<GeneratorProperties.Builder> builder = Cmd.genBuilders().get(source);
        if (builder.isPresent()) {
            GeneratorProperties properties = builder.get().build();
            IO.saveProperties(properties, Plots.API().generatorsDir());
            Cmd.genBuilders().remove(source);
            Cmd.FMT().info("Saved generator ").stress(properties.name()).info(" to file").tell(source);
        }
    }
}
