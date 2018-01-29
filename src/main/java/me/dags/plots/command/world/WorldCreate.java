package me.dags.plots.command.world;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.util.IO;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class WorldCreate {

    @Command("plotworld create <generator> <name>")
    @Permission(Permissions.WORLD_CREATE)
    @Description("Create a new PlotWorld")
    public void create(@Src CommandSource source, GeneratorProperties generator, String name) {
        createWorld(source, generator, name);
    }

    public static void createWorld(CommandSource source, GeneratorProperties generator, String name) {
        GeneratorProperties generatorProperties = generator.copyTo(name);
        PlotGenerator plotGenerator = generatorProperties.toGenerator();
        Plots.core().registerWorldGenerator(plotGenerator);

        IO.saveProperties(generatorProperties, Plots.core().configDir().resolve("worlds"));

        WorldArchetype archetype = WorldArchetype.builder()
                .generatorModifiers(plotGenerator)
                .dimension(DimensionTypes.OVERWORLD)
                .generator(GeneratorTypes.FLAT)
                .gameMode(GameModes.CREATIVE)
                .generateSpawnOnLoad(true)
                .usesMapFeatures(false)
                .loadsOnStartup(true)
                .build(generatorProperties.getId(), generatorProperties.getId());

        try {
            WorldProperties worldProperties = Sponge.getServer().createWorldProperties(name, archetype);
            Sponge.getServer().loadWorld(worldProperties).ifPresent(world -> {
                plotGenerator.onLoadWorld(world);
                Fmt.info("Created world ").stress(world.getName()).tell(source);
            });
        } catch (IOException e) {
            Fmt.warn("Unable to create world ").stress(name).tell(source);
            e.printStackTrace();
        }
    }
}
