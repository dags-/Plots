package me.dags.plots.plot;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class PlotUser {

    private static final UUID DUMMY = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final UUID uuid;
    private final PlotMask mask;
    private final boolean approved;

    private PlotUser(Builder builder) {
        this.uuid = builder.uuid;
        this.mask = builder.mask;
        this.approved = builder.approved;
    }

    public UUID uuid() {
        return uuid;
    }

    public Optional<String> name() {
        return userName(uuid());
    }

    public PlotMask plotMask() {
        return mask;
    }

    public boolean approved() {
        return approved;
    }

    public boolean hasPlot() {
        return countPlots() > 0;
    }

    public int countPlots() {
        return plotMask().plots().size();
    }

    public Builder edit() {
        return builder()
                .uuid(uuid)
                .plots(mask.plots().keySet())
                .approved(approved);
    }

    @Override
    public String toString() {
        return "id=" + uuid + ",approved=" + approved + ",mask=" + mask;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID uuid = DUMMY;
        private boolean approved = false;
        private PlotSchema plotSchema = null;
        private Set<PlotId> plots = new HashSet<>();
        private PlotMask mask = PlotMask.EMPTY;

        public Builder approved(boolean approved) {
            this.approved = approved;
            return this;
        }

        public Builder mask(PlotMask mask) {
            this.mask = mask;
            return this;
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder schema(PlotSchema schema) {
            this.plotSchema = schema;
            return this;
        }

        public Builder plot(PlotId plotId) {
            this.plots.add(plotId);
            return this;
        }

        public Builder plots(Iterable<PlotId> plots) {
            plots.forEach(this.plots::add);
            return this;
        }

        public PlotUser build() {
            if (mask == PlotMask.EMPTY) {
                mask = plotSchema != null ? PlotMask.of(plotSchema, plots) : PlotMask.EMPTY;
            }
            return new PlotUser(this);
        }
    }

    static Optional<String> userName(UUID uuid) {
        if (uuid == DUMMY) {
            return Optional.empty();
        }
        return Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid).map(User::getName);
    }
}
