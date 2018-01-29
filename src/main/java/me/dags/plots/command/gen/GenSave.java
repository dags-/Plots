package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.util.IO;
import org.spongepowered.api.command.CommandSource;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class GenSave {

    @Command("gen save")
    @Permission(Permissions.GEN_EDIT)
    public void save(@Src CommandSource source) {
        Optional<GeneratorProperties.Builder> builder = Cmd.genBuilders().get(source);
        if (builder.isPresent()) {
            GeneratorProperties properties = builder.get().build();
            saveGenerator(source, properties);
        }
    }

    public static void saveGenerator(CommandSource source, GeneratorProperties properties) {
        IO.saveProperties(properties, Plots.core().generatorsDir());
        Cmd.genBuilders().remove(source);
        Fmt.info("Saved generator ").stress(properties.name()).info(" to file").tell(source);
    }
}
