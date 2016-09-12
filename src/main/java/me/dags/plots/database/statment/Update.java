package me.dags.plots.database.statment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Update implements Statement {

    private final String statement;

    private Update(Builder builder) {
        this.statement = builder.toString();
    }

    @Override
    public String getStatement() {
        return statement;
    }

    public static class Builder implements StatementBuilder {

        private String table = "";
        private String where = "";
        private final Map<String, Object> values = new HashMap<>();

        public Builder in(String table) {
            this.table = table;
            return this;
        }

        public Builder set(String key, Object value) {
            this.values.put(key, value);
            return this;
        }

        public Builder where(Where where) {
            this.where = where.getStatement();
            return this;
        }

        private String values() {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                builder.append(builder.length() > 0 ? ", " : "").append(entry.getKey()).append("=").append(valToString(entry.getValue()));
            }
            return builder.toString();
        }

        @Override
        public String toString() {
            return "UPDATE " + table + " SET " + values() + " WHERE " + where;
        }

        @Override
        public Update build() {
            return new Update(this);
        }
    }
}
