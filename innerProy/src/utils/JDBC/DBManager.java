
package utils.JDBC;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.net.URI;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DBManager implements AutoCloseable {

    private final Connection connection;
    private final boolean sqlite;

    // CONSTRUCTORS

    public DBManager(String jdbcUrl) throws Exception {

        Objects.requireNonNull(jdbcUrl, "jdbcUrl");

        sqlite = jdbcUrl.startsWith("jdbc:sqlite:");

        if (jdbcUrl.startsWith("jdbc:rqlite:")) {

            if (isManagedLocalRqlite(jdbcUrl)) {
                JDBCDependencyHandler.startServer();
            }

        } else if (!sqlite) {
            throw new IllegalArgumentException(
                    "Unsupported JDBC URL: " + jdbcUrl
            );
        }

        connection = DriverManager.getConnection(jdbcUrl);
    }

    public DBManager(Connection connection) throws SQLException {

        this.connection = Objects.requireNonNull(
                connection, "connection"
        );

        String url = connection.getMetaData().getURL();

        sqlite = url != null && url.startsWith("jdbc:sqlite:");
    }

    // CONNECTION MANAGEMENT

    public boolean isConnected() {

        try {
            return !connection.isClosed()
                    && connection.isValid(2);

        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isSQLite() {
        return sqlite;
    }

    public Connection getConnection() {
        return connection;
    }

    private void ensureConnected() throws SQLException {

        if (connection.isClosed()) {
            throw new SQLException(
                    "Database connection is closed."
            );
        }
    }

    // SQL EXECUTION

    public boolean execute(String sql) throws SQLException {

        ensureConnected();

        try (Statement statement = connection.createStatement()) {
            return statement.execute(sql);
        }
    }

    public ResultSet executeQuery(String sql)
            throws SQLException {

        ensureConnected();

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            return detachedCopy(result);
        }
    }

    public int executeUpdate(String sql)
            throws SQLException {

        ensureConnected();

        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
    }

    // PARAMETERIZED SQL

    public boolean execute(
            String sql, Object... parameters
    ) throws SQLException {

        try (PreparedStatement statement =
                     prepareStatement(sql, parameters)) {

            return statement.execute();
        }
    }

    public ResultSet executeQuery(
            String sql, Object... parameters
    ) throws SQLException {

        try (PreparedStatement statement =
                     prepareStatement(sql, parameters);
             ResultSet result = statement.executeQuery()) {

            return detachedCopy(result);
        }
    }

    public int executeUpdate(
            String sql, Object... parameters
    ) throws SQLException {

        try (PreparedStatement statement =
                     prepareStatement(sql, parameters)) {

            return statement.executeUpdate();
        }
    }

    private PreparedStatement prepareStatement(
            String sql, Object... parameters
    ) throws SQLException {

        ensureConnected();

        PreparedStatement statement =
                connection.prepareStatement(sql);

        try {

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            return statement;

        } catch (SQLException | RuntimeException e) {

            try {
                statement.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }

            throw e;
        }
    }

    // TRANSACTIONS

    private void ensureTransactionSupport()
            throws SQLException {

        ensureConnected();

        if (!sqlite) {
            throw new SQLFeatureNotSupportedException(
                    "These transaction methods require SQLite."
            );
        }
    }

    public void beginTransaction() throws SQLException {

        ensureTransactionSupport();

        if (!connection.getAutoCommit()) {
            throw new SQLException(
                    "A transaction is already active."
            );
        }

        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {

        ensureTransactionSupport();

        if (connection.getAutoCommit()) {
            throw new SQLException(
                    "No active transaction."
            );
        }

        connection.commit();
        connection.setAutoCommit(true);
    }

    public void rollback() throws SQLException {

        ensureTransactionSupport();

        if (connection.getAutoCommit()) {
            throw new SQLException(
                    "No active transaction."
            );
        }

        connection.rollback();
        connection.setAutoCommit(true);
    }

    // DATABASE INSPECTION

    public boolean tableExists(String tableName)
            throws SQLException {

        String sql = """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                AND name = ?
                """;

        try (ResultSet result =
                     executeQuery(sql, tableName)) {

            return result.next();
        }
    }

    public Map<String, ResultSet> getAllTables()
            throws SQLException {

        Map<String, ResultSet> tables =
                new LinkedHashMap<>();

        String sql = """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                AND name NOT LIKE 'sqlite_%'
                """;

        try (ResultSet names = executeQuery(sql)) {

            while (names.next()) {

                String tableName = names.getString("name");

                String escapedName =
                        tableName.replace("\"", "\"\"");

                tables.put(
                        tableName,
                        executeQuery(
                                "SELECT * FROM \""
                                        + escapedName + "\""
                        )
                );
            }
        }

        return tables;
    }

    // DEBUGGING

    public ResultSet printQuery(String sql)
            throws SQLException {

        ensureConnected();

        try (Statement statement = connection.createStatement()) {

            if (!statement.execute(sql)) {

                System.out.println(
                        "Rows affected: "
                                + statement.getUpdateCount()
                );

                return null;
            }

            try (ResultSet result = statement.getResultSet()) {

                CachedRowSet rows = detachedCopy(result);

                ResultSetMetaData metadata =
                        rows.getMetaData();

                int columns = metadata.getColumnCount();

                boolean hasRows = false;

                while (rows.next()) {

                    hasRows = true;

                    for (int i = 1; i <= columns; i++) {

                        if (i > 1) {
                            System.out.print(" | ");
                        }

                        System.out.print(
                                metadata.getColumnLabel(i)
                                        + ": " + rows.getString(i)
                        );
                    }

                    System.out.println();
                }

                if (!hasRows) {
                    System.out.println("No matching records.");
                }

                rows.beforeFirst();

                return rows;
            }
        }
    }

    // INTERNAL UTILITIES

    private static CachedRowSet detachedCopy(
            ResultSet result
    ) throws SQLException {

        CachedRowSet rows = RowSetProvider
                .newFactory()
                .createCachedRowSet();

        rows.populate(result);
        rows.beforeFirst();

        return rows;
    }

    private static boolean isManagedLocalRqlite(
            String jdbcUrl
    ) {

        URI uri = URI.create(
                jdbcUrl.substring("jdbc:rqlite:".length())
        );

        String host = uri.getHost();

        return "http".equalsIgnoreCase(uri.getScheme())
                && uri.getPort() == JDBCDependencyHandler.getHttpPort()
                && host != null
                && (
                host.equalsIgnoreCase("localhost")
                        || host.equals("127.0.0.1")
                        || host.equals("[::1]")
                        || host.equals("::1")
        );
    }

    // RESOURCE MANAGEMENT

    @Override
    public void close() throws SQLException {

        if (connection.isClosed()) {
            return;
        }

        try {

            if (sqlite && !connection.getAutoCommit()) {
                connection.rollback();
            }

        } finally {
            connection.close();
        }
    }
}
