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

    static final UUID DUMMY = UUID.fromString("00000000-0000-0000-0000-000000000000");

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
        Builder builder = new Builder();
        builder.uuid = uuid;
        builder.plots = new HashSet<>(mask.plots().keySet());
        builder.approved = approved;
        return builder;
    }

    @Override
    public String toString() {
        return "id=" + uuid + ",approved=" + approved + ",mask=" + mask;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        public UUID uuid = DUMMY;
        public boolean approved = false;
        public PlotSchema plotSchema = null;
        public Set<PlotId> plots = new HashSet<>();
        private PlotMask mask = PlotMask.EMPTY;

        public PlotUser build() {
            mask = plotSchema != null ? PlotMask.of(plotSchema, plots) : PlotMask.EMPTY;
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
