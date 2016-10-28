package me.dags.plots.plot;

import org.spongepowered.api.text.Text;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class PlotInfo {

    private final PlotId plotId;
    private final String alias;
    private final UUID ownerId;

    private PlotInfo(Builder builder) {
        this.plotId = builder.plotId;
        this.alias = builder.alias;
        this.ownerId = builder.ownerId;
    }

    public Text toText() {
        return Text.EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        public PlotId plotId = PlotId.EMPTY;
        public String alias = "";
        public UUID ownerId = PlotUser.DUMMY;

        public PlotInfo build() {
            return new PlotInfo(this);
        }
    }
}
