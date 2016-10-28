package me.dags.plots.database;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
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

    public static PlotUser loadPlotUser(MongoCollection<Document> collection, PlotSchema plotSchema, UUID userId) {
        Document first = collection.find(Filters.eq(Keys.USER_ID, userId.toString())).first();
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

    public static void savePlotUser(MongoCollection<Document> collection, PlotUser plotUser) {
        String id = plotUser.uuid().toString();

        Document plots = new Document();
        plotUser.plotMask().plots().keySet().forEach(plotId -> plots.put(plotId.toString(), new BasicDBObject()));

        Document user = new Document();
        user.put(Keys.USER_ID, id);
        user.put(Keys.USER_APPROVED, plotUser.approved());
        user.put(Keys.USER_PLOTS, plots);

        collection.replaceOne(Filters.eq(Keys.USER_ID, id), user, UPSERT);
    }

    public static void setApproved(MongoCollection<Document> collection, UUID uuid, boolean approved) {
        Document update = new Document("$set", new Document(Keys.USER_APPROVED, approved));
        collection.updateOne(Filters.eq(Keys.USER_ID, uuid.toString()), update, UPSERT);
    }

    public static void addPlot(MongoCollection<Document> collection, UUID uuid, PlotId plotId) {
        Document update = new Document("$addToSet", new Document(Keys.USER_PLOTS, plotId.toString()));
        collection.updateOne(Filters.eq(Keys.USER_ID, uuid.toString()), update, UPSERT);
    }

    public static void removePlot(MongoCollection<Document> collection, UUID uuid, PlotId plotId) {
        Document update = new Document("$pull", new Document(Keys.USER_PLOTS, plotId.toString()));
        collection.updateOne(Filters.eq(Keys.USER_ID, uuid.toString()), update, UPSERT);
    }

    public static void removeAllPlot(MongoCollection<Document> collection, PlotId plotId) {
        String id = plotId.toString();
        Document update = new Document("$pull", new Document(Keys.USER_PLOTS, id));
        collection.updateMany(Filters.in(Keys.USER_PLOTS, id), update);
    }
}
