package me.dags.plots;

import me.dags.commandbus.utils.Format;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    private int blocks_per_tick = 500;
    private boolean database_logger = true;
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
