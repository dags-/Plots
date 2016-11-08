package me.dags.plots.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.dags.commandbus.utils.Format;
import me.dags.commandbus.utils.StringUtils;
import me.dags.plots.plot.PlotId;
import me.dags.plots.util.CountList;
import me.dags.plots.util.Pair;
import org.bson.Document;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.ArrayList;
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

    public static List<PlotId> findOwnedPlots(WorldDatabase database, UUID owner, PlotId from, int range) {
        PlotId min = PlotId.of(from.plotX() - range, from.plotZ() - range);
        PlotId max = PlotId.of(from.plotX() + range, from.plotZ() + range);
        String uuid = owner.toString();
        List<PlotId> matches = new ArrayList<>();
        for (int x = min.plotX(); x < max.plotX(); x++) {
            for (int z = min.plotZ(); z < max.plotZ(); z++) {
                PlotId test = PlotId.of(x, z);
                Document first = database.plotCollection().find(Filters.eq(Keys.PLOT_ID, test.toString())).first();
                if (first != null && first.containsKey(Keys.PLOT_OWNER) && first.getString(Keys.PLOT_OWNER).equals(uuid)) {
                    matches.add(test);
                }
            }
        }
        return matches;
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
        Format.MessageBuilder builder = format.message().info("Plot: ").stress(plotId);

        if (document != null) {
            if (document.containsKey(Keys.PLOT_ALIAS)) {
                String alias = document.getString(Keys.PLOT_ALIAS);
                if (alias != null && !alias.isEmpty()) {
                    builder.info(" (").stress(alias).info(")");
                }
            }
            if (document.containsKey(Keys.PLOT_OWNER)) {
                String id = document.getString(Keys.PLOT_OWNER);
                UUID uuid = UUID.fromString(id);
                Sponge.getServiceManager()
                        .provideUnchecked(UserStorageService.class)
                        .get(uuid)
                        .ifPresent(user -> builder.info(" Owner: ").stress(user.getName()));
            }
            if (document.containsKey(Keys.PLOT_LIKES)) {
                List<?> list = document.get(Keys.PLOT_LIKES, List.class);
                builder.info(" Likes: ").stress(list.size());
            }
        }

        String command = StringUtils.format("/plot tp {} {}", world, plotId);
        return builder.action(TextActions.runCommand(command)).build();
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
