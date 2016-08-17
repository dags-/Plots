package me.dags.plots.database.statment;

import me.dags.plots.database.ResultTransformer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Select<T> implements Statement {

    private final String statement;
    private final ResultTransformer<T> transformer;

    private Select(Builder<T> builder) {
        this.statement = builder.toString();
        this.transformer = builder.transformer;
    }

    public String getStatement() {
        return statement;
    }

    public T transform(ResultSet result) throws SQLException {
        return transformer.accept(result);
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static class Builder<T> implements StatementBuilder {

        private final List<String> select = new ArrayList<>();
        private final List<String> from = new ArrayList<>();
        private ResultTransformer<T> transformer = resultSet -> null;
        private String where = "";

        public Builder<T> select(String select) {
            this.select.add(select);
            return this;
        }

        public Builder<T> from(String from) {
            this.from.add(from);
            return this;
        }

        public Builder<T> where(Where where) {
            this.where = where.getStatement();
            return this;
        }

        public Builder<T> transformer(ResultTransformer<T> transformer) {
            this.transformer = transformer;
            return this;
        }

        private String allToString(Collection<String> collection, boolean key) {
            StringBuilder builder = new StringBuilder();
            for (String s : collection) {
                builder.append(builder.length() > 0 ? ", " : "").append(key ? keyToString(s) : valToString(s));
            }
            return builder.toString();
        }

        public Select<T> build() {
            return new Select<T>(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT ");
            builder.append(allToString(select, true));
            builder.append(" FROM ");
            builder.append(allToString(from, true));
            if (!where.isEmpty()) {
                builder.append(" WHERE ").append(where);
            }
            return builder.toString();
        }
    }
}
