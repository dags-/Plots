package me.dags.plots;

import me.dags.commandbus.utils.Format;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    private BlockState owned_plot_wall = BlockTypes.STONE_SLAB2.getDefaultState();
    private int blocks_per_tick = 5000;
    private Database database = new Database();
    private Format message_format = Format.DEFAULT;

    public Config(){}

    public Config(boolean defaultConfig) {
        message_format = Format.builder()
                .subdued(TextColors.GRAY, TextStyles.ITALIC)
                .stress(TextColors.GOLD)
                .info(TextColors.GREEN)
                .build();
    }

    public void setBlocksPerTick(int count) {
        this.blocks_per_tick = count;
    }

    public void setMessageFormat(Format format) {
        this.message_format = format;
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

    public Format formatter() {
        return message_format;
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
