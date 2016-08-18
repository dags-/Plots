package me.dags.plots;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.Format;
import org.spongepowered.api.text.format.TextColors;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    private Format message_format = CommandBus.newFormatBuilder().build();

    public Config(){}

    public Config(boolean defaultConfig) {
        message_format = CommandBus.newFormatBuilder()
                .stress(TextColors.GOLD)
                .info(TextColors.GREEN)
                .build();
    }

    public void setMessageFormat(Format format) {
        this.message_format = format;
    }

    public Format getMessageFormat() {
        return message_format;
    }

}