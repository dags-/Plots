package me.dags.plots.generator;

import org.spongepowered.api.block.BlockTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Defaults {

    public static Map<String, String> defaultGameRules() {
        Map<String, String> map = new HashMap<>();
        map.put("doDaylightCycle", "false");
        map.put("doEntityDrops", "false");
        map.put("doFireTick", "false");
        map.put("doMobSpawning", "false");
        map.put("doTileDrops", "false");
        map.put("keepInventory", "true");
        map.put("mobGriefing", "false");
        map.put("naturalRegeneration", "false");
        return map;
    }

    public static List<LayerProperties> defaultLayers() {
        List<LayerProperties> list = new ArrayList<>();
        list.add(new LayerProperties(BlockTypes.BEDROCK, BlockTypes.BEDROCK, BlockTypes.BEDROCK, 1));
        list.add(new LayerProperties(BlockTypes.DIRT, BlockTypes.BEDROCK, BlockTypes.STONE, 6));
        list.add(new LayerProperties(BlockTypes.GRASS, BlockTypes.BEDROCK, BlockTypes.GRAVEL, 1));
        list.add(new LayerProperties(BlockTypes.AIR, BlockTypes.STONE_SLAB, BlockTypes.AIR, 1));
        return list;
    }
}
