package me.dags.plots;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

/**
 * @author dags <dags@dags.me>
 */
public class TestServer {

    public static void start() {
        new Thread() {
            public void run() {
                MongoServer server = new MongoServer(new MemoryBackend());
                server.bind("127.0.0.1", 8080);
            }
        }.start();
    }
}
