package me.dags.plots.util;

import me.dags.data.node.Node;
import me.dags.data.node.NodeObject;
import me.dags.data.node.NodeTypeAdapter;
import me.dags.plots.Config;

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
        configNode.put("owned_plot_wall", config.getOwnedPlotWall().getId());
        configNode.put("blocks_per_tick", config.blocksPerTick());
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
        config.setOwnedPlotWall(object.map("owned_plot_wall", Node::asString, config.getOwnedPlotWall().getId()));
        config.setBlocksPerTick(object.map("blocks_per_tick", n -> n.asNumber().intValue(), config.blocksPerTick()));
        return config;
    }
}
