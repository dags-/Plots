package me.dags.plots.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.dags.commandbus.utils.Format;
import me.dags.plots.plot.PlotId;
import org.bson.Document;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class PlotActions {

    private static final UpdateOptions IGNORE = new UpdateOptions().upsert(false);
    private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

    public static void setPlotOwner(WorldDatabase database, PlotId plotId, UUID owner) {
        Document update = new Document("$set", new Document(Keys.PLOT_OWNER, owner.toString()));
        database.plotCollection().updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), update, UPSERT);
    }

    public static void setPlotAlias(WorldDatabase database, PlotId plotId, String alias) {
        Document update = new Document("$set", new Document(Keys.PLOT_ALIAS, alias.toLowerCase()));
        database.plotCollection().updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), update, UPSERT);
    }

    public static void addLike(WorldDatabase database, PlotId plotId, UUID liker) {
        Document update = new Document("$addToSet", new Document(Keys.PLOT_LIKES, liker.toString()));
        database.plotCollection().updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), update, IGNORE);
    }

    public static void removeLike(WorldDatabase database, PlotId plotId, UUID liker) {
        Document update = new Document("$pull", new Document(Keys.PLOT_LIKES, liker.toString()));
        database.plotCollection().updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), update, IGNORE);
    }

    public static void removePlot(WorldDatabase database, PlotId plotId) {
        database.plotCollection().findOneAndDelete(Filters.eq(Keys.PLOT_ID, plotId.toString()));
    }

    public static PlotId plotFromAlias(WorldDatabase database, String alias) {
        Document first = database.plotCollection().find(Filters.eq(Keys.PLOT_ALIAS, alias.toLowerCase())).first();
        if (first != null) {
            return PlotId.parse(first.getString(Keys.PLOT_ID));
        }
        return PlotId.EMPTY;
    }

    public static Optional<UUID> findPlotOwner(WorldDatabase database, PlotId plotId) {
        Document first = database.plotCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();
        if (first != null && first.containsKey(Keys.PLOT_OWNER)) {
            UUID uuid = UUID.fromString(first.getString(Keys.PLOT_OWNER));
            return Optional.of(uuid);
        }
        return Optional.empty();
    }

    public static Optional<String> findPlotAlias(WorldDatabase database, PlotId plotId) {
        Document first = database.plotCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();
        if (first != null && first.containsKey(Keys.PLOT_ALIAS)) {
            return Optional.ofNullable(first.getString(Keys.PLOT_ALIAS));
        }
        return Optional.empty();
    }

    public static Optional<List<UUID>> findPlotLikes(WorldDatabase database, PlotId plotId) {
        Document first = database.plotCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();
        if (first != null && first.containsKey(Keys.PLOT_LIKES)) {
            List<?> list = first.get(Keys.PLOT_LIKES, List.class);
            return Optional.of(list.stream().map(Object::toString).map(UUID::fromString).collect(Collectors.toList()));
        }
        return Optional.empty();
    }

    // PlotID: x:z (alias) | Owner: Name | Likes: #
    public static Text plotInfo(WorldDatabase database, PlotId plotId, Format format) {
        Format.MessageBuilder builder = format.message().info("Plot: ").stress(plotId);
        Document first = database.plotCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();

        if (first != null) {
            if (first.containsKey(Keys.PLOT_ALIAS)) {
                String alias = first.getString(Keys.PLOT_ALIAS);
                if (alias != null && !alias.isEmpty()) {
                    builder.info(" (").stress(alias).subdued(")");
                }
            }
            if (first.containsKey(Keys.PLOT_OWNER)) {
                String id = first.getString(Keys.PLOT_OWNER);
                UUID uuid = UUID.fromString(id);
                Sponge.getServiceManager()
                        .provideUnchecked(UserStorageService.class)
                        .get(uuid)
                        .ifPresent(user -> builder.info(", Owner: ").stress(user.getName()));
            }
            if (first.containsKey(Keys.PLOT_LIKES)) {
                List<?> list = first.get(Keys.PLOT_LIKES, List.class);
                builder.info(", Likes: ").stress(list.size());
            }
        }

        String command = "/plot tp " + database.getWorld() + " " + plotId.toString();
        return builder.build().toBuilder().onClick(TextActions.runCommand(command)).build();
    }

    public static PlotId findNextFreePlot(WorldDatabase database, PlotId closest) {
        MongoCollection collection = database.plotCollection();
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
