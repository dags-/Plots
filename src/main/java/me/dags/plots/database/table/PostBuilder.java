package me.dags.plots.database.table;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class PostBuilder {

    private static final String FORMAT = "MERGE INTO `%s` (%s) VALUES(%s)";

    private final Map<String, String> parameters = new HashMap<>();
    private final Table table;

    PostBuilder(Table table) {
        this.table = table;
    }

    public PostBuilder set(String key, Object value) {
        if (table.hasKey(key)) {
            String val = value instanceof String ? "'" + value + "'" : value.toString();
            parameters.put(key, val);
        }
        return this;
    }

    public void submit() throws SQLException {
        table.insert(this);
    }

    String build() {
        StringBuilder keys = new StringBuilder();
        StringBuilder values = new StringBuilder();
        Iterator<String> iterator = table.getKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = parameters.get(key);
            if (value != null) {
                keys.append(keys.length() > 0 ? ", " : "").append(key);
                values.append(values.length() > 0 ? ", " : "").append(value);
            }
        }
        return String.format(FORMAT, table.getName(), keys, values);
    }
}
