package me.dags.plots.database.statment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Where implements Statement {

    private final String statement;

    private Where(Builder builder) {
        this.statement = builder.toString();
    }

    @Override
    public String getStatement() {
        return statement;
    }

    public static Where.Builder of(String key, String operator, Object compareTo, boolean ignoreCase) {
        return new Where.Builder(key, operator, compareTo, ignoreCase);
    }

    public static Where.Builder of(String key, String operator, Object compareTo) {
        return new Where.Builder(key, operator, compareTo, false);
    }

    public static class Builder implements StatementBuilder {

        private final List<String> comparisons = new ArrayList<>();
        private final List<String> statements = new ArrayList<>();
        private final boolean ignoreCase;

        private Builder(String key, String operator, Object compareTo, boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            comparisons.add(statement(key, operator, compareTo));
        }

        public Builder and(String key, String operator, Object compareTo) {
            comparisons.add("AND " + statement(key, operator, compareTo));
            return this;
        }

        public Builder or(String key, String operator, Object compareTo) {
            comparisons.add("OR " + statement(key, operator, compareTo));
            return this;
        }

        public Builder and(Where.Builder whereBuilder) {
            statements.add("AND (" + whereBuilder.build() + ")");
            return this;
        }

        public Builder or(Where.Builder whereBuilder) {
            statements.add("OR (" + whereBuilder.build() + ")");
            return this;
        }

        private String allToString(Collection<String> collection) {
            StringBuilder builder = new StringBuilder();
            for (String string : collection) {
                builder.append(builder.length() > 0 ? " " : "").append(string);
            }
            return builder.toString();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(allToString(comparisons));
            if (statements.size() > 0) {
                builder.insert(0, "(").append(") ");
                builder.append(allToString(statements));
            }
            return builder.toString();
        }

        @Override
        public Where build() {
            return new Where(this);
        }

        private String statement(String key, String operator, Object compareTo) {
            String k = keyToString(key);
            String v = valToString(compareTo);
            return (ignoreCase ? "UPPER(" + k + ")" : k) + " " + operator + " " + (ignoreCase ? "UPPER(" + v + ")" : v);
        }
    }
}
