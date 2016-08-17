package me.dags.plots.database.statment;

/**
 * @author dags <dags@dags.me>
 */
public interface StatementBuilder {

    String KEY_ESCAPE = "`";
    String VAL_ESCAPE = "'";

    Statement build();

    default String keyToString(Object value) {
        return value == null ? "NULL" : value instanceof String ? KEY_ESCAPE + value + KEY_ESCAPE : value.toString();
    }

    default String valToString(Object value) {
        return value == null ? "NULL" : value instanceof String ? VAL_ESCAPE + value + VAL_ESCAPE : value.toString();
    }
}
