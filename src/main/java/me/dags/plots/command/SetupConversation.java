package me.dags.plots.command;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.commandbus.format.Formatter;
import me.dags.converse.*;
import me.dags.plots.Permissions;
import me.dags.plots.command.gen.GenReload;
import me.dags.plots.command.gen.GenSave;
import me.dags.plots.command.world.WorldCreate;
import me.dags.plots.generator.Defaults;
import me.dags.plots.generator.GeneratorProperties;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.spongepowered.api.command.args.GenericArguments.*;

/**
 * @author dags <dags@dags.me>
 */
public class SetupConversation {

    private final ConversationSpec conversation = worldConversation(1, TimeUnit.MINUTES);

    @Command(alias = "setup", parent = "plot")
    @Permission(Permissions.PLOT_SETUP)
    @Description("Start a 'conversation' to set up a new plot world from scratch")
    public void createWorld(@Caller CommandSource source) throws CommandException {
        conversation.startConversation(source);
    }

    private static ConversationSpec worldConversation(int time, TimeUnit unit) {
        ConversationNode intro = ConversationNode.route("intro")
                .parameters(string(id("response")))
                .prompt((src, context) -> FMT
                        .info("Please answer the following set of questions/prompts by typing directly into chat")
                        .newLine().info("Your responses will not be sent to any other players on the server")
                        .newLine().info("This conversation will timeout after ").stress("%s %s", time, unit.name())
                        .newLine().info("You may exit the conversation at any point by replying '").stress("exit").info("'")
                        .newLine()
                        .newLine().info("Type anything into to chat to continue...")
                        .build()
                )
                .router((src, context) -> ConversationRoute.goTo("world_name"))
                .build();

        ConversationNode worldName = ConversationNode.route("world_name")
                .parameters(remainingJoinedStrings(id("name")))
                .prompt((src, context) -> FMT.info("What would you like to call the world?").build())
                .router((src, context) -> ConversationRoute.goTo("generator_name"))
                .build();

        ConversationNode generatorName = ConversationNode.route("generator_name")
                .parameters(string(id("name")))
                .prompt((src, context) -> FMT.info("What would you like to call the generator?").build())
                .router((src, context) -> {
                    String name = context.getCurrent().<String>getLast("name").orElse("default");
                    getPropertiesBuilder(context).name(name);
                    return ConversationRoute.goTo("plot_size");
                })
                .build();

        ConversationNode plotDims = ConversationNode.route("plot_size")
                .parameters(integer(id("x_width")), integer(id("z_width")))
                .prompt((src, context) -> FMT.info("What x*z dimensions should plots be?").build())
                .router((src, context) -> {
                    int x = context.getCurrent().<Integer>getLast("x_width").orElse(0);
                    int z = context.getCurrent().<Integer>getLast("z_width").orElse(0);
                    if (x > 0 && z > 0) {
                        getPropertiesBuilder(context).xWidth(x).zWidth(z);
                        return ConversationRoute.goTo("path_width");
                    }
                    FMT.error("Plot dimensions must be greater than 0").tell(src);
                    return ConversationRoute.goTo("plot_size");
                })
                .build();

        ConversationNode pathWidth = ConversationNode.route("path_width")
                .parameters(integer(id("width")))
                .prompt((src, context) -> FMT.info("How many blocks wide should the paths be?").build())
                .router((src, context) -> {
                    int width = context.getCurrent().<Integer>getLast("width").orElse(0);
                    if (width > 0) {
                        getPropertiesBuilder(context).pathWidth(width);
                        return ConversationRoute.goTo("wall_width");
                    }
                    FMT.error("Path width must be greater than 0").tell(src);
                    return ConversationRoute.goTo("path_width");
                })
                .build();

        ConversationNode wallWidth = ConversationNode.route("wall_width")
                .parameters(integer(id("width")))
                .prompt((src, context) -> FMT.info("How many blocks wide should the plot walls be?").build())
                .router((src, context) -> {
                    int width = context.getCurrent().<Integer>getLast("width").orElse(0);
                    if (width > 0) {
                        getPropertiesBuilder(context).wallWidth(width);
                        return ConversationRoute.goTo("add_layer");
                    }
                    FMT.error("Path width must be greater than 0").tell(src);
                    return ConversationRoute.goTo("wall_width");
                })
                .build();

        ConversationNode addLayer = ConversationNode.route("add_layer")
                .parameters(string(id("response")))
                .prompt((src, context) -> {
                    if (context.hasRoute("layer_id")) {
                        return FMT.info("Would you like to add another layer?").build();
                    }
                    return FMT.info("Would you like to add a layer?").build();
                })
                .router((src, context) -> {
                    String response = context.getCurrent().<String>getLast("response").orElse("");
                    if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
                        return ConversationRoute.goTo("plot_material");
                    }
                    if (response.equalsIgnoreCase("no") || response.equalsIgnoreCase("n")) {
                        if (!context.hasRoute("layer_height")) {
                            FMT.info("Using default generator layers...").tell(src);
                            getPropertiesBuilder(context).layer(Defaults.defaultLayers());
                        }
                        return ConversationRoute.goTo("biome");
                    }
                    FMT.error("Please answer yes or no").tell(src);
                    return ConversationRoute.goTo("add_layer");
                })
                .build();

