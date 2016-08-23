package me.dags.plots.database.statment;

import me.dags.plots.database.ResultTransformer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
public class Select<T> implements Statement {

    private final String statement;
    private final ResultTransformer<T> transformer;
    private final Function<T, ? extends Statement> andUpdate;

    private Select(Builder<T> builder) {
        this.statement = builder.toString();
        this.transformer = builder.transformer;
        this.andUpdate = builder.andUpdate;
    }

    @Override
    public String getStatement() {
        return statement;
    }

    public T transform(ResultSet result) throws SQLException {
        return transformer.accept(result);
    }

    public Optional<Statement> andUpdate(T t) {
        if (andUpdate != null) {
            return Optional.ofNullable(andUpdate.apply(t));
        }
        return Optional.empty();
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static class Builder<T> implements StatementBuilder {

        private final List<String> select = new ArrayList<>();
        private final List<String> from = new ArrayList<>();
        private ResultTransformer<T> transformer = resultSet -> null;
        private Function<T, ? extends Statement> andUpdate = null;
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

        public Builder<T> andUpdate(Function<T, ? extends Statement> andThen) {
            this.andUpdate = andThen;
            return this;
        }

        private String allToString(Collection<String> collection, boolean key) {
            StringBuilder builder = new StringBuilder();
            for (String s : collection) {
                builder.append(builder.length() > 0 ? ", " : "");
                builder.append(s.endsWith("*") ? s : key ? keyToString(s) : valToString(s));
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
