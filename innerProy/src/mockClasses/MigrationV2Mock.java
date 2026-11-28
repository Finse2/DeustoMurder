
package mockClasses;

import utils.JDBC.DBManager;
import utils.JDBC.DatabaseInitializer;
import utils.JDBC.DatabaseMigrationManager;
import utils.JDBC.JDBCDependencyHandler;

import utils.JDBC.migrations.MigrationV2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MigrationV2Mock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== ACHIEVEMENT MIGRATION V2 TEST ==="
        );

        UUID playerId = UUID.randomUUID();

        String achievementCode =
                "TEST_ACHIEVEMENT_" + UUID.randomUUID();

        long now = System.currentTimeMillis();

        try (DBManager db = new DBManager()) {

            DatabaseMigrationManager manager =
                    new DatabaseMigrationManager(db);

            // Require a fresh isolated database.

            check(
                    manager.getCurrentVersion() == 0,
                    "Use a fresh isolated test database."
            );

            // TEST 1: CREATE BASELINE

            DatabaseInitializer initializer =
                    new DatabaseInitializer(db);

            initializer.initialize();

            check(
                    manager.getCurrentVersion() == 1
                            && manager.getLatestVersion() == 2,
                    "Baseline schema"
            );

            pass("Baseline schema version 1");

            // CREATE EXISTING PLAYER

            db.executeUpdate("""
                    INSERT INTO players (
                        player_id,
                        display_name,
                        created_at,
                        last_seen
                    )
                    VALUES (?, ?, ?, ?)
                    """,

                    playerId.toString(),
                    "Migration Test Player",
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

                    playerId.toString(),
                    20,
                    15,
                    5
            );

            // TEST 2: PROTECT EXISTING ACHIEVEMENTS

            db.executeUpdate("""
                    INSERT INTO achievements (
                        achievement_code,
                        name,
                        description
                    )
                    VALUES (?, ?, ?)
                    """,

                    achievementCode,
                    "Existing Achievement",
                    "Migration protection test"
            );

            boolean rejected = false;

            try {

                manager.migrate();

            } catch (SQLException e) {

                if (e.getMessage() == null
                        || !e.getMessage().contains(
                        "Migration requires empty"
                )) {

                    throw e;
                }

                rejected = true;
            }

            check(
                    rejected
                            && manager.getCurrentVersion() == 1,
                    "Existing achievement protection"
            );

            try (ResultSet rs = db.executeQuery("""
                    SELECT achievement_code
                    FROM achievements
                    WHERE achievement_code = ?
                    """, achievementCode)) {

                check(
                        rs.next(),
                        "Existing achievement was removed"
                );
            }

            db.executeUpdate("""
                    DELETE FROM achievements
                    WHERE achievement_code = ?
                    """,

                    achievementCode
            );

            pass("Existing achievement protection");

            // TEST 3: APPLY MIGRATION

            check(
                    manager.needsMigration(),
                    "Migration should be required"
            );

            manager.migrate();

            check(
                    manager.getCurrentVersion() == 2,
                    "Migration version"
            );

            pass("Migration from V1 to V2");

            // TEST 4: VERIFY SCHEMA

            new MigrationV2().verify(db);

            initializer.verifySchema();

            pass("New achievement schema verified");

            // TEST 5: PRESERVE STATISTICS

            try (ResultSet rs = db.executeQuery("""
                    SELECT
                        games_played,
                        wins,
                        losses

                    FROM player_statistics

                    WHERE player_id = ?
                    """,

                    playerId.toString()
            )) {

                check(
                        rs.next()
                                && rs.getInt("games_played") == 20
                                && rs.getInt("wins") == 15
                                && rs.getInt("losses") == 5,

                        "Existing statistics were modified"
                );
            }

            pass("Existing player statistics preserved");

            // TEST 6: NEW ACHIEVEMENT REQUIREMENTS

            db.executeUpdate("""
                    INSERT INTO achievements (

                        achievement_code,
                        name,
                        description,
                        hidden,
                        bronze_requirement,
                        gold_requirement,
                        diamond_requirement,
                        champ_requirement

                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,

                    achievementCode,
                    "Test Veteran",
                    "Win a number of matches",
                    0,
                    10,
                    25,
                    50,
                    100
            );

            try (ResultSet rs = db.executeQuery("""
                    SELECT *
                    FROM achievements
                    WHERE achievement_code = ?
                    """,

                    achievementCode
            )) {

                check(
                        rs.next()
                                && rs.getInt("bronze_requirement") == 10
                                && rs.getInt("gold_requirement") == 25
                                && rs.getInt("diamond_requirement") == 50
                                && rs.getInt("champ_requirement") == 100,

                        "Achievement requirements"
                );
            }

            pass("New achievement requirements");

            // CREATE PLAYER ACHIEVEMENT

            db.executeUpdate("""
                    INSERT INTO player_achievements (

                        player_id,
                        achievement_code,
                        progress,
                        level,
                        updated_at

                    )
                    VALUES (?, ?, ?, ?, ?)
                    """,

                    playerId.toString(),
                    achievementCode,
                    0,
                    "LOCKED",
                    now
            );

            // TEST 7: ALL VALID LEVELS

            String[] levels = {

                    "LOCKED",
                    "BRONZE",
                    "GOLD",
                    "DIAMOND",
                    "CHAMP"
            };

            for (String level : levels) {

                db.executeUpdate("""
                        UPDATE player_achievements

                        SET level = ?

                        WHERE player_id = ?
                          AND achievement_code = ?
                        """,

                        level,
                        playerId.toString(),
                        achievementCode
                );

                check(
                        getLevel(
                                db,
                                playerId,
                                achievementCode
                        ).equals(level),

                        "Level rejected: " + level
                );

                pass("Valid level: " + level);
            }

            // TEST 8: INVALID LEVELS

            String[] invalidLevels = {

                    "SILVER",
                    "PLATINUM",
                    "UNKNOWN"
            };

            for (String level : invalidLevels) {

                rejected = false;

                try {

                    db.executeUpdate("""
                            UPDATE player_achievements

                            SET level = ?

                            WHERE player_id = ?
                              AND achievement_code = ?
                            """,

                            level,
                            playerId.toString(),
                            achievementCode
                    );

                } catch (SQLException e) {

                    rejected = true;
                }

                check(
                        rejected,
                        "Invalid level accepted: " + level
                );

                check(
                        getLevel(
                                db,
                                playerId,
                                achievementCode
                        ).equals("CHAMP"),

                        "Invalid update changed player level"
                );

                pass("Invalid level rejected: " + level);
            }

            // TEST 9: REPEATED MIGRATION

            manager.migrate();
            manager.migrate();

            check(
                    manager.getCurrentVersion() == 2
                            && !manager.needsMigration(),

                    "Repeated migration"
            );

            check(
                    getLevel(
                            db,
                            playerId,
                            achievementCode
                    ).equals("CHAMP"),

                    "Repeated migration removed progression"
            );

            pass("Repeated migration preserves progress");

            // CLEANUP TEST RECORDS

            db.executeUpdate("""
                    DELETE FROM player_achievements
                    WHERE player_id = ?
                    """,

                    playerId.toString()
            );

            db.executeUpdate("""
                    DELETE FROM achievements
                    WHERE achievement_code = ?
                    """,

                    achievementCode
            );

            db.executeUpdate("""
                    DELETE FROM player_statistics
                    WHERE player_id = ?
                    """,

                    playerId.toString()
            );

            db.executeUpdate("""
                    DELETE FROM players
                    WHERE player_id = ?
                    """,

                    playerId.toString()
            );

            pass("Test record cleanup");

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            JDBCDependencyHandler.stopServer();
        }
    }

    // TEST UTILITIES

    private static String getLevel(
            DBManager db,
            UUID playerId,
            String achievementCode
    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT level

                FROM player_achievements

                WHERE player_id = ?
                  AND achievement_code = ?
                """,

                playerId.toString(),
                achievementCode
        )) {

            if (!rs.next()) {

                throw new AssertionError(
                        "Player achievement not found."
                );
            }

            return rs.getString("level");
        }
    }

    private static void check(
            boolean condition,
            String message
    ) {

        if (!condition) {

            throw new AssertionError(message);
        }
    }

    private static void pass(String message) {

        System.out.println("[PASS] " + message);
    }
}
