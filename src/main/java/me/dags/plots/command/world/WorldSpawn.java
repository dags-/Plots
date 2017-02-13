package me.dags.plots.command.world;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class WorldSpawn {

    @Command(aliases = "spawn", parent = "plotworld", desc = "Set the world spawn", perm = @Permission(Permissions.WORLD_SPAWN))
    public void setSpawn(@Caller Player player) {
        World world = player.getWorld();
        Vector3i pos = player.getLocation().getBlockPosition();
        world.getProperties().setSpawnPosition(pos);
        FMT.info("Set world ").stress(world.getName()).info("'s spawn to ").stress(pos).tell(player);
    }
}
