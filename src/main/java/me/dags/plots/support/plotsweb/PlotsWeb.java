package me.dags.plots.support.plotsweb;

import me.dags.plots.util.Support;

/**
 * @author dags <dags@dags.me>
 */
public class PlotsWeb implements Support.Hook {

    private static ExportHelper helper = new ServiceDummy();

    @Override
    public void init() {
        helper = new ServiceImpl();
    }

    public static ExportHelper getHelper() {
        return helper;
    }
}
