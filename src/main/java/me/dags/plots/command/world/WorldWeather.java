package me.dags.plots.command.world;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.weather.Weathers;

/**
 * @author dags <dags@dags.me>
 */
public class WorldWeather {

    @Command(alias = "sun", parent = "plotworld")
    @Permission(Permissions.WORLD_WEATHER)
    public void sun(@Caller Player player) {
        player.getWorld().setWeather(Weathers.CLEAR, Integer.MAX_VALUE);
    }
}
