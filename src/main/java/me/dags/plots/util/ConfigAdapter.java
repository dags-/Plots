package me.dags.plots.util;

import me.dags.commandbus.utils.Format;
import me.dags.data.node.Node;
import me.dags.data.node.NodeObject;
import me.dags.data.node.NodeTypeAdapter;
import me.dags.data.node.NodeTypeAdapters;
import me.dags.plots.Config;
import org.spongepowered.api.block.BlockTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class ConfigAdapter implements NodeTypeAdapter<Config> {

    @Override
    public Node toNode(Config config) {
        NodeObject database = new NodeObject();
        database.put("address", config.database().address());
        database.put("port", config.database().port());

        NodeObject configNode = new NodeObject();
        configNode.put("database", database);
        configNode.put("highlight_block", config.highlightBlock().getId());
        configNode.put("proof_block", config.proofBlock().getId());
        configNode.put("convert_old_db", config.convert());
        configNode.put("blocks_per_tick", config.blocksPerTick());
        configNode.put("message_format", NodeTypeAdapters.serialize(config.formatter().toMap()));
        return configNode;
    }

    @Override
    public Config fromNode(Node node) {
        NodeObject object = node.asNodeObject();
        Config config = new Config();
        object.ifPresent("database", db -> {
            if (db.isNodeObject()) {
                config.database().setAddress(db.asNodeObject().map("address", Node::asString, "127.0.0.1"));
                config.database().setPort(db.asNodeObject().map("port", n -> n.asNumber().intValue(), 27017));
            }
        });
        config.setHighlight(object.map("highlight_block", Node::asString, BlockTypes.GLOWSTONE.getId()));
        config.setProof(object.map("proof_block", Node::asString, BlockTypes.DIAMOND_BLOCK.getId()));
        config.setConvert(!object.contains("convert_old_db") || object.map("convert_old_db", Node::asBoolean, false));
        config.setBlocksPerTick(object.map("blocks_per_tick", n -> n.asNumber().intValue(), 10000));
        config.setMessageFormat(object.map("message_format", n -> Format.fromMap(ConfigAdapter.toMap(n)), Format.DEFAULT));
        return config;
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> toMap(Node node) {
        return (Map<Object, Object>) toObject(node);
    }

    private static Object toObject(Node node) {
        if (node.isNodeObject()) {
            Map<Object, Object> map = new HashMap<>();
            for (Map.Entry<Node, Node> entry : node.asNodeObject().entries()) {
                map.put(toObject(entry.getKey()), toObject(entry.getValue()));
            }
            return map;
        } else if (node.isNodeArray()) {
            List<Object> list = new ArrayList<>();
            for (Node child : node.asNodeArray().values()) {
                list.add(toObject(child));
            }
            return list;
        } else {
            return node.asObject();
        }
    }
}
