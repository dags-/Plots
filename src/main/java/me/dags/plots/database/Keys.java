package me.dags.plots.database;

import me.dags.plots.plot.PlotId;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Keys {

    public static final String UID = "uid";
    public static final String USER_ID = "user_id";
    public static final String PLOT_ID = "plot_id";
    public static final String META_NAME = "meta_name";
    public static final String META_OWNER = "meta_owner";
    public static final String META_APPROVED = "meta_approved";

    static String uid(UUID uuid, PlotId plotId) {
        return uuid + ";" + plotId;
    }
}
