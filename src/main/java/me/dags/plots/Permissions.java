package me.dags.plots;

/**
 * @author dags <dags@dags.me>
 */
public final class Permissions {

    public static final String PLOT_AUTO = "plots.command.auto";
    public static final String PLOT_CLAIM = "plots.command.claim";
    public static final String PLOT_UNCLAIM = "plots.command.unclaim.self";
    public static final String PLOT_UNCLAIM_OTHER = "plots.command.unclaim.other";
    public static final String PLOT_INFO = "plots.command.info";
    public static final String PLOT_LIST = "plots.command.list.self";
    public static final String PLOT_LIST_OTHER = "plots.command.list.other";
    public static final String PLOT_TP = "plots.command.tp";
    public static final String PLOT_ALIAS = "plots.command.alias";
    public static final String PLOT_BIOME = "plots.command.biome";
    public static final String PLOT_COPY = "plots.command.copy";
    public static final String PLOT_MASKALL = "plots.command.maskall";
    public static final String PLOT_ADD = "plots.command.add";
    public static final String PLOT_REMOVE = "plots.command.remove.other";
    public static final String PLOT_REMOVE_ANY = "plots.command.remove.any";
    public static final String PLOT_RESET = "plots.command.reset.self";
    public static final String PLOT_RESET_OTHER = "plots.command.reset.other";
    public static final String WHITELIST_RECIPIENT = "plots.command.whitelist.recipient";
    public static final String WHITELIST_ANY = "plots.command.whitelist.any";

    public static final String GEN_EDIT = "plots.command.gen";

    public static final String WORLD_TP = "plots.command.world.tp";
    public static final String WORLD_LOAD = "plots.command.world.load";
    public static final String WORLD_UNLOAD = "plots.command.world.unload";
    public static final String WORLD_ENABLE = "plots.command.world.enable";
    public static final String WORLD_CREATE = "plots.command.world.create";
    public static final String WORLD_SPAWN = "plots.command.world.spawn";

    public static final String ACTION_BYPASS = "plots.action.bypass";
    public static final String ACTION_DAMAGE = "plots.action.damage";
    public static final String ACTION_SPAWN_INANIMATE = "plots.action.spawn.inanimate";
    public static final String ACTION_SPAWN_LIVING = "plots.action.spawn.living";
    public static final String ACTION_USE = "plots.action.item.use";
    public static final String ACTION_DROP = "plots.action.item.drop";
    public static final String ACTION_MODIFY = "plots.action.modify.block";
    public static final String ACTION_INTERACT_ENTITY = "plots.action.interact.entity";

    private Permissions(){}
}