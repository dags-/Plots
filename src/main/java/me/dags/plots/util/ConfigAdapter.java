package me.dags.plots.util;

import me.dags.commandbus.CommandBus;
import me.dags.data.node.Node;
import me.dags.data.node.NodeObject;
import me.dags.data.node.NodeTypeAdapter;
import me.dags.data.node.NodeTypeAdapters;
import me.dags.plots.Config;

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
        NodeObject configNode = new NodeObject();
        configNode.put("blocks_per_tick", config.blocksPerTick());
        configNode.put("database_logger", config.logDatabase());
        configNode.put("message_format", NodeTypeAdapters.serialize(config.getMessageFormat().toMap()));
        return configNode;
    }

    @Override
    public Config fromNode(Node node) {
        NodeObject object = node.asNodeObject();
        Config config = new Config();
        config.setBlocksPerTick(object.map("blocks_per_tick", n -> n.asNumber().intValue(), 10000));
        config.setDatabaseLogging(object.map("database_logger", Node::asBoolean, true));
        config.setMessageFormat(object.map("message_format", n -> CommandBus.getFormatter(ConfigAdapter.toMap(n)), CommandBus.newFormatBuilder().build()));
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
