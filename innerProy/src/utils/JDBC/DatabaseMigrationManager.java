
package utils.JDBC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import utils.JDBC.migrations.MigrationV2;
import utils.JDBC.migrations.MigrationV3;
import utils.JDBC.migrations.MigrationV4;

public final class DatabaseMigrationManager {

    private static final Logger LOGGER =
            Logger.getLogger(
                    DatabaseMigrationManager.class.getName()
            );

    private static final int BASE_VERSION = 1;

    private final DBManager db;

    private final List<Migration> migrations;

    // MIGRATION INTERFACE

    public interface Migration {

        int version();

        String description();

        void apply(DBManager db) throws SQLException;

        void verify(DBManager db) throws SQLException;
    }

    // CONSTRUCTORS

    public DatabaseMigrationManager(DBManager db) {

        this(db, registeredMigrations());
    }

    public DatabaseMigrationManager(
            DBManager db,
            List<Migration> migrations
    ) {

        this.db = Objects.requireNonNull(db, "db");

        List<Migration> sorted =
                new ArrayList<>(
                        Objects.requireNonNull(migrations)
                );

        sorted.sort(
                Comparator.comparingInt(Migration::version)
        );

        this.migrations = List.copyOf(sorted);

        validateMigrations();
    }

    // REGISTERED MIGRATIONS

    private static List<Migration> registeredMigrations() {

        return List.of(
                new MigrationV2(),
                new MigrationV3(),
                new MigrationV4()
        );
    }

    private void validateMigrations() {

        int expectedVersion = BASE_VERSION + 1;

        for (Migration migration : migrations) {

            if (migration.version() != expectedVersion) {

                throw new IllegalArgumentException(
                        "Expected migration version "
                                + expectedVersion
                                + ", found "
                                + migration.version()
                );
            }

            expectedVersion++;
        }
    }

    // VERSION MANAGEMENT

    public int getLatestVersion() {

        if (migrations.isEmpty()) {
            return BASE_VERSION;
        }

        return migrations.getLast().version();
    }

    public int getCurrentVersion()
            throws SQLException {

        if (!db.tableExists("schema_version")) {
            return 0;
        }

        try (ResultSet rs = db.executeQuery("""
                SELECT version
                FROM schema_version
                WHERE id = 1
                """)) {

            return rs.next()
                    ? rs.getInt("version")
                    : 0;
        }
    }

    public boolean needsMigration()
            throws SQLException {

        return getCurrentVersion() < getLatestVersion();
    }

    // MIGRATION EXECUTION

    public synchronized void migrate()
            throws SQLException {

        int currentVersion = getCurrentVersion();

        int latestVersion = getLatestVersion();

        if (currentVersion == 0) {

            throw new SQLException(
                    "Database must be initialized "
                            + "before applying migrations."
            );
        }

        if (currentVersion > latestVersion) {

            throw new SQLException(
                    "Database version " + currentVersion
                            + " is newer than supported version "
                            + latestVersion
            );
        }

        if (currentVersion == latestVersion) {

            LOGGER.info(
                    "Database schema is up to date. Version: "
                            + currentVersion
            );

            return;
        }

        for (Migration migration : migrations) {

            if (migration.version() <= currentVersion) {
                continue;
            }

            applyMigration(
                    migration,
                    currentVersion
            );

            currentVersion = migration.version();
        }

        LOGGER.info(
                "All migrations completed. Schema version: "
                        + currentVersion
        );
    }

    private void applyMigration(
            Migration migration,
            int previousVersion
    ) throws SQLException {

        LOGGER.info(
                "Applying migration V"
                        + migration.version()
                        + ": "
                        + migration.description()
        );

        boolean transactional = db.isSQLite();

        if (transactional) {
            db.beginTransaction();
        }

        try {

            // Apply the schema changes.

            migration.apply(db);

            // Verify that the schema is correct.

            migration.verify(db);

            // Only update the version after verification.

            updateVersion(
                    previousVersion,
                    migration.version()
            );

            if (transactional) {
                db.commit();
            }

            LOGGER.info(
                    "Migration V"
                            + migration.version()
                            + " completed successfully."
            );

        } catch (SQLException | RuntimeException e) {

            if (transactional) {

                try {
                    db.rollback();

                } catch (SQLException rollbackError) {

                    e.addSuppressed(rollbackError);
                }
            }

            LOGGER.severe(
                    "Migration V"
                            + migration.version()
                            + " failed: "
                            + e.getMessage()
            );

            throw e;
        }
    }

    private void updateVersion(
            int previousVersion,
            int newVersion
    ) throws SQLException {

        db.executeUpdate("""
                UPDATE schema_version
                SET version = ?,
                    updated_at = ?
                WHERE id = 1
                  AND version = ?
                """,

                newVersion,
                System.currentTimeMillis(),
                previousVersion
        );

        if (getCurrentVersion() != newVersion) {

            throw new SQLException(
                    "Failed to update schema version to "
                            + newVersion
            );
        }
    }

    // MIGRATION UTILITIES

    public static boolean columnExists(
            DBManager db,
            String tableName,
            String columnName
    ) throws SQLException {

        Objects.requireNonNull(tableName);
        Objects.requireNonNull(columnName);

        if (!tableName.matches(
                "[a-zA-Z_][a-zA-Z0-9_]*"
        )) {

            throw new IllegalArgumentException(
                    "Invalid table name: " + tableName
            );
        }

        try (ResultSet rs = db.executeQuery(
                "PRAGMA table_info(\"" + tableName + "\")"
        )) {

            while (rs.next()) {

                if (columnName.equalsIgnoreCase(
                        rs.getString("name")
                )) {

                    return true;
                }
            }
        }

        return false;
    }
}
