package me.dags.plots.util;

import me.dags.plots.PlotsPlugin;

/**
 * @author dags <dags@dags.me>
 */
public class Support implements Runnable {

    private final String name;
    private final String lookupClass;
    private final String hookClass;

    private Support(String name, String lookup, String hook) {
        this.name = name;
        this.lookupClass = lookup;
        this.hookClass = hook;
    }

    @Override
    public void run() {
        PlotsPlugin.log("Checking for {}...", name);
        try {
            Class.forName(lookupClass);
            Class<?> hook = Class.forName(hookClass);
            PlotsPlugin.log("Detected support for {}", name);
            try {
                Object object = hook.newInstance();
                if (Hook.class.isInstance(object)) {
                    Hook.class.cast(object).init();
                    PlotsPlugin.log("Initialized support for {}", name);
                } else {
                    PlotsPlugin.log("Hook class {} is not of the required type {}", hook, Hook.class);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                PlotsPlugin.log("Unable to instantiate hook class {}", hookClass);
            }
        } catch (ClassNotFoundException e) {
            PlotsPlugin.log("{} not detected. {} support disabled", lookupClass, name);
        }
    }

    public static Support of(String name, String lookupClass, String hookClass) {
        return new Support(name, lookupClass, hookClass);
    }

    public interface Hook {

        void init();
    }
}
