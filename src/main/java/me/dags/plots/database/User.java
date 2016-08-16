package me.dags.plots.database;

import me.dags.plots.database.table.PostBuilder;
import me.dags.plots.database.table.Table;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotMeta;
import me.dags.plots.plot.PlotUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
class User {

    static void userBuilderPopulator(PlotUser.Builder builder, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String id = resultSet.getString(Keys.PLOT_ID);
            boolean owner = resultSet.getBoolean(Keys.META_OWNER);

            PlotId plotId = PlotId.valueOf(id);
            PlotMeta.Builder meta = PlotMeta.builder().owner(owner);
            builder.plot(plotId, meta.build());
        }
    }

    static void plotEntryPopulator(Table table, PlotUser plotUser) throws SQLException {
        for (Map.Entry<PlotId, PlotMeta> plotData : plotUser.getPlots()) {
            PlotId plotId = plotData.getKey();
            PlotMeta plotMeta = plotData.getValue();

            String id = plotUser.getWorld() + ";" + plotId.toString();
            String plot_world = plotUser.getWorld();
            String plot_id = plotId.toString();

            PostBuilder builder = table.post();
            builder.set(Keys.ID, id)
                    .set(Keys.PLOT_WORLD, plot_world)
                    .set(Keys.PLOT_ID, plot_id);

            if (plotMeta.isPresent()) {
                builder.set(Keys.META_OWNER, plotMeta.isOwner());
            }

            builder.submit();
        }
    }

    static Table createUserTable(String database, UUID uuid) throws SQLException {
        return Table.builder()
                .name(uuid.toString())
                .database(database)
                .column(Keys.ID, "VARCHAR(150) PRIMARY KEY")
                .column(Keys.PLOT_WORLD, "VARCHAR(100)")
                .column(Keys.PLOT_ID, "VARCHAR(50)")
                .column(Keys.META_OWNER, "BOOLEAN")
                .build()
                .submit();
    }
}
