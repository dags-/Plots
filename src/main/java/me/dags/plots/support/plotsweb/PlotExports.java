package me.dags.plots.support.plotsweb;

import me.dags.plots.util.Support;

/**
 * @author dags <dags@dags.me>
 */
public class PlotExports implements Support.Hook {

    private static ExportHelper helper = new ExportsDummy();

    @Override
    public void init() {
        helper = new ExportsImpl();
    }

    public static ExportHelper getHelper() {
        return helper;
    }
}
