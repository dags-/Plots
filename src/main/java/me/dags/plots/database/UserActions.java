package me.dags.plots.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.dags.commandbus.utils.Format;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotSchema;
import me.dags.plots.plot.PlotUser;
import org.bson.Document;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class UserActions {

    private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

    public static PlotUser loadPlotUser(WorldDatabase database, PlotSchema plotSchema, UUID userId) {
        Document first = database.userCollection().find(Filters.eq(Keys.USER_ID, userId.toString())).first();
        PlotUser.Builder builder = PlotUser.builder();
        builder.uuid = userId;
        builder.plotSchema = plotSchema;

        if (first != null) {
            if (first.containsKey(Keys.USER_APPROVED)) {
                builder.approved = first.getBoolean(Keys.USER_APPROVED);
            }
            if (first.containsKey(Keys.USER_PLOTS)) {
                List<?> list = first.get(Keys.USER_PLOTS, List.class);
                list.forEach(o -> builder.plots.add(PlotId.parse(o.toString())));
            }
        }

        return builder.build();
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

    public static boolean hasPlot(WorldDatabase database, UUID uuid, PlotId plotId) {
        Document first = database.userCollection().find(Filters.eq(Keys.USER_ID, uuid.toString())).first();
        if (first != null && first.containsKey(Keys.USER_PLOTS)) {
            List<?> list = first.get(Keys.USER_PLOTS, List.class);
            return list.stream().anyMatch(o -> o.toString().equals(plotId.toString()));
        }
        return false;
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

    public static void setApproved(WorldDatabase database, UUID uuid, boolean approved) {
        Document update = new Document("$set", new Document(Keys.USER_APPROVED, approved));
        database.userCollection().updateOne(Filters.eq(Keys.USER_ID, uuid.toString()), update, UPSERT);
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
