package me.dags.plots.plot;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class PlotUser {

    public static final PlotUser EMPTY = new PlotUser();

    private final UUID uuid;
    private final String world;
    private final Map<PlotId, PlotMeta> plotData;

    private PlotUser() {
        this.uuid = null;
        this.world = null;
        this.plotData = null;
    }

    private PlotUser(Builder builder) {
        this.uuid = builder.uuid;
        this.world = builder.world;
        this.plotData = Collections.unmodifiableMap(builder.plotData);
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public String getWorld() {
        return world;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Collection<Map.Entry<PlotId, PlotMeta>> getPlots() {
        return plotData.entrySet();
    }

    public boolean isWhitelisted(PlotId plotId) {
        return plotData.containsKey(plotId);
    }

    public boolean isOwner(PlotId plotId) {
        PlotMeta meta = plotData.get(plotId);
        return meta != null && meta.isPresent() && meta.isOwner();
    }

    public Builder toBuilder() {
        return builder().uuid(uuid).world(world).plot(plotData);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID uuid = null;
        private String world = null;
        private final Map<PlotId, PlotMeta> plotData = new HashMap<>();

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

        public PlotUser build() {
            return new PlotUser(this);
        }
    }
}
