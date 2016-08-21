package me.dags.plots.worldedit;

import me.dags.plots.Plots;

/**
 * @author dags <dags@dags.me>
 */
public class WESupport {

    public static void initialize() {
        Plots.log("Checking for WorldEdit...");
        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            Plots.log("Detected WorldEdit! Registering SessionListener");
            WESessionListener.register();
        } catch (ClassNotFoundException e) {
            Plots.log("WorldEdit not detected!");
        }
    }
}
