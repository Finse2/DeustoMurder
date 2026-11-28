
package utils.JDBC;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.net.URI;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.rowset.RowSetMetaDataImpl;

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


    public DBManager(DatabaseConfig config) throws Exception {

        Objects.requireNonNull(config, "config");

        this.sqlite = config.isSQLite();

        if (config.isManagedRqlite()) {

            JDBCDependencyHandler.startServer();

        } else if (config.isSQLite()) {

            Path parent =
                    config.getSQLiteFile().getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }
        }

        this.connection = DriverManager.getConnection(
                config.getJdbcUrl()
        );
    }

    public DBManager() throws Exception {
        this(DatabaseConfig.getInstance());
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

        ResultSetMetaData source = result.getMetaData();

        int columnCount = source.getColumnCount();

        RowSetMetaDataImpl metadata =
                new RowSetMetaDataImpl();

        metadata.setColumnCount(columnCount);

        // Copy and correct column metadata.

        for (int i = 1; i <= columnCount; i++) {

            int type = source.getColumnType(i);

            // SQLite integers can contain 64-bit values.

            if (type == Types.INTEGER) {
                type = Types.BIGINT;
            }

            metadata.setColumnName(
                    i, source.getColumnName(i)
            );

            metadata.setColumnLabel(
                    i, source.getColumnLabel(i)
            );

            metadata.setColumnType(i, type);

            metadata.setColumnTypeName(
                    i,
                    type == Types.BIGINT
                            ? "BIGINT"
                            : source.getColumnTypeName(i)
            );

            metadata.setNullable(
                    i, source.isNullable(i)
            );
        }

        rows.setMetaData(metadata);

        // Copy data using explicit JDBC getters.

        while (result.next()) {

            rows.moveToInsertRow();

            for (int i = 1; i <= columnCount; i++) {

                int type = metadata.getColumnType(i);

                Object value = switch (type) {

                    case Types.TINYINT,
                         Types.SMALLINT,
                         Types.INTEGER,
                         Types.BIGINT -> result.getLong(i);

                    case Types.FLOAT,
                         Types.REAL,
                         Types.DOUBLE -> result.getDouble(i);

                    case Types.NUMERIC,
                         Types.DECIMAL -> result.getBigDecimal(i);

                    case Types.BOOLEAN,
                         Types.BIT -> result.getBoolean(i);

                    case Types.CHAR,
                         Types.VARCHAR,
                         Types.LONGVARCHAR,
                         Types.NCHAR,
                         Types.NVARCHAR,
                         Types.LONGNVARCHAR -> result.getString(i);

                    case Types.BINARY,
                         Types.VARBINARY,
                         Types.LONGVARBINARY,
                         Types.BLOB -> result.getBytes(i);

                    case Types.NULL -> null;

                    default -> result.getObject(i);
                };

                if (value == null || result.wasNull()) {

                    rows.updateNull(i);

                } else {

                    rows.updateObject(i, value);
                }
            }

            rows.insertRow();
            rows.moveToCurrentRow();

            // Preserve the original query ordering.

            rows.last();
        }

        rows.beforeFirst();

        while (rows.next()) {
            rows.setOriginalRow();
        }

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
