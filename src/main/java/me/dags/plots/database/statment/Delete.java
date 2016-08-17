package me.dags.plots.database.statment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Delete implements Statement {

    private final String statement;

    private Delete(Builder builder) {
        this.statement = builder.toString();
    }

    @Override
    public String getStatement() {
        return statement;
    }

    public static class Builder implements StatementBuilder {

        private final List<String> from = new ArrayList<>();
        private String where = "";

        public Builder table(String name) {
            this.from.add(name);
            return this;
        }

        public Builder where(Where where) {
            this.where = where.getStatement();
            return this;
        }

        private String allToString(Collection<String> collection) {
            StringBuilder builder = new StringBuilder();
            for (String s : collection) {
                builder.append(builder.length() > 0 ? ", " : "").append(keyToString(s));
            }
            return builder.toString();
        }

        @Override
        public String toString() {
            return "DELETE FROM " + allToString(from) + " WHERE " + where;
        }

        @Override
        public Delete build() {
            return new Delete(this);
        }
    }
}
