package me.dags.plots.database;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotSchema;
import me.dags.plots.plot.PlotUser;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

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

    public static void savePlotUser(WorldDatabase database, PlotUser plotUser) {
        String id = plotUser.uuid().toString();

        Document plots = new Document();
        plotUser.plotMask().plots().keySet().forEach(plotId -> plots.put(plotId.toString(), new BasicDBObject()));

        Document user = new Document();
        user.put(Keys.USER_ID, id);
        user.put(Keys.USER_APPROVED, plotUser.approved());
        user.put(Keys.USER_PLOTS, plots);

        database.userCollection().replaceOne(Filters.eq(Keys.USER_ID, id), user, UPSERT);
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
