package me.dags.plots.command.plot;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.*;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.Cmd;
import me.dags.plots.database.PlotActions;
import me.dags.plots.plot.PlotBounds;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotWorld;
import me.dags.plots.support.plotsweb.PlotsWeb;
import me.dags.plots.util.Pair;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;
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

    @Command(alias = "export", parent = "plot")
    @Permission(Permissions.PLOT_EXPORT)
    @Description("Export the plot to a schematic that you can download")
    public void export(@Caller Player player) {
        if (!PlotsWeb.getHelper().isEnabled()) {
            FMT.error("PlotsWeb service is not running!").tell(player);
            return;
        }

        Pair<PlotWorld, PlotId> plot = Cmd.getPlot(player);
        if (plot.present()) {
            PlotWorld plotWorld = plot.first();
            PlotId plotId = plot.second();
            Supplier<Optional<UUID>> owner = () -> PlotActions.findPlotOwner(plotWorld.database(), plotId);
            Consumer<Optional<UUID>> export = export(plotWorld, plotId, player);
            Plots.executor().async(owner, export);
        }
    }

    @Command(alias = "export", parent = "plot")
    @Permission(Permissions.PLOT_EXPORT)
    @Description("Export the plot to a schematic that you can download")
    public void export(@Caller CommandSource source, @One("world") PlotWorld world, @One("plot") String plotId) {
        if (!PlotsWeb.getHelper().isEnabled()) {
            FMT.error("PlotsWeb service is not running!").tell(source);
            return;
        }

        if (!PlotId.isValid(plotId)) {
            FMT.stress(plotId).error(" is not a valid plot id").tell(source);
            return;
        }

        exportPlot(world, PlotId.parse(plotId), source);
    }

    static Consumer<Optional<UUID>> export(PlotWorld plotWorld, PlotId plotId, Player player) {
        return uuid -> {
            if (uuid.isPresent()) {
                // Player doesn't own plot
                if (uuid.get() != player.getUniqueId() && !player.hasPermission(Permissions.PLOT_EXPORT_OTHER)) {
                    FMT.error("You do not own plot ").stress(plotId).tell(player);
                    return;
                }

                exportPlot(plotWorld, plotId, player);
                return;
            }

            FMT.error("Nobody owns plot ").stress(plotId).tell(player);
        };
    }

    static void exportPlot(PlotWorld plotWorld, PlotId plotId, CommandSource source) {
        Optional<World> world = plotWorld.getWorld();
        if (!world.isPresent()) {
            FMT.error("Could not locate world ").stress(plotWorld.getName()).tell(source);
            return;
        }

        // Send existing link if it hasn't expired
        Optional<Text> lookup = lookupLink(plotWorld, plotId);
        if (lookup.isPresent()) {
            source.sendMessage(lookup.get());
            return;
        }

        // Export plot as schematic to file
        FMT.info("Exporting plot ").stress(plotId).info("...").tell(source);
        PlotBounds bounds = plotWorld.plotSchema().plotBounds(plotId);
        Vector3i min = bounds.getBlockMin();
        Vector3i max = bounds.getBlockMax();
        Vector3i origin = source instanceof Locatable ? ((Locatable) source).getLocation().getBlockPosition() : min;
        ArchetypeVolume volume = world.get().createArchetypeVolume(min, max, origin);

        Schematic schematic = Schematic.builder()
                .paletteType(BlockPaletteTypes.GLOBAL)
                .metaValue(Schematic.METADATA_AUTHOR, source.getName())
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

        Optional<Text> link = exportLink(plotWorld, plotId, bytesOut.toByteArray());
        if (link.isPresent()) {
            source.sendMessage(link.get());
            return;
        }

        FMT.subdued("No existing export found for plot ").stress(plotId).tell(source);
    }

    private static Optional<Text> lookupLink(PlotWorld plotWorld, PlotId plotId) {
        String name = toExportName(plotWorld, plotId);
        Optional<URL> url = PlotsWeb.getHelper().lookup(name);
        if (url.isPresent()) {
            Text text = Text.builder(url.get().toString())
                    .format(TextFormat.of(TextColors.YELLOW, TextStyles.UNDERLINE))
                    .onClick(TextActions.openUrl(url.get())).build();
            return Optional.of(text);
        }
        return Optional.empty();
    }

    private static Optional<Text> exportLink(PlotWorld plotWorld, PlotId plotId, byte[] data) {
        String name = toExportName(plotWorld, plotId);
        Optional<URL> url = PlotsWeb.getHelper().getExportLink(name, data);
        if (url.isPresent()) {
            Text text = Text.builder(url.get().toString())
                    .format(TextFormat.of(TextColors.YELLOW, TextStyles.UNDERLINE))
                    .onClick(TextActions.openUrl(url.get())).build();

            return Optional.of(text);
        }
        return Optional.empty();
    }

    private static String toExportName(PlotWorld world, PlotId id) {
        return String.format("%s_%s;%s.schematic", world.world(), id.plotX(), id.plotZ());
    }
}
