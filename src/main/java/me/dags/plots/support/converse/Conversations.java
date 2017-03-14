package me.dags.plots.support.converse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Conversations {

    private static final Conversations instance = new Conversations();

    private final Map<String, Conversation> conversations = new HashMap<>();

    public static Conversations getInstance() {
        return instance;
    }

    public Optional<Conversation> getConversation(String id) {
        return Optional.ofNullable(conversations.get(id));
    }

    void register(String id, Conversation conversation) {
        conversations.put(id, conversation);
    }
}
