package me.dags.plots.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * @author dags <dags@dags.me>
 */
public class WorldDatabase {

    private final MongoDatabase database;

    public WorldDatabase(MongoDatabase database) {
        this.database = database;
    }

    public MongoCollection<Document> userCollection() {
        return database.getCollection("users");
    }

    public MongoCollection<Document> plotCollection() {
        return database.getCollection("plots");
    }
}
