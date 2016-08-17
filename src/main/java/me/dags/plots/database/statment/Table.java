package me.dags.plots.database.statment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Table implements Statement {

    private final String statement;

    private Table(Builder builder) {
        this.statement = builder.toString();
    }

    @Override
    public String getStatement() {
        return statement;
    }

    public static class Builder implements StatementBuilder {

        private String name = "";
        private String primary = "";
        private final List<Column> columns = new ArrayList<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder primary(String key) {
            primary = key;
            return this;
        }

        public Builder column(String key, String type) {
            columns.add(new Column(key, type));
            return this;
        }

        private String allToString(Collection<Column> collection) {
            StringBuilder builder = new StringBuilder();
            for (Column c : collection) {
                builder.append(builder.length() > 0 ? ", " : "").append(keyToString(c));
            }
            return builder.toString();
        }

        @Override
        public String toString() {
            return "CREATE TABLE IF NOT EXISTS " +
                    keyToString(name) +
                    " (" + allToString(columns) + ", PRIMARY KEY(" + primary + ")" + ")";
        }

        @Override
        public Table build() {
            return new Table(this);
        }

        private class Column {

            private final String key;
            private final String type;

            private Column(String key, String type) {
                this.key = key;
                this.type = type;
            }

            @Override
            public String toString() {
                return Builder.this.keyToString(key) + " " + type;
            }
        }
    }
}
