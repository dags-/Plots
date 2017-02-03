package me.dags.plots.plot;

import me.dags.plots.util.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.*;
import java.util.stream.Collectors;

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
        private Set<PlotId> individualPlots = new HashSet<>();
        private Set<Pair<PlotId, PlotId>> mergedPlots = new HashSet<>();
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
            this.individualPlots.add(plotId);
            return this;
        }

        public Builder plots(Iterable<PlotId> plots) {
            plots.forEach(this.individualPlots::add);
            return this;
        }

        public Builder merge(Pair<PlotId, PlotId> merge) {
            mergedPlots.add(merge);
            return this;
        }

        public PlotUser build() {
            if (mask == PlotMask.EMPTY && plotSchema != null) {
                Map<PlotId, PlotBounds> plots = singleBounds();
                Map<PlotId, PlotBounds> merged = mergedBounds();
                plots.putAll(merged);
                mask = new PlotMask(plotSchema, plots);
            }
            return new PlotUser(this);
        }

        private Map<PlotId, PlotBounds> singleBounds() {
            return individualPlots.stream().collect(Collectors.toMap(id -> id, plotSchema::plotBounds));
        }

        private Map<PlotId, PlotBounds> mergedBounds() {
            Map<PlotId, PlotBounds> all = new HashMap<>();

            outer:
            for (Pair<PlotId, PlotId> pair : mergedPlots) {
                PlotBounds min = plotSchema.plotBounds(pair.first());
                PlotBounds max = plotSchema.plotBounds(pair.second());

                if (min.present() && max.present()) {
                    Map<PlotId, PlotBounds> merged = new HashMap<>();
                    PlotBounds bounds = new PlotBounds(min.getMin(), max.getMax());

                    for (int x = pair.first().plotX(); x <= pair.second().plotX(); x++) {
                        for (int z = pair.first().plotZ(); z <= pair.second().plotZ(); z++) {
                            PlotId plotId = PlotId.of(x, z);
                            if (!individualPlots.contains(plotId)) {
                                continue outer;
                            }
                            merged.put(plotId, bounds);
                        }
                    }
                    all.putAll(merged);
                }
            }

            return all;
        }
    }

    static Optional<String> userName(UUID uuid) {
        if (uuid == DUMMY) {
            return Optional.empty();
        }
        return Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid).map(User::getName);
    }
}
