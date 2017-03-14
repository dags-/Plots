package me.dags.plots.command.plot;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.plots.Permissions;
import me.dags.plots.support.converse.Conversation;
import me.dags.plots.support.converse.Conversations;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Setup {

    @Command(alias = "setup", parent = "plot")
    @Permission(Permissions.PLOT_SETUP)
    @Description("Start a 'conversation' to set up a new plot world from scratch")
    public void setup(@Caller CommandSource source) throws CommandException {
        Optional<Conversation> conversation = Conversations.getInstance().getConversation("setup");
        if (conversation.isPresent()) {
            conversation.get().startConversation(source);
        } else {
            FMT.error("Setup is not supported. Please ensure 'Converse' is installed").tell(source);
        }
    }
}
