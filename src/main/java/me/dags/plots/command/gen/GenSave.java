package me.dags.plots.command.gen;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
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

    @Command(alias = "save", parent = "gen")
    @Permission(Permissions.GEN_EDIT)
    public void save(@Caller CommandSource source) {
        Optional<GeneratorProperties.Builder> builder = Cmd.genBuilders().get(source);
        if (builder.isPresent()) {
            GeneratorProperties properties = builder.get().build();
            IO.saveProperties(properties, Plots.core().generatorsDir());
            Cmd.genBuilders().remove(source);
            FMT.info("Saved generator ").stress(properties.name()).info(" to file").tell(source);
        }
    }
}
