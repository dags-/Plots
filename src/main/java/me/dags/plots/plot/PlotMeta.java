package me.dags.plots.plot;

/**
 * @author dags <dags@dags.me>
 */
public class PlotMeta {

    public static final PlotMeta EMPTY = new PlotMeta();

    private final boolean owner;
    private final String name;

    private PlotMeta() {
        this.owner = false;
        this.name = null;
    }

    private PlotMeta(Builder builder) {
        this.owner = builder.owner;
        this.name = builder.name;
    }
    public boolean isPresent() {
        return this != EMPTY;
    }

    public boolean hasMeta() {
        return isOwner() || name != null;
    }

    public boolean isOwner() {
        return isPresent() && owner;
    }

    public String getName() {
        return isPresent() ? name : null;
    }

    public Builder toBuilder() {
        return isPresent() ? builder().name(name).owner(owner) : builder();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean owner = false;
        private String name = null;

        public Builder owner(boolean b) {
            owner = b;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public PlotMeta build() {
            return new PlotMeta(this);
        }
    }
}
