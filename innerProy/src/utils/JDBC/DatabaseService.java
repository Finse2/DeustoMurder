
package utils.JDBC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public final class DatabaseService
        implements AutoCloseable {

    private static final Logger LOGGER =
            Logger.getLogger(
                    DatabaseService.class.getName()
            );

    private final DatabaseConfig config;

    private boolean initialized;

    // DATABASE OPERATIONS

    @FunctionalInterface
    public interface DatabaseOperation<T> {

        T execute(DBManager db) throws SQLException;
    }

    // SINGLETON

    private static class Holder {

        private static final DatabaseService INSTANCE =
                new DatabaseService();
    }

    public static DatabaseService getInstance() {
        return Holder.INSTANCE;
    }

    private DatabaseService() {

        this.config = DatabaseConfig.getInstance();
    }

    // INITIALIZATION

    public synchronized void initialize()
            throws Exception {

        if (initialized) {

            LOGGER.fine(
                    "Database service is already initialized."
            );

            return;
        }

        LOGGER.info("Starting database service...");

        try (DBManager db = createConnection()) {

            DatabaseInitializer initializer =
                    new DatabaseInitializer(db);

            DatabaseMigrationManager migrationManager =
                    new DatabaseMigrationManager(db);

            int currentVersion =
                    migrationManager.getCurrentVersion();

            // Initialize a new database.

            if (currentVersion == 0) {

                initializer.initialize();
            }

            // Upgrade existing databases if necessary.

            migrationManager.migrate();

            // Verify required tables.

            initializer.verifySchema();

            LOGGER.info(
                    "Database schema version: "
                            + migrationManager.getCurrentVersion()
            );
        }

        initialized = true;

        LOGGER.info(
                "Database service initialized successfully."
        );
    }

    // CONNECTION MANAGEMENT

    private DBManager createConnection()
            throws Exception {

        DBManager db = new DBManager(config);

        try {

            if (!db.isConnected()) {

                throw new SQLException(
                        "Database connection failed."
                );
            }

            // SQLite foreign keys must be enabled
            // separately for each connection.

            if (db.isSQLite()) {

                db.execute("PRAGMA foreign_keys = ON");

                try (ResultSet rs = db.executeQuery(
                        "PRAGMA foreign_keys"
                )) {

                    if (!rs.next() || rs.getInt(1) != 1) {

                        throw new SQLException(
                                "Could not enable foreign keys."
                        );
                    }
                }
            }

            return db;

        } catch (Exception e) {

            try {
                db.close();

            } catch (SQLException closeError) {

                e.addSuppressed(closeError);
            }

            throw e;
        }
    }

    private void ensureInitialized() {

        if (!initialized) {

            throw new IllegalStateException(
                    "Database service has not been initialized."
            );
        }
    }

    // EXECUTE DATABASE OPERATIONS

    public synchronized <T> T execute(
            DatabaseOperation<T> operation
    ) throws Exception {

        Objects.requireNonNull(
                operation,
                "operation"
        );

        ensureInitialized();

        try (DBManager db = createConnection()) {

            return operation.execute(db);
        }
    }

    // SQLITE TRANSACTIONS

    public synchronized <T> T executeTransaction(
            DatabaseOperation<T> operation
    ) throws Exception {

        Objects.requireNonNull(
                operation,
                "operation"
        );

        ensureInitialized();

        if (!config.isSQLite()) {

            throw new SQLException(
                    "JDBC transactions require SQLite. "
                            + "Use backend-specific atomic "
                            + "operations for rqlite."
            );
        }

        try (DBManager db = createConnection()) {

            db.beginTransaction();

            try {

                T result = operation.execute(db);

                db.commit();

                return result;

            } catch (SQLException | RuntimeException e) {

                try {
                    db.rollback();

                } catch (SQLException rollbackError) {

                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    // SERVICE STATUS

    public synchronized boolean isInitialized() {

        return initialized;
    }

    public synchronized boolean isHealthy() {

        if (!initialized) {
            return false;
        }

        try (DBManager db = createConnection();
             ResultSet rs = db.executeQuery(
                     "SELECT 1 AS result"
             )) {

            return rs.next()
                    && rs.getInt("result") == 1;

        } catch (Exception e) {

            LOGGER.warning(
                    "Database health check failed: "
                            + e.getMessage()
            );

            return false;
        }
    }

    public int getSchemaVersion()
            throws Exception {

        return execute(db -> {

            DatabaseMigrationManager manager =
                    new DatabaseMigrationManager(db);

            return manager.getCurrentVersion();
        });
    }

    public DatabaseConfig getConfig() {

        return config;
    }

    // SHUTDOWN

    public synchronized void shutdown() {

        LOGGER.info("Shutting down database service...");

        initialized = false;

        if (config.isManagedRqlite()) {

            JDBCDependencyHandler.stopServer();
        }

        LOGGER.info("Database service stopped.");
    }

    @Override
    public void close() {

        shutdown();
    }
}
