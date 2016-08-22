package me.dags.plots.util;

import me.dags.data.node.Node;
import me.dags.data.node.NodeArray;
import me.dags.data.node.NodeObject;
import me.dags.data.node.NodeTypeAdapter;
import me.dags.plots.generator.GeneratorProperties;
import me.dags.plots.generator.LayerProperties;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
public class GeneratorPropertiesAdapter implements NodeTypeAdapter<GeneratorProperties> {

    @Override
    public Node toNode(GeneratorProperties properties) {
        NodeObject node = new NodeObject();
        node.put("name", properties.name());
        node.put("plot_x_width", properties.getXWidth());
        node.put("plot_z_width", properties.getZWidth());
        node.put("wall_width", properties.getWallWidth());
        node.put("path_width", properties.getPathWidth());
        node.put("biome", properties.biomeType().getId());

        NodeArray layersNode = new NodeArray();
        for (LayerProperties layer : properties.layerProperties()) {
            NodeObject layerNode = new NodeObject();
            layerNode.put("body", layer.body().getId());
            layerNode.put("wall", layer.wall().getId());
            layerNode.put("path", layer.path().getId());
            layerNode.put("thickness", layer.thickness());
            layersNode.add(layerNode);
        }
        node.put("layers", layersNode);

        NodeObject gameRules = new NodeObject();
        for (Map.Entry<String, String> rule : properties.gameRules().entrySet()) {
            gameRules.put(rule.getKey(), rule.getValue());
        }
        node.put("game_rules", gameRules);

        return node;
    }

    @Override
    public GeneratorProperties fromNode(Node node) {
        NodeObject object = node.asNodeObject();

        GeneratorProperties.Builder builder = GeneratorProperties.builder();
        builder.name(get(object.get("name"), Node::asString, "error"));
        builder.xWidth(get(object.get("plot_x_width"), n -> n.asNumber().intValue(), 42));
        builder.zWidth(get(object.get("plot_z_width"), n -> n.asNumber().intValue(), 42));
        builder.wallWidth(get(object.get("wall_width"), n -> n.asNumber().intValue(), 1));
        builder.pathWidth(get(object.get("path_width"), n -> n.asNumber().intValue(), 6));
        builder.biome(get(object.get("biome"), n -> Sponge.getRegistry().getType(BiomeType.class, n.asString()).orElse(BiomeTypes.PLAINS), BiomeTypes.PLAINS));

        Node layers = object.get("layers");
        if (layers.isPresent() && layers.isNodeArray()) {
            for (Node layer : layers.asNodeArray().values()) {
                if (!layer.isNodeObject()) {
                    continue;
                }
                BlockType body = blockType(layer.asNodeObject(), "body");
                BlockType wall = blockType(layer.asNodeObject(), "wall");
                BlockType path = blockType(layer.asNodeObject(), "path");
                int thickness = get(layer.asNodeObject().get("thickness"), n -> n.asNumber().intValue(), 1);
                builder.layer(body, wall, path, thickness);
            }
        }

        Node rules = object.get("game_rules");
        if (rules.isPresent() && rules.isNodeObject()) {
            for (Map.Entry<Node, Node> rule : rules.asNodeObject().entries()) {
                Object value = rule.getValue().asObject();
                builder.gameRule(rule.getKey().asString(), value != null ? value.toString() : "false");
            }
        }

        return builder.build();
    }

    private static <T> T get(Node node, Function<Node, T> transformer, T fallback) {
        if (node.isPresent()) {
            return transformer.apply(node);
        }
        return fallback;
    }

    private static BlockType blockType(NodeObject node, String key) {
        Node n = node.get(key);
        if (n.isPresent()) {
            return Sponge.getRegistry().getType(BlockType.class, n.asString()).orElse(BlockTypes.AIR);
        }
        return BlockTypes.AIR;
    }
}
