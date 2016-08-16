package me.dags.plots.database.table;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author dags <dags@dags.me>
 */
public interface ResultConsumer {

    void accept(ResultSet resultSet) throws SQLException;
}
