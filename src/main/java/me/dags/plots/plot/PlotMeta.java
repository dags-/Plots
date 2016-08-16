package me.dags.plots.plot;

/**
 * @author dags <dags@dags.me>
 */
public class PlotMeta {

    static final PlotMeta EMPTY = builder().owner(false).build();

    private final boolean isOwner;

    private PlotMeta(Builder builder) {
        this.isOwner = builder.owner;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Boolean owner = null;

        public Builder owner(boolean b) {
            owner = b;
            return this;
        }

        public PlotMeta build() {
            return owner != null ? new PlotMeta(this) : EMPTY;
        }
    }
}
