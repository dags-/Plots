package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.biome.BiomeType;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Biome {

    @Permission(Permissions.PLOT_BIOME)
    @Description("Set the biome of the plot")
    @Command("plot biome <biome>")
    public void biome(@Src Player player, String biome) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            Optional<BiomeType> biomeType = Sponge.getRegistry().getType(BiomeType.class, biome);
            if (biomeType.isPresent()) {
                PlotWorld world = plot.first();
                PlotId plotId = plot.second();
                UUID uuid = player.getUniqueId();
                Supplier<Boolean> owner = () -> PlotActions.findPlotOwner(world.database(), plotId).map(id -> id.equals(uuid)).orElse(false);
                Consumer<Boolean> setBiome = biome(player, world, plotId, biomeType.get());
                Plots.executor().async(owner, setBiome);
            } else {
                Fmt.error("Biome ").stress(biome).error(" is not recognised").tell(player);
            }
        }
    }

    static Consumer<Boolean> biome(Player player, PlotWorld world, PlotId plotId, BiomeType biome) {
        return owner -> {
            if (owner) {
                Fmt.info("Setting the biome of plot ").stress(plotId).info(" to ").stress(biome.getName()).tell(player);
                world.setBiome(plotId, biome);
            } else {
                Fmt.error("You do not own plot ").stress(plotId).tell(player);
            }
        };
    }
}
