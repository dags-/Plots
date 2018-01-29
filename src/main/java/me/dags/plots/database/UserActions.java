package me.dags.plots.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.dags.commandbus.fmt.Format;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotSchema;
import me.dags.plots.plot.PlotUser;
import me.dags.plots.util.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class UserActions {

    private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

    public static PlotUser loadPlotUser(WorldDatabase database, PlotSchema plotSchema, UUID userId) {
        Document first = database.userCollection().find(Filters.eq(Keys.USER_ID, userId.toString())).first();
        PlotUser.Builder builder = PlotUser.builder();
        builder.uuid(userId);
        builder.schema(plotSchema);

        if (first != null && first.containsKey(Keys.USER_PLOTS)) {
            List<?> list = first.get(Keys.USER_PLOTS, List.class);
            list.forEach(o -> {
                PlotId plotId = PlotId.parse(o.toString());
                builder.plot(plotId);

                Pair<PlotId, PlotId> merge = PlotActions.findMergeRange(database, plotId);

                if (merge.present()) {
                    builder.merge(merge);
                }
            });
        }


        Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(userId).ifPresent(user -> {
            user.getOption("plots:max_claims").ifPresent(s -> {
                try {
                    builder.claimCount(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    builder.claimCount(1);
                }
            });
        });

        return builder.build();
    }

    public static Stream<PlotId> findNearbyPlots(WorldDatabase database, UUID uuid, PlotId centre, int radius) {
        Document first = database.userCollection().find(Filters.eq(Keys.USER_ID, uuid.toString())).first();
        if (first != null) {
            if (first.containsKey(Keys.USER_PLOTS)) {
                List<?> list = first.get(Keys.USER_PLOTS, List.class);
                Predicate<PlotId> filter = id ->
                        id.plotX() >= centre.plotX() - radius
                                && id.plotX() <= centre.plotX() + radius
                                && id.plotZ() >= centre.plotZ() - radius
                                && id.plotZ() <= centre.plotZ() + radius;
                return list.stream().map(o -> PlotId.parse(o.toString())).filter(filter);
            }
        }
        return Stream.empty();
    }

    public static Stream<PlotId> findNearbySoloPlots(WorldDatabase database, UUID uuid, PlotId centre, int radius) {
        return findNearbyPlots(database, uuid, centre, radius).filter(id -> countPlotUsers(database, id) == 1);
    }

    public static PaginationList listPlots(WorldDatabase database, String title, UUID uuid, Format format) {
        PaginationList.Builder builder = PaginationList.builder();
        builder.title(format.stress(title).build());
        builder.linesPerPage(9);

        Document first = database.userCollection().find(Filters.eq(Keys.USER_ID, uuid.toString())).first();
        if (first != null) {
            if (first.containsKey(Keys.USER_PLOTS)) {
                List<?> list = first.get(Keys.USER_PLOTS, List.class);

                List<Text> lines = list.stream()
                        .map(plot -> PlotActions.plotInfo(database, PlotId.parse(plot.toString()), format))
                        .collect(Collectors.toList());

                builder.contents(lines);
            }
        } else {
            builder.contents(Collections.emptyList());
        }
        return builder.build();
    }

    public static PaginationList listLikes(WorldDatabase database, String name, UUID uuid, Format format) {
        PaginationList.Builder builder = PaginationList.builder();
        builder.title(format.stress("%s's Liked Plots", name).build());
        builder.linesPerPage(9);
        builder.contents(PlotActions.likedPlots(database, uuid, format));
        return builder.build();
    }

    public static boolean hasPlot(WorldDatabase database, UUID uuid, PlotId plotId) {
        Bson user = Filters.eq(Keys.USER_ID, uuid.toString());
        Bson plot = Filters.in(Keys.USER_PLOTS, plotId.toString());
        return database.userCollection().count(Filters.and(user, plot)) > 0;
    }

    public static long countPlotUsers(WorldDatabase database, PlotId plotId) {
        return database.userCollection().count(Filters.in(Keys.USER_PLOTS, plotId.toString()));
    }

    public static List<UUID> getWhitelisted(WorldDatabase database, PlotId plotId) {
        FindIterable<Document> search = database.userCollection().find(Filters.in(Keys.USER_PLOTS, plotId.toString()));
        List<UUID> list = new ArrayList<>();
        for (Document document : search) {
            String id = document.getString(Keys.USER_ID);
            list.add(UUID.fromString(id));
        }
        return list;
    }

    public static void addPlot(WorldDatabase database, UUID uuid, PlotId plotId) {
        Document update = new Document("$addToSet", new Document(Keys.USER_PLOTS, plotId.toString()));
        database.userCollection().updateOne(Filters.eq(Keys.USER_ID, uuid.toString()), update, UPSERT);
    }

    public static void removePlot(WorldDatabase database, UUID uuid, PlotId plotId) {
        Document update = new Document("$pull", new Document(Keys.USER_PLOTS, plotId.toString()));
        database.userCollection().updateOne(Filters.eq(Keys.USER_ID, uuid.toString()), update, UPSERT);
    }

    public static void removeAllPlot(WorldDatabase database, PlotId plotId) {
        String id = plotId.toString();
        Document update = new Document("$pull", new Document(Keys.USER_PLOTS, id));
        database.userCollection().updateMany(Filters.in(Keys.USER_PLOTS, id), update);
    }
}
