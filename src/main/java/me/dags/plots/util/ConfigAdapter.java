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
        configNode.put("message_format", NodeTypeAdapters.serialize(config.getMessageFormat().toMap()));
        return configNode;
    }

    @Override
    public Config fromNode(Node node) {
        Config config = new Config();
        node.asNodeObject().ifPresent("message_format", child -> {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) toObject(node);
            config.setMessageFormat(CommandBus.getFormatter(map));
        });
        return config;
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
