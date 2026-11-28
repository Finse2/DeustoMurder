
package mockClasses;

import utils.JDBC.DBManager;
import utils.JDBC.DatabaseConfig;
import utils.JDBC.DatabaseInitializer;
import utils.JDBC.JDBCDependencyHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseInitializerMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== DATABASE INITIALIZER TEST ==="
        );

        String playerId =
                UUID.randomUUID().toString();

        try {

            try (DBManager db = new DBManager()) {

                DatabaseInitializer initializer =
                        new DatabaseInitializer(db);

                // TEST 1: INITIALIZATION

                initializer.initialize();

                initializer.verifySchema();

                System.out.println(
                        "[PASS] Database initialization"
                );

                // TEST 2: SCHEMA VERSION

                if (initializer.getSchemaVersion()
                        != DatabaseInitializer.SCHEMA_VERSION) {

                    throw new AssertionError(
                            "Incorrect schema version."
                    );
                }

                System.out.println(
                        "[PASS] Schema version"
                );

                // TEST 3: INSERT PLAYER

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
                        "JDBC Test Player",
                        now,
                        now
                );

                db.executeUpdate("""
                        INSERT INTO player_statistics (
                            player_id,
                            games_played,
                            wins,
                            losses
                        )
                        VALUES (?, ?, ?, ?)
                        """,
                        playerId,
                        10,
                        7,
                        3
                );

                System.out.println(
                        "[PASS] Player and statistics insertion"
                );

                // TEST 4: IDEMPOTENCE

                initializer.initialize();
                initializer.initialize();

                assertPlayerStatistics(db, playerId);

                System.out.println(
                        "[PASS] Repeated initialization"
                );

                // TEST 5: DATABASE CONSTRAINTS

                boolean rejected = false;

                try {

                    db.executeUpdate("""
                            UPDATE player_statistics
                            SET wins = -1
                            WHERE player_id = ?
                            """,
                            playerId
                    );

                } catch (SQLException e) {

                    rejected = true;
                }

                if (!rejected) {

                    throw new AssertionError(
                            "Negative wins were accepted."
                    );
                }

                assertPlayerStatistics(db, playerId);

                System.out.println(
                        "[PASS] Database constraints"
                );
            }

            // TEST 6: CONNECTION PERSISTENCE

            try (DBManager db = new DBManager()) {

                assertPlayerStatistics(db, playerId);

                System.out.println(
                        "[PASS] Persistence after reconnecting"
                );
            }

        } finally {

            // Remove only the test player's records.

            try (DBManager db = new DBManager()) {

                if (db.tableExists("player_statistics")) {

                    db.executeUpdate("""
                            DELETE FROM player_statistics
                            WHERE player_id = ?
                            """,
                            playerId
                    );
                }

                if (db.tableExists("players")) {

                    db.executeUpdate("""
                            DELETE FROM players
                            WHERE player_id = ?
                            """,
                            playerId
                    );
                }

            } finally {

                if (DatabaseConfig.getInstance()
                        .isManagedRqlite()) {

                    JDBCDependencyHandler.stopServer();
                }
            }
        }

        System.out.println(
                "All assertions passed."
        );
    }

    private static void assertPlayerStatistics(
            DBManager db,
            String playerId
    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT
                    games_played,
                    wins,
                    losses
                FROM player_statistics
                WHERE player_id = ?
                """,
                playerId
        )) {

            if (!rs.next()) {

                throw new AssertionError(
                        "Player statistics not found."
                );
            }

            if (rs.getInt("games_played") != 10
                    || rs.getInt("wins") != 7
                    || rs.getInt("losses") != 3) {

                throw new AssertionError(
                        "Player statistics were modified."
                );
            }

            if (rs.next()) {

                throw new AssertionError(
                        "Duplicate player statistics."
                );
            }
        }
    }
}
