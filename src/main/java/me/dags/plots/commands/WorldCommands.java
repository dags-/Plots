package me.dags.plots.commands;

import me.dags.commandbus.Format;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.plots.Plots;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.util.IO;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class WorldCommands {

    private static final Format FORMAT = Plots.getConfig().getMessageFormat();

    @Command(aliases = "world")
    public void world(@Caller Player player, @One("world") String name) {
        Optional<World> world = Sponge.getServer().getWorld(name);
        if (world.isPresent()) {
            FORMAT.info("Teleporting to ").stress(world.get().getName()).tell(player);
            player.setLocation(world.get().getSpawnLocation());
        } else {
            FORMAT.error("World ").stress(name).error(" does not exist").tell(player);
        }
    }

    @Command(aliases = "create", parent = "plotworld")
    public void create(@Caller CommandSource source, @One("generator") String generator, @One("world") String name) {
        Plots.getApi().getBaseGenerator(generator).ifPresent(properties -> {
            GeneratorProperties worldProperties = properties.copyTo(name);
            PlotGenerator plotGenerator = worldProperties.toGenerator();
            Plots.getApi().registerWorldGenerator(plotGenerator);

            IO.saveProperties(worldProperties, Plots.getApi().configDir().resolve("worlds"));

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
                        FORMAT.info("Created world ").stress(world.getName()).tell(source);
                    });
        });
    }
}
