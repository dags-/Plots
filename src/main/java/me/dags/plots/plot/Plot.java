package me.dags.plots.plot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Plot {

    public static final Plot EMPTY = builder().build();

    private final PlotId plotId;
    private final PlotBounds bounds;
    private final UUID owner;
    private final Set<UUID> whitelisted = new HashSet<>();

    private Plot(Builder builder) {
        this.plotId = builder.plotId;
        this.bounds = builder.bounds;
        this.owner = builder.owner;
    }

    public boolean present() {
        return this != EMPTY;
    }

    public PlotId plotId() {
        return plotId;
    }

    public boolean isWhitelisted(UUID uuid) {
        return owner.equals(uuid) || whitelisted.contains(uuid);
    }

    public PlotBounds getBounds() {
        return bounds;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof Plot && this.hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        return plotId.hashCode();
    }

    public static class Builder {

        private PlotId plotId;
        private PlotBounds bounds;
        private UUID owner;

        public Builder id(PlotId id) {
            this.plotId = id;
            return this;
        }

        public Builder bounds(PlotBounds bounds) {
            this.bounds = bounds;
            return this;
        }

        public Builder owner(UUID id) {
            this.owner = id;
            return this;
        }

        public Plot build() {
            return new Plot(this);
        }
    }
}
