package me.dags.plots.commands;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.Format;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.generator.PlotGenerator;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.IO;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class WorldCommands {

    private static final Format FORMAT = Plots.getConfig().getMessageFormat();

    @Command(aliases = {"worldtp", "wtp"}, perm = Permissions.WORLD_TP, desc = "Teleport yourself to the spawn of the given world")
    public void world(@Caller Player player, @One("world") String name) {
        Optional<World> world = Sponge.getServer().getWorld(name);
        if (world.isPresent()) {
            FORMAT.info("Teleporting to ").stress(world.get().getName()).tell(player);
            player.setLocation(world.get().getSpawnLocation());
        } else {
            FORMAT.error("World ").stress(name).error(" does not exist").tell(player);
        }
    }

    @Command(aliases = "create", parent = "plotworld", perm = Permissions.WORLD_CREATE, desc = "Create and load a new plot world using the given generator")
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

    @Command(aliases = "load", parent = "plotworld", perm = Permissions.WORLD_LOAD, desc = "Load the given world")
    public void load(@Caller CommandSource source, @One("world") String name) {
        Optional<World> world = Sponge.getServer().loadWorld(name);
        if (world.isPresent()) {
            FORMAT.info("Successfully loaded world ").stress(world.get().getName()).tell(source);
        } else {
            FORMAT.error("World ").stress(name).error(" was not recognised").tell(source);
        }
    }

    @Command(aliases = "unload", parent = "plotworld", perm = Permissions.WORLD_UNLOAD, desc = "Unload the given world")
    public void unload(@Caller CommandSource source, @One("world") String name) {
        Optional<World> world = Plots.getApi().getPlotWorld(name).map(PlotWorld::getWorldId).flatMap(Sponge.getServer()::getWorld);
        if (world.isPresent()) {
            if (Sponge.getServer().unloadWorld(world.get())) {
                FORMAT.info("Successfully unloaded world ").stress(world.get().getName()).tell(source);
            } else {
                FORMAT.error("Could not unload world ").stress(world.get().getName())
                        .error(". There may still be players in it").tell(source);
            }
        } else {
            FORMAT.error("World ").stress(name).error(" was not recognised").tell(source);
        }
    }

    @Command(aliases = "enable", parent = "plotworld", perm = Permissions.WORLD_ENABLE, desc = "Set whether the world should load at startup")
    public void setEnabled(@Caller CommandSource source, @One("world") String name, @One("enable") boolean enable) {
        Optional<WorldProperties> world = Sponge.getServer().getWorldProperties(name);
        if (world.isPresent()) {
            world.get().setEnabled(enable);
            world.get().setLoadOnStartup(enable);
            FORMAT.info("Set world ").stress(world.get().getWorldName()).info(" enabled: ").stress(enable).tell(source);
        } else {
            FORMAT.error("World ").stress(name).error(" was not recognised").tell(source);
        }
    }

    @Command(aliases = "spawn", parent = "plotworld", perm = Permissions.WORLD_SPAWN)
    public void setSpawn(@Caller Player player) {
        World world = player.getWorld();
        Vector3i pos = player.getLocation().getBlockPosition();
        world.getProperties().setSpawnPosition(pos);

        FORMAT.info("Set world ").stress(world.getName()).info("'s spawn to ").stress(pos).tell(player);
    }
}
