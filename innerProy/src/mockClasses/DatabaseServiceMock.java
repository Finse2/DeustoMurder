
package mockClasses;

import utils.JDBC.DatabaseService;
import utils.JDBC.DatabaseConfig;
import utils.JDBC.JDBCDependencyHandler;

import java.sql.ResultSet;
import java.util.UUID;

public class DatabaseServiceMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== DATABASE SERVICE TEST ==="
        );

        DatabaseService service =
                DatabaseService.getInstance();

        String playerId =
                UUID.randomUUID().toString();

        boolean managedServer = false;

        try {

            // TEST 1: SINGLETON

            if (service != DatabaseService.getInstance()) {

                throw new AssertionError(
                        "Singleton verification failed."
                );
            }

            System.out.println(
                    "[PASS] Singleton"
            );

            // TEST 2: INITIALIZATION

            service.initialize();

            if (!service.isInitialized()) {

                throw new AssertionError(
                        "Service initialization failed."
                );
            }

            managedServer =
                    JDBCDependencyHandler.isManagedServer();

            System.out.println(
                    "[PASS] Service initialization"
            );

            // TEST 3: HEALTH CHECK

            if (!service.isHealthy()) {

                throw new AssertionError(
                        "Database health check failed."
                );
            }

            System.out.println(
                    "[PASS] Database health check"
            );

            // TEST 4: SCHEMA VERSION

            int version = service.getSchemaVersion();

            if (version != 2) {

                throw new AssertionError(
                        "Unexpected schema version: "
                                + version
                );
            }

            System.out.println(
                    "[PASS] Schema version: " + version
            );

            // TEST 5: WRITE OPERATION

            long now = System.currentTimeMillis();

            service.execute(db -> {

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
                        "Service Test Player",
                        now,
                        now
                );

                return null;
            });

            System.out.println(
                    "[PASS] Write operation"
            );

            // TEST 6: READ OPERATION

            String playerName =
                    service.execute(db -> {

                        try (ResultSet rs = db.executeQuery("""
                        SELECT display_name
                        FROM players
                        WHERE player_id = ?
                        """, playerId)) {

                            return rs.next()
                                    ? rs.getString("display_name")
                                    : null;
                        }
                    });

            if (!"Service Test Player".equals(playerName)) {

                throw new AssertionError(
                        "Incorrect player name."
                );
            }

            System.out.println(
                    "[PASS] Read operation"
            );

            // TEST 7: REPEATED INITIALIZATION

            service.initialize();
            service.initialize();

            if (!service.isHealthy()) {

                throw new AssertionError(
                        "Repeated initialization failed."
                );
            }

            System.out.println(
                    "[PASS] Repeated initialization"
            );

            // TEST 8: CLEANUP

            service.execute(db -> {

                db.executeUpdate("""
                        DELETE FROM players
                        WHERE player_id = ?
                        """, playerId);

                return null;
            });

            System.out.println(
                    "[PASS] Record cleanup"
            );

        } finally {

            service.shutdown();
        }

        // TEST 9: SHUTDOWN

        if (service.isInitialized()) {

            throw new AssertionError(
                    "Service is still initialized."
            );
        }

        if (service.isHealthy()) {

            throw new AssertionError(
                    "Stopped service reported healthy."
            );
        }

        System.out.println(
                "[PASS] Service shutdown"
        );

        // TEST 10: SERVER OWNERSHIP

        if (DatabaseConfig.getInstance().isManagedRqlite()) {

            if (managedServer
                    && JDBCDependencyHandler.isRunning()) {

                throw new AssertionError(
                        "Managed server is still running."
                );
            }
        }

        System.out.println(
                "[PASS] Managed server shutdown check"
        );

        // TEST 11: OPERATIONS AFTER SHUTDOWN

        boolean rejected = false;

        try {

            service.execute(db -> null);

        } catch (IllegalStateException e) {

            rejected = true;
        }

        if (!rejected) {

            throw new AssertionError(
                    "Stopped service accepted an operation."
            );
        }

        System.out.println(
                "[PASS] Operations after shutdown rejected"
        );

        System.out.println(
                "All assertions passed."
        );
    }
}
