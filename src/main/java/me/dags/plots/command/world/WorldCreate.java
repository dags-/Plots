package me.dags.plots.command.world;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.util.IO;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.WorldCreationSettings;

/**
 * @author dags <dags@dags.me>
 */
public class WorldCreate {

    @Command(aliases = "create", parent = "plotworld", desc = "Create a new PlotWorld", perm = @Permission(Permissions.WORLD_CREATE))
    public void create(@Caller CommandSource source, @One("generator") String generator, @One("world") String name) {
        Plots.API().baseGenerator(generator).ifPresent(properties -> {
            GeneratorProperties worldProperties = properties.copyTo(name);
            PlotGenerator plotGenerator = worldProperties.toGenerator();
            Plots.API().registerWorldGenerator(plotGenerator);

            IO.saveProperties(worldProperties, Plots.API().configDir().resolve("worlds"));

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
                        Cmd.FMT().info("Created world ").stress(world.getName()).tell(source);
                    });
        });
    }
}
