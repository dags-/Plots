package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.util.Pair;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Alias {

    @Command(aliases = "alias", parent = "plot", desc = "Set the plot's alias", perm = @Permission(Permissions.PLOT_ALIAS))
    public void alias(@Caller Player player, @One("alias") String alias) {
        Pair<PlotWorld, PlotId> plot = Cmd.getContainingPlot(player);
        if (plot.present()) {
            PlotWorld world = plot.first();
            PlotId plotId = plot.second();
            Supplier<Optional<UUID>> owner = () -> PlotActions.findPlotOwner(world.database(), plotId);
            Consumer<Optional<UUID>> setAlias = setIfOwner(player, world, plotId, alias);
            Plots.executor().async(owner, setAlias);
        }
    }

    static Consumer<Optional<UUID>> setIfOwner(Player player, PlotWorld world, PlotId plotId, String alias) {
        return uuid -> {
            if (uuid.isPresent() && player.getUniqueId().equals(uuid.get())) {
                Supplier<Boolean> exists = () -> PlotActions.plotFromAlias(world.database(), alias).present();
                Consumer<Boolean> setAlias = setAlias(player, world, plotId, alias);
                Plots.executor().async(exists, setAlias);
            } else {
                FMT.error("You do not own plot ").stress(plotId).tell(player);
            }
        };
    }

    static Consumer<Boolean> setAlias(Player player, PlotWorld world, PlotId plotId, String alias) {
        return exists -> {
            if (exists) {
                FMT.error("The alias ").stress(alias).error(" has already been taken by another plot").tell(player);
            } else {
                Runnable async = () -> PlotActions.setPlotAlias(world.database(), plotId, alias);
                Runnable sync = () -> FMT.info("Set plot ").stress(plotId).info("'s alias to ").stress(alias).tell(player);
                Plots.executor().async(async, sync);
            }
        };
    }
}
