package me.dags.plots.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.dags.commandbus.format.Format;
import me.dags.commandbus.format.Formatter;
import me.dags.plots.plot.PlotId;
import me.dags.plots.util.CountList;
import me.dags.plots.util.Pair;
import org.bson.Document;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.*;
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

    public static Collection<PlotId> findOwnedPlots(WorldDatabase database, Collection<PlotId> lookup, UUID owner) {
        Set<PlotId> owned = new HashSet<>();
        for (PlotId plotId : lookup) {
            Optional<UUID> plotOwner = findPlotOwner(database, plotId);
            if (plotOwner.isPresent() && plotOwner.get().equals(owner)) {
                owned.add(plotId);
            }
        }
        return owned;
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

    public static Pair<Boolean, Text> mergePlots(WorldDatabase database, Format format, UUID uuid, PlotId from, PlotId to) {
        MongoCollection<Document> collection = database.plotCollection();
        String owner = uuid.toString();

        for (int x = from.plotX(); x <= to.plotX(); x++) {
            for (int z = from.plotZ(); z <= to.plotZ(); z++) {
                PlotId plotId = PlotId.of(x, z);
                Document first = collection.find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();

                // Plot is owned
                if (first != null && first.containsKey(Keys.PLOT_OWNER)) {
                    // Plot owner is someone else
                    if (!first.getString(Keys.PLOT_OWNER).equals(owner)) {
                        Text err = format.error("Plot ").stress(plotId)
                                .error(" is within merge range ").stress("{} <> {}", from, to)
                                .error(" but is owned by someone else").build();
                        return Pair.of(false, err);
                    }

                    // Plot belongs to existing merge group
                    if (first.containsKey(Keys.PLOT_MERGE_MIN) && first.containsKey(Keys.PLOT_MERGE_MAX)) {
                        PlotId min = PlotId.parse(first.getString(Keys.PLOT_MERGE_MIN));
                        PlotId max = PlotId.parse(first.getString(Keys.PLOT_MERGE_MAX));

                        // Existing merge is not contained by new merge
                        if (min.plotX() < from.plotX() || min.plotX() > to.plotX() || min.plotZ() < from.plotZ() || max.plotZ() > to.plotZ()) {
                            Text error = format.stress(plotId)
                                    .error(" has already been merged in range: ").stress("{} <> {}", min, max)
                                    .error(". Your new merge must contain this range").build();
                            return Pair.of(false, error);
                        }
                    }
                }
            }
        }

        int count = 0;
        for (int x = from.plotX(); x <= to.plotX(); x++) {
            for (int z = from.plotZ(); z <= to.plotZ(); z++) {
                count++;
                PlotId plotId = PlotId.of(x, z);
                collection.updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), new Document("$set", new Document(Keys.PLOT_OWNER, owner)), UPSERT);
                collection.updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), new Document("$set", new Document(Keys.PLOT_MERGE_MIN, from.toString())), UPSERT);
                collection.updateOne(Filters.eq(Keys.PLOT_ID, plotId.toString()), new Document("$set", new Document(Keys.PLOT_MERGE_MAX, to.toString())), UPSERT);
                UserActions.addPlot(database, uuid, plotId);
            }
        }

        return Pair.of(true, format.info("Merged ").stress(count).info(" plots").build());
    }

    public static void removeMerge(WorldDatabase database, PlotId plotId) {
        Pair<PlotId, PlotId> range = findMergeRange(database, plotId);
        if (range.present()) {
            MongoCollection plots = database.plotCollection();

            // TODO: test!
            Document unset = new Document()
                    .append("$unset", Keys.PLOT_MERGE_MIN)
                    .append("$unset", Keys.PLOT_MERGE_MAX);

            for (int x = range.first().plotX(); x <= range.second().plotX(); x++) {
                for (int z = range.first().plotZ(); z <= range.second().plotZ(); z++) {
                    plots.updateOne(Filters.eq(Keys.PLOT_ID, PlotId.string(x, z)), unset);
                }
            }
        }
    }

    public static Pair<PlotId, PlotId> findMergeRange(WorldDatabase database, PlotId plotId) {
        Document first = database.plotCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();
        if (first != null) {
            if (first.containsKey(Keys.PLOT_MERGE_MIN) && first.containsKey(Keys.PLOT_MERGE_MAX)) {
                PlotId min = PlotId.parse(first.getString(Keys.PLOT_MERGE_MIN));
                PlotId max = PlotId.parse(first.getString(Keys.PLOT_MERGE_MAX));
                if (min.present() && max.present()) {
                    return Pair.of(min, max);
                }
            }
        }
        return Pair.empty();
    }

    public static PaginationList topPlots(WorldDatabase database, int size, Format format) {
        FindIterable<Document> all = database.plotCollection().find();
        CountList<Integer, Pair<PlotId, Document>> topLikes = new CountList<>((i1, i2) -> i1 - i2, size);
        for (Document entry : all) {
            if (entry.containsKey(Keys.PLOT_ID) && entry.containsKey(Keys.PLOT_LIKES)) {
                PlotId plotId = PlotId.parse(entry.getString(Keys.PLOT_ID));
                List likes = entry.get(Keys.PLOT_LIKES, List.class);
                topLikes.add(likes.size(), Pair.of(plotId, entry));
            }
        }

        List<Text> lines = topLikes.get()
                .map(p -> plotInfo(p.first(), database.getWorld(), p.second(), format))
                .collect(Collectors.toList());

        PaginationList.Builder builder = PaginationList.builder();
        builder.title(format.stress("Top " + size + " Plots").build());
        builder.linesPerPage(9);
        builder.contents(lines);

        return builder.build();
    }

    public static List<Text> likedPlots(WorldDatabase database, UUID uuid, Format format) {
        FindIterable<Document> results = database.plotCollection().find(Filters.in(Keys.PLOT_LIKES, uuid.toString()));
        List<Text> info = new ArrayList<>();
        for (Document document : results) {
            if (document.containsKey(Keys.PLOT_ID)) {
                PlotId plotId = PlotId.parse(document.getString(Keys.PLOT_ID));
                info.add(plotInfo(plotId, database.getWorld(), document, format));
            }
        }
        return info;
    }

    public static Text plotInfo(WorldDatabase database, PlotId plotId, Format format) {
        Document first = database.plotCollection().find(Filters.eq(Keys.PLOT_ID, plotId.toString())).first();
        return plotInfo(plotId, database.getWorld(), first, format);
    }

    private static Text plotInfo(PlotId plotId, String world, Document document, Format format) {
        Formatter formatter = format.message().info("Plot: ").stress(plotId);

        if (document != null) {
            if (document.containsKey(Keys.PLOT_ALIAS)) {
                String alias = document.getString(Keys.PLOT_ALIAS);
                if (alias != null && !alias.isEmpty()) {
                    formatter.info(" (").stress(alias).info(")");
                }
            }
            if (document.containsKey(Keys.PLOT_OWNER)) {
                String id = document.getString(Keys.PLOT_OWNER);
                UUID uuid = UUID.fromString(id);
                Sponge.getServiceManager()
                        .provideUnchecked(UserStorageService.class)
                        .get(uuid)
                        .map(User::getName)
                        .ifPresent(formatter.info(" Owner: ")::stress);
            }
            if (document.containsKey(Keys.PLOT_LIKES)) {
                List<?> list = document.get(Keys.PLOT_LIKES, List.class);
                formatter.info(" Likes: ").stress(list.size());
            }
        }

        String command = String.format("/plot tp %s %s", world, plotId);
        return formatter.action(TextActions.runCommand(command)).build();
    }

    public static PlotId findNextFreePlot(WorldDatabase database, PlotId closest) {
        MongoCollection collection = database.plotCollection();
        int x = closest.plotX(), z = closest.plotZ(), xMax = x, xMin = x, zMax = z, zMin = z;
        int timeout = 10000;
        for (int d = 1; d < timeout; d++) {
            xMax += d;
            xMin -= d;
            zMax += d;
            zMin -= d;
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
