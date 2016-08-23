package me.dags.plots.plot;

import me.dags.commandbus.Format;
import me.dags.plots.Plots;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class PlotUser {

    private static final UUID DUMMY = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final PlotUser EMPTY = new PlotUser();

    private final UUID uuid;
    private final String world;
    private final Map<PlotId, PlotMeta> plotData;

    private transient PlotMask mask = null;

    private PlotUser() {
        this.uuid = PlotUser.DUMMY;
        this.world = "";
        this.plotData = Collections.emptyMap();
        this.mask = PlotMask.EMPTY;
    }

    private PlotUser(Builder builder) {
        this.uuid = builder.uuid;
        this.world = builder.world;
        this.plotData = Collections.unmodifiableMap(builder.plotData);
        this.mask = null;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public String getWorld() {
        return world;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Collection<Map.Entry<PlotId, PlotMeta>> getPlots() {
        return plotData.entrySet();
    }

    public PlotMask getMask() {
        return mask != null ? mask : (mask = PlotMask.calculate(getWorld(), plotData.keySet()));
    }

    public PlotMeta getMeta(PlotId plotId) {
        PlotMeta meta = plotData.get(plotId);
        return meta != null ? meta : PlotMeta.EMPTY;
    }

    public void maskAnywhere() {
        this.mask = PlotMask.ANYWHERE;
    }

    public void resetMask() {
        this.mask = null;
    }

    public boolean isWhitelisted(PlotId plotId) {
        return isPresent() && plotData.containsKey(plotId);
    }

    public boolean isOwner(PlotId plotId) {
        PlotMeta meta = plotData.get(plotId);
        return meta != null && meta.isPresent() && meta.isOwner();
    }

    public PaginationList listPlots() {
        List<Text> lines = new ArrayList<>();
        Format format = Plots.getConfig().getMessageFormat();
        for (Map.Entry<PlotId, PlotMeta> entry : plotData.entrySet()) {
            PlotId plotId = entry.getKey();
            PlotMeta meta = entry.getValue();
            String line = "(" + plotId + ")" + (meta.isPresent() && meta.hasName() ? " " + meta.getName() : "");
            Text text = format.stress(" - ").info(line).build().toBuilder().onClick(TextActions.runCommand("/plot tp " + plotId)).build();
            lines.add(text);
        }
        return PaginationList.builder().title(format.stress("Plots").build()).contents(lines).build();
    }

    public Builder toBuilder() {
        return isPresent() ? builder().uuid(uuid).world(world).plot(plotData) : builder();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID uuid = DUMMY;
        private String world = "";
        private final Map<PlotId, PlotMeta> plotData = new HashMap<>(0);

        public Builder uuid(UUID id) {
            this.uuid = id;
            return this;
        }

        public Builder world(String world) {
            this.world = world;
            return this;
        }

        public Builder plot(Map<PlotId, PlotMeta> plotData) {
            this.plotData.putAll(plotData);
            return this;
        }

        public Builder plot(PlotId plotId, PlotMeta data) {
            plotData.put(plotId, data);
            return this;
        }

        public Builder removePlot(PlotId plotId) {
            plotData.remove(plotId);
            return this;
        }

        public PlotUser build() {
            if (uuid == PlotUser.DUMMY) {
                throw new UnsupportedOperationException("UUID not set!");
            }
            if (world.isEmpty()) {
                throw new UnsupportedOperationException("World cannot be empty!");
            }
            return new PlotUser(this);
        }
    }
}
