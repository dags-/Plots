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
        this.name = "";
    }

    private PlotMeta(Builder builder) {
        this.owner = builder.owner;
        this.name = builder.name;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public boolean hasMeta() {
        return isOwner() || !name.isEmpty();
    }

    public boolean hasName() {
        return !name.isEmpty();
    }

    public boolean isOwner() {
        return isPresent() && owner;
    }

    public String getName() {
        return name;
    }

    public Builder toBuilder() {
        return builder().name(name).owner(owner);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean owner = false;
        private String name = "";

        public Builder owner(boolean owner) {
            this.owner = owner;
            return this;
        }

        public Builder name(String name) {
            this.name = name == null ? "" : name;
            return this;
        }

        public PlotMeta build() {
            return new PlotMeta(this);
        }
    }
}
