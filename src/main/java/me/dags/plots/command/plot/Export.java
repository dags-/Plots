package me.dags.plots.command.plot;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.plot.PlotBounds;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.support.plotsweb.PlotsWeb;
import me.dags.plots.util.Pair;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class Export {

    @Command(aliases = "export", parent = "plot", desc = "Export the plot to a schematic that you can download", perm = @Permission(Permissions.PLOT_EXPORT))
    public void export(@Caller Player player) {
        if (!PlotsWeb.getHelper().isEnabled()) {
            Cmd.FMT().error("PlotsWeb service is not running!").tell(player);
            return;
        }

        Pair<PlotWorld, PlotId> plot = Cmd.getPlot(player);
        if (plot.present()) {
            PlotWorld plotWorld = plot.first();
            PlotId plotId = plot.second();
            Supplier<Optional<UUID>> owner = () -> PlotActions.findPlotOwner(plotWorld.database(), plotId);
            Consumer<Optional<UUID>> export = export(player, plotWorld, plotId);
            Plots.executor().async(owner, export);
        }
    }

    static Consumer<Optional<UUID>> export(Player player, PlotWorld world, PlotId plotId) {
        return uuid -> {
            if (uuid.isPresent()) {
                // Play doesn't own plot
                if (uuid.get() != player.getUniqueId() && !player.hasPermission(Permissions.PLOT_EXPORT_OTHER)) {
                    Cmd.FMT().error("You do not own plot ").stress(plotId).tell(player);
                    return;
                }

                // Send existing link if it hasn't expired
                Optional<Text> lookup = getExportLink(world, plotId);
                if (lookup.isPresent()) {
                    player.sendMessage(lookup.get());
                    return;
                }

                // Export plot as schematic to file
                Cmd.FMT().info("Exporting plot ").stress(plotId).info("...").tell(player);
                PlotBounds bounds = world.plotSchema().plotBounds(plotId);
                Vector3i min = bounds.getBlockMin();
                Vector3i max = bounds.getBlockMax();
                Vector3i origin = player.getLocation().getBlockPosition();
                ArchetypeVolume volume = player.getWorld().createArchetypeVolume(min, max, origin);

                Schematic schematic = Schematic.builder()
                        .paletteType(BlockPaletteTypes.GLOBAL)
                        .metaValue(Schematic.METADATA_AUTHOR, player.getName())
                        .metaValue(Schematic.METADATA_NAME, plotId.toString())
                        .volume(volume)
                        .build();

                DataContainer container = DataTranslators.SCHEMATIC.translate(schematic);
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(1024);
                try (GZIPOutputStream gzipOut = new GZIPOutputStream(bytesOut)) {
                    DataFormats.NBT.writeTo(gzipOut, container);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Optional<Text> link = getExportLink(world, plotId);
                if (link.isPresent()) {
                    Cmd.FMT().info("Download: ").append(link.get()).tell(player);
                    return;
                }

                Cmd.FMT().subdued("No existing export found for plot ").stress(plotId).tell(player);
            } else {
                Cmd.FMT().error("Nobody owns plot ").stress(plotId).tell(player);
            }
        };
    }

    private static Optional<Text> getExportLink(PlotWorld plotWorld, PlotId plotId) {
        String name = plotWorld.world() + "_" + plotId.plotX() + ";" + plotId.plotZ() + ".schematic";
        Optional<URL> url = PlotsWeb.getHelper().lookup(name);
        if (url.isPresent()) {
            Text text = Text.builder(url.get().toString())
                    .format(TextFormat.of(TextColors.YELLOW, TextStyles.UNDERLINE))
                    .onClick(TextActions.openUrl(url.get())).build();
            return Optional.of(text);
        }
        return Optional.empty();
    }
}
