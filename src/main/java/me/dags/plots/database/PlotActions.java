package me.dags.plots.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotInfo;
import org.bson.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class PlotActions {

    private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

    public static void setPlotOwner(WorldDatabase database, PlotId plotId, UUID owner) {
        Document update = new Document("$set", new Document(Keys.PLOT_OWNER, owner.toString()));
        database.userCollection().updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), update, UPSERT);
    }

    public static void setPlotAlias(WorldDatabase database, PlotId plotId, String alias) {
        Document update = new Document("$set", new Document(Keys.PLOT_ALIAS, alias.toLowerCase()));
        database.userCollection().updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), update, UPSERT);
    }

    public static void addComment(WorldDatabase database, PlotId plotId, String comment) {
        Document update = new Document("$push", new Document(Keys.PLOT_COMMENTS, comment));
        database.userCollection().updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), update, UPSERT);
    }

    public static void removePlot(WorldDatabase database, PlotId plotId) {
        database.userCollection().findOneAndDelete(Filters.eq(Keys.PLOT_ID, plotId.toString()));
    }

    public static PlotId plotFromAlias(WorldDatabase database, String alias) {
        Document first = database.userCollection().find(Filters.eq(Keys.PLOT_ALIAS, alias.toLowerCase())).first();
        if (first != null) {
            return PlotId.parse(first.getString(Keys.PLOT_ID));
        }
        return PlotId.EMPTY;
    }

    public static Optional<UUID> findPlotOwner(WorldDatabase database, PlotId plotId) {
        Document first = database.userCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();
        if (first != null && first.containsKey(Keys.PLOT_OWNER)) {
            UUID uuid = UUID.fromString(first.getString(Keys.PLOT_OWNER));
            return Optional.of(uuid);
        }
        return Optional.empty();
    }

    public static Optional<String> findPlotAlias(WorldDatabase database, PlotId plotId) {
        Document first = database.userCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();
        if (first != null && first.containsKey(Keys.PLOT_ALIAS)) {
            return Optional.ofNullable(first.getString(Keys.PLOT_ALIAS));
        }
        return Optional.empty();
    }

    public static Optional<List<String>> findPlotComments(WorldDatabase database, PlotId plotId) {
        Document first = database.userCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();
        if (first != null && first.containsKey(Keys.PLOT_COMMENTS)) {
            List<?> list = first.get(Keys.PLOT_COMMENTS, List.class);
            return Optional.of(list.stream().map(Object::toString).collect(Collectors.toList()));
        }
        return Optional.empty();
    }

    public static PlotInfo plotInfo(WorldDatabase database, PlotId plotId) {
        PlotInfo.Builder builder = PlotInfo.builder();
        builder.plotId = plotId;

        Document first = database.userCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();
        if (first != null) {
            if (first.containsKey(Keys.PLOT_OWNER)) {
                builder.ownerId = UUID.fromString(first.getString(Keys.PLOT_OWNER));
            }
            if (first.containsKey(Keys.PLOT_ALIAS)) {
                builder.alias = first.getString(Keys.PLOT_ALIAS);
            }
        }

        return builder.build();
    }

    public static PlotId findNextFreePlot(WorldDatabase database, PlotId closest) {
        MongoCollection collection = database.userCollection();
        int x = closest.plotX(), z = closest.plotZ(), xMax = x, xMin = x, zMax = z, zMin = z;
        int timeout = 10000;
        for (int d = 1; d < timeout; d++) {
            xMax += d; xMin -= d; zMax += d; zMin -= d;
            while (x < xMax) {
                if (collection.count(Filters.eq(Keys.PLOT_ID, PlotId.string(x, z))) == 0) {
                    return new PlotId(x, z);
                }
                x++;
            }
            while (z < zMax) {
                if (collection.count(Filters.eq(Keys.PLOT_ID, PlotId.string(x, z))) == 0) {
                    return new PlotId(x, z);
                }
                z++;
            }
            while (x > xMin) {
                if (collection.count(Filters.eq(Keys.PLOT_ID, PlotId.string(x, z))) == 0) {
                    return new PlotId(x, z);
                }
                x--;
            }
            while (z > zMin) {
                if (collection.count(Filters.eq(Keys.PLOT_ID, PlotId.string(x, z))) == 0) {
                    return new PlotId(x, z);
                }
                z--;
            }
        }
        return PlotId.EMPTY;
    }
}
