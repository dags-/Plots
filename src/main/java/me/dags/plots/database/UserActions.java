package me.dags.plots.database;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.dags.commandbus.utils.Format;
import me.dags.commandbus.utils.StringUtils;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotSchema;
import me.dags.plots.plot.PlotUser;
import org.bson.Document;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

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

                List<Text> lines = list.stream().map(plot -> {
                    Format.MessageBuilder line = format.info(" - ").stress(plot);
                    PlotActions.findPlotAlias(database, PlotId.parse(plot.toString())).ifPresent(alias -> line.stress(" ({})", alias));
                    line.action(TextActions.runCommand(StringUtils.format("/plot tp {} {}", database.getWorld(), plot)));
                    return line.build();
                }).collect(Collectors.toList());

                builder.contents(lines);
            }
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
