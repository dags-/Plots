package me.dags.plots.command.world;

import me.dags.commandbus.annotation.*;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class WorldTP {

    @Command(alias = {"worldtp", "wtp"})
    @Permission(Permissions.WORLD_TP)
    @Description("Teleport to a world")
    public void world(@Caller Player player, @One("world") String name) {
        Optional<World> world = matchWorld(name);
        if (world.isPresent()) {
            FMT.info("Teleporting to ").stress(world.get().getName()).tell(player);
            player.setLocation(world.get().getSpawnLocation());
        } else {
            FMT.error("World ").stress(name).error(" does not exist").tell(player);
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
