package me.dags.plots.command.world;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.plots.Permissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class WorldTP {

    @Command("worldtp|wtp <world>")
    @Permission(Permissions.WORLD_TP)
    @Description("Teleport to a world")
    public void world(@Src Player player, String name) {
        Optional<World> world = matchWorld(name);
        if (world.isPresent()) {
            Fmt.info("Teleporting to ").stress(world.get().getName()).tell(player);
            player.setLocation(world.get().getSpawnLocation());
        } else {
            Fmt.error("World ").stress(name).error(" does not exist").tell(player);
        }
    }

    private Optional<World> matchWorld(String world) {
        world = world.toLowerCase();
        World closest = null;
        for (World w : Sponge.getServer().getWorlds()) {
            String test = w.getName().toLowerCase();
            if (test.equals(world)) {
                return Optional.of(w);
            }
            if (test.startsWith(world) && (closest == null || test.length() > closest.getName().length())) {
                closest = w;
            }
        }
        return Optional.ofNullable(closest);
    }
}
