
package mockClasses;

import utils.JDBC.DBManager;
import utils.JDBC.DatabaseInitializer;
import utils.JDBC.DatabaseMigrationManager;
import utils.JDBC.JDBCDependencyHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DatabaseMigrationManagerMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== DATABASE MIGRATION TEST ==="
        );

        try (DBManager db = new DBManager()) {

            DatabaseMigrationManager baseline =
                    new DatabaseMigrationManager(db);

            if (baseline.getCurrentVersion() != 0) {

                throw new IllegalStateException(
                        "Use a fresh isolated test database."
                );
            }

            DatabaseInitializer initializer =
                    new DatabaseInitializer(db);

            initializer.initialize();

            DatabaseMigrationManager.Migration testMigration =
                    new DatabaseMigrationManager.Migration() {

                        @Override
                        public int version() {
                            return 2;
                        }

                        @Override
                        public String description() {
                            return "Create migration test table";
                        }

                        @Override
                        public void apply(DBManager db)
                                throws SQLException {

                            db.execute("""
                            CREATE TABLE IF NOT EXISTS
                            migration_test (
                                id INTEGER PRIMARY KEY,
                                message TEXT NOT NULL
                            )
                            """);
                        }

                        @Override
                        public void verify(DBManager db)
                                throws SQLException {

                            if (!db.tableExists("migration_test")) {

                                throw new SQLException(
                                        "Migration table missing."
                                );
                            }
                        }
                    };

            DatabaseMigrationManager manager =
                    new DatabaseMigrationManager(
                            db,
                            List.of(testMigration)
                    );

            // TEST 1: VERSION DETECTION

            if (manager.getCurrentVersion() != 1
                    || manager.getLatestVersion() != 2
                    || !manager.needsMigration()) {

                throw new AssertionError(
                        "Incorrect version detection."
                );
            }

            System.out.println(
                    "[PASS] Version detection"
            );

            // CREATE EXISTING PLAYER

            String playerId =
                    UUID.randomUUID().toString();

            long now = System.currentTimeMillis();

            db.executeUpdate("""
                    INSERT INTO players (
                        player_id,
                        display_name,
                        created_at,
                        last_seen
                    )
                    VALUES (?, ?, ?, ?)
                    """,

                    playerId,
                    "Migration Test Player",
                    now,
                    now
            );

            // TEST 2: MIGRATION

            manager.migrate();

            if (manager.getCurrentVersion() != 2) {

                throw new AssertionError(
                        "Migration version incorrect."
                );
            }

            if (!db.tableExists("migration_test")) {

                throw new AssertionError(
                        "Migration table not created."
                );
            }

            System.out.println(
                    "[PASS] Migration execution"
            );

            // TEST 3: DATA PRESERVATION

            try (ResultSet rs = db.executeQuery("""
                    SELECT display_name
                    FROM players
                    WHERE player_id = ?
                    """, playerId)) {

                if (!rs.next()
                        || !"Migration Test Player".equals(
                        rs.getString("display_name")
                )) {

                    throw new AssertionError(
                            "Player data was lost."
                    );
                }
            }

            System.out.println(
                    "[PASS] Existing data preserved"
            );

            // TEST 4: IDEMPOTENCE

            manager.migrate();
            manager.migrate();

            if (manager.needsMigration()
                    || manager.getCurrentVersion() != 2) {

                throw new AssertionError(
                        "Repeated migration failed."
                );
            }

            System.out.println(
                    "[PASS] Repeated migration"
            );

            // TEST 5: DOWNGRADE PROTECTION

            DatabaseMigrationManager oldManager =
                    new DatabaseMigrationManager(db);

            boolean rejected = false;

            try {

                oldManager.migrate();

            } catch (SQLException e) {

                rejected = true;
            }

            if (!rejected) {

                throw new AssertionError(
                        "Newer schema was incorrectly accepted."
                );
            }

            System.out.println(
                    "[PASS] Downgrade protection"
            );

            // TEST 6: FAILED MIGRATION

            DatabaseMigrationManager.Migration failedMigration =
                    new DatabaseMigrationManager.Migration() {

                        @Override
                        public int version() {
                            return 3;
                        }

                        @Override
                        public String description() {
                            return "Intentional failure";
                        }

                        @Override
                        public void apply(DBManager db) {

                            // Simulate an incomplete migration.
                        }

                        @Override
                        public void verify(DBManager db)
                                throws SQLException {

                            throw new SQLException(
                                    "Intentional verification failure."
                            );
                        }
                    };

            DatabaseMigrationManager failureManager =
                    new DatabaseMigrationManager(
                            db,
                            List.of(
                                    testMigration,
                                    failedMigration
                            )
                    );

            boolean failed = false;

            try {

                failureManager.migrate();

            } catch (SQLException e) {

                failed = true;
            }

            if (!failed
                    || failureManager.getCurrentVersion() != 2) {

                throw new AssertionError(
                        "Failed migration changed schema version."
                );
            }

            System.out.println(
                    "[PASS] Failed migration protection"
            );

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            JDBCDependencyHandler.stopServer();
        }
    }
}
