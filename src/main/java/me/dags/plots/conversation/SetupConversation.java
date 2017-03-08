package me.dags.plots.conversation;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.commandbus.format.Formatter;
import me.dags.converse.*;
import me.dags.plots.Permissions;
import me.dags.plots.Plots;
import me.dags.plots.command.gen.GenReload;
import me.dags.plots.command.gen.GenSave;
import me.dags.plots.command.world.WorldCreate;
import me.dags.plots.generator.Defaults;
import me.dags.plots.generator.GeneratorProperties;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
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

    private final ConversationManager manager;
    private final ConversationSpec conversation;

    public SetupConversation(Plots plugin) {
        this.manager = ConversationManager.create(plugin);
        this.conversation = worldConversation(manager, 1, TimeUnit.MINUTES);
    }

    @Command(alias = "setup", parent = "plot")
    @Permission(Permissions.PLOT_SETUP)
    @Description("Start a 'conversation' to set up a new plot world from scratch")
    public void createWorld(@Caller CommandSource source) throws CommandException {
        conversation.startConversation(source);
    }

    private static ConversationSpec worldConversation(ConversationManager manager, int time, TimeUnit unit) {
        ConversationNode intro = ConversationNode.builder("intro")
                .parameter(remainingJoinedStrings(id("intro")))
                .prompt((src, context) -> FMT
                        .info("Please answer the following set of questions/prompts by typing directly into chat")
                        .newLine().info("Your responses will not be sent to any other players on the server")
                        .newLine().info("This conversation will timeout after ").stress("%s %s", time, unit.name())
                        .newLine().info("You may exit the conversation at any point by replying '").stress("exit").info("'")
                        .newLine()
                        .newLine().info("Please type anything into to chat to continue...")
                        .build()
                )
                .router((src, context) -> ConversationRoute.goTo("world_name"))
                .build();

        ConversationNode worldName = ConversationNode.builder("world_name")
                .parameter(remainingJoinedStrings(id("world_name")))
                .prompt((src, context) -> FMT.info("What would you like to call the world?").build())
                .router((src, context) -> ConversationRoute.goTo("generator_name"))
                .build();

        ConversationNode generatorName = ConversationNode.builder("generator_name")
                .parameter(string(id("generator_name")))
                .prompt((src, context) -> FMT.info("What would you like to call the generator?").build())
                .router((src, context) -> {
                    String name = context.<String>getLast("generator_name").orElse("default");
                    getPropertiesBuilder(context).name(name);
                    return ConversationRoute.goTo("plot_dims");
                })
                .build();

        ConversationNode plotDims = ConversationNode.builder("plot_dims")
                .parameter(integer(id("plot_x_width")), integer(id("plot_z_width")))
                .prompt((src, context) -> FMT.info("What x*z dimensions should the plots be?").build())
                .router((src, context) -> {
                    int x = context.<Integer>getLast("plot_x_width").orElse(0);
                    int z = context.<Integer>getLast("plot_z_width").orElse(0);
                    if (x > 0 && z >0) {
                        getPropertiesBuilder(context).xWidth(x).zWidth(z);
                        return ConversationRoute.goTo("path_width");
                    }
                    FMT.error("Plot dimensions must be greater than 0").tell(src);
                    return ConversationRoute.goTo("plot_dims");
                })
                .build();

        ConversationNode pathWidth = ConversationNode.builder("path_width")
                .parameter(integer(id("path_width")))
                .prompt((src, context) -> FMT.info("How many blocks wide should the paths be?").build())
                .router((src, context) -> {
                    int width = context.<Integer>getLast("path_width").orElse(0);
                    if (width > 0) {
                        getPropertiesBuilder(context).pathWidth(width);
                        return ConversationRoute.goTo("wall_width");
                    }
                    FMT.error("Path width must be greater than 0").tell(src);
                    return ConversationRoute.goTo("path_width");
                })
                .build();

        ConversationNode wallWidth = ConversationNode.builder("wall_width")
                .parameter(integer(id("wall_width")))
                .prompt((src, context) -> FMT.info("How many blocks wide should the plot walls be?").build())
                .router((src, context) -> {
                    int width = context.<Integer>getLast("wall_width").orElse(0);
                    if (width > 0) {
                        getPropertiesBuilder(context).wallWidth(width);
                        return ConversationRoute.goTo("add_layer");
                    }
                    FMT.error("Path width must be greater than 0").tell(src);
                    return ConversationRoute.goTo("wall_width");
                })
                .build();

        ConversationNode addLayer = ConversationNode.builder("add_layer")
                .parameter(string(id("add_layer")))
                .prompt((src, context) -> {
                    if (context.hasAny("layer_id")) {
                        return FMT.info("Would you like to add another layer?").build();
                    }
                    return FMT.info("Would you like to add a layer?").build();
                })
                .router((src, context) -> {
                    String response = context.<String>getLast("add_layer").orElse("");
                    if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
                        return ConversationRoute.goTo("plot_material");
                    }
                    if (response.equalsIgnoreCase("no") || response.equalsIgnoreCase("n")) {
                        if (!context.hasAny("layer_height")) {
                            FMT.info("Using default generator layers...").tell(src);
                            getPropertiesBuilder(context).layer(Defaults.defaultLayers());
                        }
                        return ConversationRoute.goTo("biome");
                    }
                    FMT.error("Please answer yes or no").tell(src);
                    return ConversationRoute.goTo("add_layer");
                })
                .build();

        ConversationNode plotMaterial = ConversationNode.builder("plot_material")
                .parameter(string(id("plot_material")))
                .prompt((src, context) -> {
                    int layer = context.<Integer>getLast("layer_id").orElse(1);
                    return FMT.info("What material should the plots be in layer %s?", layer).build();
                })
                .router(layer("plot_material", "wall_material"))
                .build();

        ConversationNode wallMaterial = ConversationNode.builder("wall_material")
                .parameter(string(id("wall_material")))
                .prompt((src, context) -> {
                    int layer = context.<Integer>getLast("layer_id").orElse(1);
                    return FMT.info("What material should the walls be in layer %s?", layer).build();
                })
                .router(layer("wall_material", "path_material"))
                .build();

        ConversationNode pathMaterial = ConversationNode.builder("path_material")
                .parameter(string(id("path_material")))
                .prompt((src, context) -> {
                    int layer = context.<Integer>getLast("layer_id").orElse(1);
                    return FMT.info("What material should the paths be in layer %s?", layer).build();
                })
                .router(layer("path_material", "layer_height"))
                .build();

        ConversationNode layerDepth = ConversationNode.builder("layer_height")
                .parameter(integer(id("layer_height")))
                .prompt((src, context) -> FMT.info("How many blocks high should this layer be?").build())
                .router((src, context) -> {
                    int depth = context.<Integer>getLast("layer_height").orElse(-1);
                    if (depth > 0 && depth < 256) {
                        int layer = context.<Integer>getLast("layer_id").orElse(1);
                        context.putArg("layer_id", layer + depth);

                        GeneratorProperties.Builder builder = getPropertiesBuilder(context);
                        BlockType plot = context.<BlockType>getLast("plot_material").orElse(BlockTypes.BEDROCK);
                        BlockType wall = context.<BlockType>getLast("wall_material").orElse(BlockTypes.BEDROCK);
                        BlockType path = context.<BlockType>getLast("path_material").orElse(BlockTypes.BEDROCK);
                        builder.layer(plot, wall, path, depth);

                        return ConversationRoute.goTo("add_layer");
                    } else {
                        FMT.error("Please specify a value between 1 & 255").tell(src);
                        return ConversationRoute.goTo("layer_height");
                    }
                })
                .build();

        ConversationNode biome = ConversationNode.builder("biome")
                .parameter(string(id("biome")))
                .prompt((src, context) -> FMT.info("What biome should be used in the world?").build())
                .router((src, context) -> ConversationRoute.goTo("default_gamerules"))
                .build();

        ConversationNode defaultGameRules = ConversationNode.builder("default_gamerules")
                .parameter(string(id("default_gamerules")))
                .prompt((src, context) -> FMT.info("Do you want to use the default game-rules?").build())
                .router((src, context) -> {
                    String response = context.<String>getLast("default_gamerules").orElse("");
                    if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
                        getPropertiesBuilder(context).defaultGameRules();
                        return ConversationRoute.goTo("add_gamerules");
                    }
                    if (response.equalsIgnoreCase("no") || response.equalsIgnoreCase("n")) {
                        return ConversationRoute.goTo("add_gamerules");
                    }
                    return ConversationRoute.goTo("default_gamerules");
                })
                .build();

        ConversationNode addGameRule = ConversationNode.builder("add_gamerule")
                .parameter(string(id("add_gamerule")))
                .prompt((src, context) -> {
                    if (context.hasAny("rule")) {
                        return FMT.info("Would you like to set another game-rule?").build();
                    } else {
                        return FMT.info("Would you like to set a custom game-rule?").build();
                    }
                })
                .router((src, context) -> {
                    String response = context.<String>getLast("add_gamerule").orElse("");
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

        ConversationNode gamerule = ConversationNode.builder("gamerule")
                .parameter(string(id("rule")), string(id("value")))
                .prompt((src, context) -> FMT.info("Please specify a gamerule and value").build())
                .router((src, context) -> {
                    String rule = context.<String>getLast("rule").orElse("");
                    String value = context.<String>getLast("value").orElse("");
                    if (!rule.isEmpty() && !value.isEmpty()) {
                        return ConversationRoute.goTo("add_gamerule");
                    }
                    FMT.error("Did not recognise rule '%s=%s'", rule, value).tell(src);
                    return ConversationRoute.goTo("add_gamerule");
                })
                .build();

        ConversationNode confirm = ConversationNode.builder("confirm")
                .parameter(string(id("confirm")))
                .prompt((src, context) -> {
                    GeneratorProperties.Builder builder = getPropertiesBuilder(context);
                    Formatter fmt = FMT.info("Summary").newLine();
                    fmt.info("World Name: ").stress(context.getLast("world_name").orElse("")).newLine();
                    fmt.info("Generator Name: ").stress(builder.getName()).newLine();
                    fmt.info("Plot Dimensions: ").stress(builder.getxWidth()).info("x").stress(builder.getzWidth()).newLine();
                    fmt.info("Wall Width: ").stress(builder.getWallWidth()).newLine();
                    fmt.info("Path Width: ").stress(builder.getPathWidth()).newLine();
                    fmt.newLine().info("Create a new world with these properties?");
                    return fmt.build();
                })
                .router((src, context) -> {
                    String response = context.<String>getLast("confirm").orElse("");
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

        return manager.specBuilder()
                .onComplete(conversation -> conversation.getSource().ifPresent(src -> {
                        String world = conversation.getContext().<String>getLast("world_name").orElse("");
                        FMT.info("Generating new world ").stress(world).tell(src);
                        FMT.info("Use the command ").stress("/wtp %s", world).info(" to teleport there").tell(src);
                        GeneratorProperties properties = getPropertiesBuilder(conversation.getContext()).build();
                        GenSave.saveGenerator(src, properties);
                        GenReload.reloadGenerators(src);
                        WorldCreate.createWorld(src, properties.name(), world);
                }))
                .timeOut(1, TimeUnit.MINUTES)
                .start(intro)
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
                        layerDepth,
                        biome,
                        defaultGameRules,
                        addGameRule,
                        gamerule,
                        confirm
                )
                .build();
    }

    private static ConversationRouter layer(String current, String next) {
        return (src, context) -> {
            String material = context.<String>getLast(current).orElse("");
            Optional<BlockType> type = Sponge.getRegistry().getType(BlockType.class, material);
            if (type.isPresent()) {
                context.putArg(current, type.get());
                return ConversationRoute.goTo(next);
            }
            FMT.error("'%s' is not recognised, please try again", material).tell(src);
            return ConversationRoute.goTo(current);
        };
    }

    private static GeneratorProperties.Builder getPropertiesBuilder(ConversationContext context) {
        Optional<GeneratorProperties.Builder> builder = context.getOne("property_builder");
        if (!builder.isPresent()) {
            GeneratorProperties.Builder b = GeneratorProperties.builder();
            context.putArg("property_builder", b);
            return b;
        }
        return builder.get();
    }

    private static Text id(String name) {
        return Text.of(name);
    }
}
