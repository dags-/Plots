package me.dags.plots.database.table;

import java.sql.SQLException;

/**
 * @author dags <dags@dags.me>
 */
public class GetBuilder {

    private static final String QUERY = "SELECT %s FROM `%s`";
    private static final String WHERE_QUERY = "SELECT %s FROM `%s` WHERE %s = %s";

    private final Table table;
    private String select = "*";
    private String lookupKey = null;
    private Object lookupVal = null;
    private ResultConsumer resultConsumer = null;

    GetBuilder(Table table) {
        this.table = table;
    }

    public GetBuilder consumer(ResultConsumer consumer) {
        this.resultConsumer = consumer;
        return this;
    }

    public GetBuilder select(String in) {
        this.select = in;
        return this;
    }

    public GetBuilder lookupKey(String in) {
        this.lookupKey = in;
        return this;
    }

    public GetBuilder lookupVal(String in) {
        this.lookupVal = in;
        return this;
    }

    public void submit() throws SQLException {
        table.query(this, resultConsumer);
    }

    String build() {
        return lookupKey== null || lookupVal == null
                ? String.format(QUERY, select, table)
                : String.format(WHERE_QUERY, select, table.getName(), lookupKey, value());
    }

    private String value() {
        return lookupVal != null ? lookupVal instanceof String ? "'" + lookupVal + "'" : lookupVal.toString() : "";
    }
}
