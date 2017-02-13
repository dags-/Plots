package me.dags.plots;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SlabTypes;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    private BlockState owned_plot_wall = BlockTypes.STONE_SLAB.getDefaultState().with(Keys.SLAB_TYPE, SlabTypes.QUARTZ).orElse(BlockTypes.STONE_SLAB.getDefaultState());
    private int blocks_per_tick = 5000;
    private Database database = new Database();

    public Config(){}

    public void setBlocksPerTick(int count) {
        this.blocks_per_tick = count;
    }

    public Database database() {
        return database;
    }

    public void setOwnedPlotWall(BlockState blockState) {
        this.owned_plot_wall = blockState;
    }

    public void setOwnedPlotWall(String input) {
        Sponge.getRegistry().getType(BlockState.class, input).ifPresent(this::setOwnedPlotWall);
    }

    public BlockState getOwnedPlotWall() {
        return this.owned_plot_wall;
    }

    public int blocksPerTick() {
        return blocks_per_tick;
    }

    public static class Database {

        private String address = "127.0.0.1";
        private int port = 27017;

        public String address() {
            return address;
        }

        public int port() {
            return port;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
