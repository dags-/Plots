package me.dags.plots.database.table;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Table {

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `%s` (%s)";

    private final String name;
    private final String databaseConnector;
    private final Map<String, String> columns;
    private final SqlService service;

    private Table(Builder builder) {
        this.databaseConnector = builder.database;
        this.name = builder.name;
        this.columns = Collections.unmodifiableMap(builder.map);
        this.service = Sponge.getServiceManager().provideUnchecked(SqlService.class);
    }

    public Table submit() throws SQLException {
        Sponge.getServiceManager().provideUnchecked(SqlService.class);
        try (Connection connection = service.getDataSource(databaseConnector).getConnection()) {
            connection.createStatement().execute(createTableString());
        }
        return this;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseConnector);
    }

    public void insert(PostBuilder post) throws SQLException {
        try (Connection connection = service.getDataSource(databaseConnector).getConnection()) {
            connection.createStatement().executeUpdate(post.build());
        }
    }

    public <T> void query(GetBuilder get, ResultConsumer consumer) throws SQLException {
        try (Connection connection = service.getDataSource(databaseConnector).getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(get.build());
            consumer.accept(resultSet);
        }
    }

    public GetBuilder get() {
        return new GetBuilder(this);
    }

    public PostBuilder post() {
        return new PostBuilder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    Iterator<String> getKeys() {
        return columns.keySet().iterator();
    }

    boolean hasKey(String key) {
        return columns.containsKey(key);
    }

    private String createTableString() {
        StringBuilder create = new StringBuilder();
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            create.append(create.length() > 0 ? ", " : "").append(entry.getKey()).append(" ").append(entry.getValue());
        }
        return String.format(CREATE_TABLE, name, create);
    }

    public static class Builder {

        private String database;
        private String name;
        private LinkedHashMap<String, String> map = new LinkedHashMap<>();

        public Builder column(String key, String type) {
            map.put(key, type);
            return this;
        }

        public Builder database(String connector) {
            this.database = connector;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Table build() {
            return new Table(this);
        }
    }
}
