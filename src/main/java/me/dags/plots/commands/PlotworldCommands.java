package me.dags.plots.commands;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.plots.Plots;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.util.IO;
import me.dags.plots.PlotWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.WorldCreationSettings;

import java.nio.file.Files;

/**
 * @author dags <dags@dags.me>
 */
public class PlotworldCommands {

    @Command(aliases = "world")
    public void worldWarp(@Caller Player player, @One("world") String world) {
        Sponge.getServer().getWorld(world).ifPresent(w -> player.setLocation(w.getSpawnLocation()));
    }

    @Command(aliases = "reload", parent = "plotworld")
    public void reload(@Caller CommandSource source) {
        if (!Files.exists(Plots.getApi().configDir().resolve("generators").resolve("default.conf"))) {
            IO.saveProperties(GeneratorProperties.DEFAULT, Plots.getApi().configDir().resolve("generators"));
        }
        IO.loadGeneratorProperties(Plots.getApi().configDir().resolve("generators")).forEach(Plots.getApi()::register);
        source.sendMessage(Text.of("Reloading..."));
    }

    @Command(aliases = "create", parent = "plotworld")
    public void create(@Caller CommandSource source, @One("generator") String generator, @One("world") String name) {
        Plots.getApi().getGenerator(generator).ifPresent(plotGenerator -> {
            WorldCreationSettings settings = WorldCreationSettings.builder()
                    .generatorModifiers(plotGenerator)
                    .dimension(DimensionTypes.OVERWORLD)
                    .generator(GeneratorTypes.FLAT)
                    .gameMode(GameModes.CREATIVE)
                    .generateSpawnOnLoad(true)
                    .usesMapFeatures(false)
                    .loadsOnStartup(true)
                    .name(name)
                    .build();

            Sponge.getServer().createWorldProperties(settings)
                    .flatMap(Sponge.getServer()::loadWorld)
                    .ifPresent(world -> {
                        plotGenerator.onLoadWorld(world);
                        source.sendMessage(Text.of("Created world: ", world.getName()));
                        Plots.getApi().registerPlotWorld(new PlotWorld(world, plotGenerator.plotProvider()));
                    });
        });
    }
}