        ConversationNode plotMaterial = ConversationNode.route("plot_material")
                .parameters(catalogedElement(id("material"), BlockState.class))
                .prompt((src, context) -> {
                    int layer = context.<Integer>getLast("layer_id", "id").orElse(1);
                    return FMT.info("What material should the plots be in layer %s?", layer).build();
                })
                .router(ConversationRoute.goTo("wall_material"))
                .build();

        ConversationNode wallMaterial = ConversationNode.route("wall_material")
                .parameters(catalogedElement(id("material"), BlockState.class))
                .prompt((src, context) -> {
                    int layer = context.<Integer>getLast("layer_id", "id").orElse(1);
                    return FMT.info("What material should the walls be in layer %s?", layer).build();
                })
                .router(ConversationRoute.goTo("path_material"))
                .build();

        ConversationNode pathMaterial = ConversationNode.route("path_material")
                .parameters(catalogedElement(id("material"), BlockState.class))
                .prompt((src, context) -> {
                    int layer = context.<Integer>getLast("layer_id", "id").orElse(1);
                    return FMT.info("What material should the paths be in layer %s?", layer).build();
                })
                .router(ConversationRoute.goTo("layer_height"))
                .build();

        ConversationNode layerHeight = ConversationNode.route("layer_height")
                .parameters(integer(id("height")))
                .prompt((src, context) -> FMT.info("How many blocks high should this layer be?").build())
                .router((src, context) -> {
                    int depth = context.getCurrent().<Integer>getLast("height").orElse(-1);
                    if (depth > 0 && depth < 256) {
                        Optional<ConversationContext> id = context.getLastContext("layer_id");

                        if (id.isPresent()) {
                            int layer = id.get().<Integer>getLast("id").orElse(1) + depth;
                            id.get().putArg("id", layer);
                        } else {
                            ConversationContext layerId = new ConversationContext();
                            layerId.putArg("id", 1 + depth);
                            context.putContext("layer_id", layerId);
                        }

                        GeneratorProperties.Builder builder = getPropertiesBuilder(context);
                        BlockState fallback = BlockTypes.BEDROCK.getDefaultState();
                        BlockState plot = context.<BlockState>getLast("plot_material", "material").orElse(fallback);
                        BlockState wall = context.<BlockState>getLast("wall_material", "material").orElse(fallback);
                        BlockState path = context.<BlockState>getLast("path_material", "material").orElse(fallback);
                        builder.layer(plot, wall, path, depth);

                        return ConversationRoute.goTo("add_layer");
                    } else {
                        FMT.error("Please specify a value between 1 & 255").tell(src);
                        return ConversationRoute.goTo("layer_height");
                    }
                })
                .build();

        ConversationNode biome = ConversationNode.route("biome")
                .parameters(string(id("biome")))
                .prompt((src, context) -> FMT.info("What biome should be used in the world?").build())
                .router((src, context) -> ConversationRoute.goTo("default_gamerules"))
                .build();

