package me.dags.plots.database.statment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Insert implements Statement {

    private final String statement;

    private Insert(Builder builder) {
        this.statement = builder.toString();
    }

    @Override
    public String getStatement() {
        return statement;
    }

    public static class Builder implements StatementBuilder {

        private String table = "";
        private boolean merge = false;
        private final Map<String, Object> values = new HashMap<>();

        public Builder table(String table) {
            this.table = table;
            return this;
        }

        public Builder update(String key, Object value) {
            this.merge = !key.isEmpty() && value != null;
            return set(key, value);
        }

        public Builder set(String key, Object value) {
            this.values.put(key, value);
            return this;
        }

        private String allToString(Collection<?> collection, boolean key) {
            StringBuilder builder = new StringBuilder();
            for (Object o : collection) {
                builder.append(builder.length() > 0 ? ", " : "").append(key ? keyToString(o) : valToString(o));
            }
            return builder.toString();
        }

        @Override
        public String toString() {
            return (merge ? "MERGE INTO " : "INSERT INTO ")
                    + keyToString(table)
                    + " (" + allToString(values.keySet(), true) + ")"
                    + " VALUES(" + allToString(values.values(), false) + ")";
        }

        @Override
        public Insert build() {
            return new Insert(this);
        }
    }
}
