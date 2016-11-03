package me.dags.plotsconv;

import com.mongodb.MongoClient;
import me.dags.plots.database.PlotActions;
import me.dags.plots.database.UserActions;
import me.dags.plots.database.WorldDatabase;
import me.dags.plots.plot.PlotId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public final class Converter {

    private static final String UID = "uid";
    private static final String USER_ID = "user_id";
    private static final String PLOT_ID = "plot_id";
    private static final String META_NAME = "meta_name";
    private static final String META_OWNER = "meta_owner";
    private static final String META_APPROVED = "meta_approved";
    private static final Logger logger = LoggerFactory.getLogger("PLOTS_CONVERTER");

    private final String database;
    private final MongoClient client;

    private DataSource dataSource;

    public Converter(MongoClient client, String database) {
        this.client = client;
        this.database = database;
    }

    public void convert() {
        try {
            DatabaseMetaData metaData = getDataSource().getConnection().getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                convertTable(tables.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DataSource getDataSource() throws SQLException {
        if (dataSource == null) {
            dataSource = Sponge.getServiceManager().provideUnchecked(SqlService.class).getDataSource(database);
        }
        return dataSource;
    }

    private void convertTable(String name) throws SQLException {
        logger.info("Converting table: {}", name);

        WorldDatabase database = new WorldDatabase(client.getDatabase(name));

        try (Connection connection = getDataSource().getConnection()) {
            // Select all entries in the table
            String select = "SELECT * FROM `" + name + "`";

            ResultSet resultSet = connection.createStatement().executeQuery(select);
            while (resultSet.next()) {
                String userId = resultSet.getString(USER_ID);
                String plotId = resultSet.getString(PLOT_ID);
                String owner = resultSet.getString(META_OWNER);
                boolean approved = resultSet.getBoolean(META_APPROVED);

                // Found a user id and a valid PlotId
                if (userId != null && plotId != null && PlotId.isValid(plotId)) {
                    UUID uuid = UUID.fromString(userId);
                    PlotId plot = PlotId.parse(plotId);

                    // Add plot to user's plots
                    UserActions.addPlot(database, uuid, plot);

                    boolean isOwner = false;
                    // If user owns this plot...
                    if (isOwner = (owner != null && owner.equalsIgnoreCase(userId))) {
                        // Set user as owner
                        PlotActions.setPlotOwner(database, plot, uuid);

                        // If this plot was approved
                        if (approved) {
                            // Set user as approved
                            UserActions.setApproved(database, uuid, true);
                        }
                    }

                    logger.info("Converted entry for user={} plot={} owner={} approved={}", uuid, plot, isOwner, approved);
                }
            }
        }
    }
}
