package me.dags.plots.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author dags <dags@dags.me>
 */
public interface ResultTransformer<T> {

    T accept(ResultSet resultSet) throws SQLException;
}