        ConversationNode defaultGameRules = ConversationNode.route("default_gamerules")
                .parameters(string(id("default_gamerules")))
                .prompt((src, context) -> FMT.info("Do you want to use the default game-rules?").build())
                .router((src, context) -> {
                    String response = context.getCurrent().<String>getLast("default_gamerules").orElse("");
                    if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
                        getPropertiesBuilder(context).defaultGameRules();
                        return ConversationRoute.goTo("add_gamerule");
                    }
                    if (response.equalsIgnoreCase("no") || response.equalsIgnoreCase("n")) {
                        return ConversationRoute.goTo("add_gamerule");
                    }
                    return ConversationRoute.goTo("default_gamerules");
                })
                .build();

        ConversationNode addGameRule = ConversationNode.route("add_gamerule")
                .parameters(string(id("add_gamerule")))
                .prompt((src, context) -> {
                    if (context.hasRoute("rule")) {
                        return FMT.info("Would you like to set another game-rule?").build();
                    } else {
                        return FMT.info("Would you like to set a custom game-rule?").build();
                    }
                })
                .router((src, contexts) -> {
                    String response = contexts.getCurrent().<String>getLast("add_gamerule").orElse("");
                    if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
                        return ConversationRoute.goTo("gamerule");
                    }
                    if (response.equalsIgnoreCase("no") || response.equalsIgnoreCase("n")) {
                        return ConversationRoute.goTo("confirm");
                    }
                    FMT.error("Response '%s' was not recognised, please answer yes or no", response).tell(src);
                    return ConversationRoute.goTo("add_gamerule");
                })
                .build();

        ConversationNode gamerule = ConversationNode.route("gamerule")
                .parameters(string(id("rule")), string(id("value")))
                .prompt((src, context) -> FMT.info("Please specify a gamerule and value").build())
                .router((src, contexts) -> {
                    String rule = contexts.getCurrent().<String>getLast("rule").orElse("");
                    String value = contexts.getCurrent().<String>getLast("value").orElse("");
                    if (!rule.isEmpty() && !value.isEmpty()) {
                        return ConversationRoute.goTo("add_gamerule");
                    }
                    FMT.error("Did not recognise rule '%s=%s'", rule, value).tell(src);
                    return ConversationRoute.goTo("add_gamerule");
                })
                .build();

        ConversationNode confirm = ConversationNode.route("confirm")
                .parameters(string(id("confirm")))
                .prompt((src, context) -> {
                    GeneratorProperties.Builder builder = getPropertiesBuilder(context);
                    Formatter fmt = FMT.info("Summary").newLine();
                    fmt.info("World Name: ").stress(context.getLast("world_name", "name").orElse("")).newLine();
                    fmt.info("Generator Name: ").stress(builder.getName()).newLine();
                    fmt.info("Plot Dimensions: ").stress(builder.getxWidth()).info("x").stress(builder.getzWidth()).newLine();
                    fmt.info("Wall Width: ").stress(builder.getWallWidth()).newLine();
                    fmt.info("Path Width: ").stress(builder.getPathWidth()).newLine();
                    fmt.newLine().info("Create a new world with these properties?");
                    return fmt.build();
                })
                .router((src, contexts) -> {
                    String response = contexts.getCurrent().<String>getLast("confirm").orElse("");
                    if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
                        return ConversationRoute.end();
                    }
                    if (response.equalsIgnoreCase("no") || response.equalsIgnoreCase("n")) {
                        FMT.subdued("Cancelled generation of new world").tell(src);
                        return ConversationRoute.goTo("cancel");
                    }
                    FMT.error("Please answer yes or no").tell(src);
                    return ConversationRoute.goTo("confirm");
                })
                .build();

        return ConversationSpec.builder()
                .onComplete(conversation -> conversation.getSource().ifPresent(src -> {
                    String world = conversation.getContext().<String>getLast("world_name", "world_name").orElse("");
                    FMT.info("Generating new world ").stress(world).tell(src);
                    FMT.info("Use the command ").stress("/wtp %s", world).info(" to teleport there").tell(src);
                    GeneratorProperties properties = getPropertiesBuilder(conversation.getContext()).build();
                    GenSave.saveGenerator(src, properties);
                    GenReload.reloadGenerators(src);
                    WorldCreate.createWorld(src, properties.name(), world);
                }))
                .timeOut(time, unit)
                .first(intro)
                .nodes(
                        worldName,
                        generatorName,
                        plotDims,
                        pathWidth,
                        wallWidth,
                        addLayer,
                        plotMaterial,
                        pathMaterial,
                        wallMaterial,
                        layerHeight,
                        biome,
                        defaultGameRules,
                        addGameRule,
                        gamerule,
                        confirm
                )
                .build();
    }

    private static Text id(String name) {
        return Text.of(name);
    }

    private static GeneratorProperties.Builder getPropertiesBuilder(ContextCollection context) {
        Optional<GeneratorProperties.Builder> builder = context.getOne("root", "property_builder");
        if (!builder.isPresent()) {
            GeneratorProperties.Builder b = GeneratorProperties.builder();
            ConversationContext c = new ConversationContext();
            c.putArg("property_builder", b);
            context.putContext("root", c);
            return b;
        }
        return builder.get();
    }
}
