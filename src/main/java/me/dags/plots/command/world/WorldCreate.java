package me.dags.plots.command.world;

import me.dags.commandbus.annotation.*;
import me.dags.commandbus.format.FMT;
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

    @Command(alias = "create")
    @Permission(Permissions.WORLD_CREATE)
    @Description("Create a new PlotWorld")
    public void create(@Caller CommandSource source, @One("generator") String generator, @One("world") String name) {
        createWorld(source, generator, name);
    }

    public static void createWorld(CommandSource source, String generator, String name) {
        Plots.core().baseGenerator(generator).ifPresent(baseGenerator -> {
            GeneratorProperties generatorProperties = baseGenerator.copyTo(name);
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
                    .build(generator, generator);

            try {
                WorldProperties worldProperties = Sponge.getServer().createWorldProperties(name, archetype);
                Sponge.getServer().loadWorld(worldProperties).ifPresent(world -> {
                    plotGenerator.onLoadWorld(world);
                    FMT.info("Created world ").stress(world.getName()).tell(source);
                });
            } catch (IOException e) {
                FMT.warn("Unable to create world ").stress(name).tell(source);
                e.printStackTrace();
            }
        });
    }
}
