
package utils.JDBC;

import java.sql.*;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

public class DBManager implements AutoCloseable {

    public Connection connection;

    public DBManager(String DB_URL) throws Exception {
        JDBCDependencyHandler.startServer() ;
        connection = DriverManager.getConnection(DB_URL);
    }
    public DBManager(Connection connection) throws Exception {
        JDBCDependencyHandler.startServer() ;
        this.connection = connection;

    }

    /**executes and returns a SQL query, displaying is done throught System.out.println()<br>
     * if no "," is present at the end of the provided sql string, it will be added at the beginning of the function<br>
     * @return null if there are no rows that where selected, ResultSet if there was a returnable result*/
    public ResultSet printQuery(String sql) throws SQLException {

        if(sql.endsWith(";")) {
            sql += ";";
        }

        try (Statement statement = connection.createStatement()) {
            if (!statement.execute(sql)) {
                System.out.println("Statement executed. Rows affected: " + statement.getUpdateCount());
                return null;
            }

            try (ResultSet result = statement.getResultSet()) {
                CachedRowSet rows = RowSetProvider.newFactory().createCachedRowSet();
                rows.populate(result);
                ResultSetMetaData metadata = rows.getMetaData();
                int columns = metadata.getColumnCount();
                boolean hasRows = false;

                while (rows.next()) {
                    hasRows = true;
                    for (int i = 1; i <= columns; i++) {
                        if (i > 1) System.out.print(" | ");
                        System.out.print(metadata.getColumnLabel(i) + ": " + rows.getString(i));
                    }
                    System.out.println();
                }

                if (!hasRows) System.out.println("No matching records.");
                rows.beforeFirst();
                return rows;
            }
        }

    }
    /**executes and returns a SQL query<br>
     * if no "," is present at the end of the provided sql string, it will be added at the beginning of the function<br>
     * @return null if there are no rows that where selected, ResultSet if there was a returnable result*/
    public ResultSet executeQuery(String sql) throws SQLException {

        try (Statement statement = connection.createStatement()) {
            if (!statement.execute(sql)) {
                return null;
            }

            try (ResultSet result = statement.getResultSet()) {
                CachedRowSet rows = RowSetProvider.newFactory().createCachedRowSet();
                rows.populate(result);
                rows.beforeFirst();
                return rows;
            }
        }

    }

    /**to close the Connection at the end, it won't do anything if the Connection is already closed */
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
