package utils.JDBC;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * jdbc:sqlite:some/path/database.db -> file-backed SQLite (no server).<br>
 * jdbc:rqlite:http://localhost:4001 -> managed local rqlite server.<br>
 * jdbc:rqlite:http://host:4001 -> connect to an existing rqlite server.<br>
 */
public class DBManager implements AutoCloseable {

    public final Connection connection;

    public DBManager(String jdbcUrl) throws Exception {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");

        if (jdbcUrl.startsWith("jdbc:rqlite:")) {
            if (isManagedLocalRqlite(jdbcUrl)) {
                JDBCDependencyHandler.startServer();        //only gets executed if we are trying to run a host / localhost
            }
        } else if (!jdbcUrl.startsWith("jdbc:sqlite:")) {
            throw new IllegalArgumentException("Unsupported JDBC URL: " + jdbcUrl);
        }

        this.connection = DriverManager.getConnection(jdbcUrl); //only gets executed if we are trying to run a local .db
    }

    /** Wrap an existing JDBC connection without starting a server. */
    public DBManager(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    private static boolean isManagedLocalRqlite(String jdbcUrl) {
        try {
            URI uri = URI.create(jdbcUrl.substring("jdbc:rqlite:".length()));
            String host = uri.getHost();
            return "http".equalsIgnoreCase(uri.getScheme())
                    && uri.getPort() == 4001
                    && host != null
                    && (host.equalsIgnoreCase("localhost")
                    || host.equals("127.0.0.1")
                    || host.equals("[::1]")
                    || host.equals("::1"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed rqlite URL: " + jdbcUrl, e);
        }
    }

    /**
     * Executes any SQL statement.
     * @return true if it produced a ResultSet, false if it produced an update count.
     * Use executeQuery() to access rows or executeUpdate() for affected-row counts.
     */
    public boolean execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.execute(sql);
        }
    }

    /** Executes a SELECT and returns a disconnected ResultSet usable after the Statement closes. */
    public ResultSet executeQuery(String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return detachedCopy(result);
        }
    }

    /**
     * Executes INSERT, UPDATE, DELETE, CREATE TABLE, DROP TABLE, etc.
     * @return rows affected (typically 0 for DDL).
     */
    public int executeUpdate(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
    }

    /** Executes any SQL statement, prints results or update count, and returns rows when present. */
    public ResultSet printQuery(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if (!statement.execute(sql)) {
                System.out.println("Statement executed. Rows affected: " + statement.getUpdateCount());
                return null;
            }

            try (ResultSet result = statement.getResultSet()) {
                CachedRowSet rows = detachedCopy(result);
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

    /** Returns each non-system table name mapped to a disconnected ResultSet of its rows. */
    public Map<String, ResultSet> getAllTables() throws SQLException {
        Map<String, ResultSet> tablesByName = new LinkedHashMap<>();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'";

        try (ResultSet names = executeQuery(sql)) {
            while (names.next()) {
                String tableName = names.getString("name");
                String escapedName = tableName.replace("\"", "\"\"");
                tablesByName.put(tableName, executeQuery("SELECT * FROM \"" + escapedName + "\""));
            }
        }
        return tablesByName;
    }

    private static CachedRowSet detachedCopy(ResultSet result) throws SQLException {
        CachedRowSet rows = RowSetProvider.newFactory().createCachedRowSet();
        rows.populate(result);
        rows.beforeFirst();
        return rows;
    }

    @Override
    public void close() throws SQLException {
        if (!connection.isClosed()) connection.close();
    }
}
