package me.dags.plots.support.converse;

import me.dags.plots.support.converse.conversations.Setup;
import me.dags.plots.util.Support;

/**
 * @author dags <dags@dags.me>
 */
public class ConverseSupport implements Support.Hook {

    @Override
    public void init() {
        Conversations.getInstance().register("setup", new Setup());
    }
}
