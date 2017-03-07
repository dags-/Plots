package me.dags.plots.command.gen;

import me.dags.commandbus.format.FMT;
import me.dags.converse.ConversationNode;
import me.dags.converse.ConversationRoute;
import me.dags.converse.ConversationRouter;
import me.dags.plots.generator.GeneratorProperties;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.api.command.args.GenericArguments.string;

/**
 * @author dags <dags@dags.me>
 */
public class Conversation {

    private void derp() {
        ConversationNode worldName = ConversationNode.builder("world_name")
                .parameter(string(id("world_name")))
                .prompt((src, context) -> FMT.info("What would you like to call the world?").build())
                .executor((src, context) -> ConversationRoute.goTo("plot_dims"))
                .build();

        ConversationNode plotDims = ConversationNode.builder("plot_dims")
                .parameter(integer(id("plot_width")), integer(id("plot_depth")))
                .prompt((src, context) -> FMT.info("What width and depth would you like plots to have?").build())
                .executor((src, context) -> ConversationRoute.goTo("path_width"))
                .build();

        ConversationNode pathWidth = ConversationNode.builder("path_width")
                .parameter(integer(id("path_width")))
                .prompt((src, context) -> FMT.info("How many blocks wide should paths be?").build())
                .executor((src, context) -> ConversationRoute.goTo("wall_width"))
                .build();

        ConversationNode wallWidth = ConversationNode.builder("wall_width")
                .parameter(integer(id("wall_width")))
                .prompt((src, context) -> FMT.info("How many blocks wide should boundary walls be?").build())
                .executor((src, context) -> ConversationRoute.goTo("wall_width"))
                .build();

        ConversationNode addLayer = ConversationNode.builder("add_layer")
                .parameter(string(id("response")))
                .prompt((src, context) -> {
                    if (context.hasAny("layer_depth")) {
                        return FMT.info("Would you like to add another layer?").build();
                    }
                    return FMT.info("Would you like to add a layer?").build();
                })
                .executor((src, context) -> {
                    Optional<String> response = context.getOne("response");
                    if (response.isPresent()) {
                        if (response.get().equalsIgnoreCase("yes")) {
                            return ConversationRoute.goTo("layer");
                        }
                        if (response.get().equalsIgnoreCase("no")) {
                            return ConversationRoute.goTo("");
                        }
                    }
                    FMT.error("Response '%s' was not recognised, please answer yes or no").tell(src);
                    return ConversationRoute.goTo("add_layers");
                })
                .build();

        ConversationNode plotMaterial = ConversationNode.builder("plot_material")
                .parameter(string(id("plot_material")))
                .prompt((src, context) -> FMT.info("What material should the plot be for this layer?").build())
                .executor(layer("plot_material", "wall_material"))
                .build();

        ConversationNode wallMaterial = ConversationNode.builder("wall_material")
                .parameter(string(id("wall_material")))
                .prompt((src, context) -> FMT.info("What material should the plot be for this layer?").build())
                .executor(layer("wall_material", "path_material"))
                .build();

        ConversationNode pathMaterial = ConversationNode.builder("path_material")
                .parameter(string(id("path_material")))
                .prompt((src, context) -> FMT.info("What material should the plot be for this layer?").build())
                .executor(layer("path_material", "layer_depth"))
                .build();

        ConversationNode layerDepth = ConversationNode.builder("layer_depth")
                .parameter(integer(id("layer_depth")))
                .prompt((src, context) -> FMT.info("How many blocks deep should this layer be?").build())
                .executor((src, context) -> {
                    int depth = context.<Integer>getOne("layer_depth").orElse(-1);
                    if (depth > 0 && depth < 256) {
                        addLayer(context);
                        return ConversationRoute.goTo("add_layer");
                    } else {
                        FMT.error("Please specify a value between 1 - 255").tell(src);
                        return ConversationRoute.goTo("layer_depth");
                    }
                })
                .build();

        ConversationNode biome = ConversationNode.builder("biome")
                .parameter(string(id("biome")))
                .prompt((src, context) -> FMT.info("What biome should be used in the world?").build())
                .executor((src, context) -> ConversationRoute.goTo("add_gamerule"))
                .build();

        ConversationNode addGameRule = ConversationNode.builder("add_gamerule")
                .parameter(string(id("add_gamerule")))
                .prompt((src, context) -> {
                    if (context.hasAny("rule")) {
                        return FMT.info("Would you like to add another gamerule?").build();
                    } else {
                        return FMT.info("Would you like to set a game rule?").build();
                    }
                })
                .executor((src, context) -> {
                    String response = context.<String>getOne("add_gamerule").orElse("");
                    if (response.equalsIgnoreCase("yes")) {
                        return ConversationRoute.goTo("gamerule");
                    }
                    if (response.equalsIgnoreCase("no")) {
                        return ConversationRoute.goTo("");
                    }
                    FMT.error("Response '%s' was not recognised, please answer yes or no", response).tell(src);
                    return ConversationRoute.goTo("add_gamerule");
                })
                .build();

        ConversationNode gamerule = ConversationNode.builder("gamerule")
                .parameter(string(id("rule")), string(id("value")))
                .prompt((src, context) -> FMT.info("Please specify a gamerule and value").build())
                .executor((src, context) -> {
                    String rule = context.<String>getOne("rule").orElse("");
                    String value = context.<String>getOne("value").orElse("");
                    if (!rule.isEmpty() && !value.isEmpty()) {
                        return ConversationRoute.goTo("add_gamerule");
                    }
                    FMT.error("Did not recognise rule '%s=%s'", rule, value).tell(src);
                    return ConversationRoute.goTo("add_gamerule");
                })
                .build();
    }

    private static ConversationRouter layer(String current, String next) {
        return (src, context) -> {
            String material = context.<String>getOne("material").orElse("");
            Optional<BlockType> type = Sponge.getRegistry().getType(BlockType.class, material);
            if (type.isPresent()) {
                context.putArg(current, type.get());
                return ConversationRoute.goTo(next);
            }
            FMT.error("'%s' is not recognised, please try again", material).tell(src);
            return ConversationRoute.goTo(current);
        };
    }

    private static void addLayer(CommandContext context) {
        GeneratorProperties.Builder builder = getPropertyBuilder(context);
        BlockType plot = context.<BlockType>getOne("plot_material").orElse(BlockTypes.BEDROCK);
        BlockType wall = context.<BlockType>getOne("wall_material").orElse(BlockTypes.BEDROCK);
        BlockType path = context.<BlockType>getOne("path_material").orElse(BlockTypes.BEDROCK);
        int depth = context.<Integer>getOne("layer_depth").orElse(1);
        builder.layer(plot, wall, path, depth);
    }

    private static GeneratorProperties.Builder getPropertyBuilder(CommandContext context) {
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
