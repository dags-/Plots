package me.dags.plots;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.Format;
import org.spongepowered.api.text.format.TextColors;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    private int blocks_per_tick = 10000;
    private boolean database_logger = true;
    private Format message_format = CommandBus.newFormatBuilder().build();

    public Config(){}

    public Config(boolean defaultConfig) {
        message_format = CommandBus.newFormatBuilder()
                .stress(TextColors.GOLD)
                .info(TextColors.GREEN)
                .build();
    }

    public void setBlocksPerTick(int count) {
        this.blocks_per_tick = count;
    }

    public void setDatabaseLogging(boolean enable) {
        this.database_logger = enable;
    }

    public void setMessageFormat(Format format) {
        this.message_format = format;
    }

    public int blocksPerTick() {
        return blocks_per_tick;
    }

    public boolean logDatabase() {
        return database_logger;
    }

    public Format getMessageFormat() {
        return message_format;
    }
}
