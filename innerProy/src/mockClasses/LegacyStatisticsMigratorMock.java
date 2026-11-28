package mockClasses;

import game.statistics.LegacyStatisticsMigrator;
import game.statistics.StatisticsSnapshot;

import utils.JDBC.DatabaseService;
import utils.JDBC.DAO.PlayerDAO;
import utils.JDBC.DAO.StatisticsDAO;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.UUID;

public final class LegacyStatisticsMigratorMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== LEGACY STATISTICS MIGRATION TEST ==="
        );

        DatabaseService database =
                DatabaseService.getInstance();

        PlayerDAO playerDAO =
                new PlayerDAO(database);

        StatisticsDAO statisticsDAO =
                new StatisticsDAO(database);

        UUID playerId = UUID.randomUUID();

        Path directory =
                Files.createTempDirectory(
                        "legacy-statistics-test"
                );

        Path legacyFile =
                directory.resolve("statistics.properties");

        try {

            database.initialize();

            // TEST 1

            check(
                    database.getSchemaVersion() == 4,
                    "Schema version 4 required"
            );

            pass("Schema version 4");

            playerDAO.createPlayer(
                    playerId,
                    "Migration Test Player"
            );

            statisticsDAO.initializeStatistics(playerId);

            LegacyStatisticsMigrator migrator =
                    new LegacyStatisticsMigrator(
                            database,
                            legacyFile
                    );

            // TEST 2

            check(

                    migrator.migrate(playerId)

                            == LegacyStatisticsMigrator
                            .MigrationResult.NO_LEGACY_FILE,

                    "Missing file handling"
            );

            pass("Missing legacy file handled");

            // TEST 3

            Files.writeString(
                    legacyFile,
                    """
                    gamesPlayed=invalid
                    """
            );

            boolean rejected = false;

            try {

                migrator.migrate(playerId);

            } catch (IOException e) {

                rejected = true;
            }

            check(

                    rejected

                            && !statisticsDAO
                            .hasImportedLegacyStatistics(
                                    playerId
                            ),

                    "Invalid file accepted"
            );

            pass("Corrupted legacy file rejected");

            // CREATE VALID LEGACY DATA

            Files.writeString(
                    legacyFile,
                    """
                    playerName=Old Player
                    gamesPlayed=10
                    wins=6
                    losses=4
                    accusationsMade=8
                    correctAccusations=6
                    incorrectAccusations=2
                    totalGameDurationMillis=36000000
                    """
            );

            // TEST 4: EXISTING JDBC DATA

            statisticsDAO.recordWin(
                    playerId,
                    120000L,
                    100
            );

            check(

                    statisticsDAO
                            .getOrCreateStatistics(playerId)
                            .wins() == 1,

                    "Existing JDBC statistics missing"
            );

            pass("Existing JDBC statistics initialized");

            // TEST 5: IMPORT

            check(

                    migrator.migrate(playerId)

                            == LegacyStatisticsMigrator
                            .MigrationResult.IMPORTED,

                    "Legacy import failed"
            );

            pass("Legacy statistics imported");

            // TEST 6: VERIFY MERGED DATA

            StatisticsSnapshot stats =
                    statisticsDAO.getOrCreateStatistics(
                            playerId
                    );

            check(

                    stats.gamesPlayed() == 11

                            && stats.wins() == 7

                            && stats.losses() == 4

                            && stats.accusationsMade() == 8

                            && stats.correctAccusations() == 6

                            && stats.incorrectAccusations() == 2

                            && stats.totalGameDurationMillis()
                            == 36120000L

                            && stats.totalPoints() == 100

                            && stats.currentWinStreak() == 1

                            && stats.bestWinStreak() == 1,

                    "Imported statistics incorrect"
            );

            pass("Existing and legacy statistics merged");

            // TEST 7: IMPORT MARKER

            check(

                    statisticsDAO
                            .hasImportedLegacyStatistics(
                                    playerId
                            ),

                    "Import marker missing"
            );

            pass("Import marker persisted");

            // TEST 8: DUPLICATE IMPORT

            check(

                    migrator.migrate(playerId)

                            == LegacyStatisticsMigrator
                            .MigrationResult.ALREADY_IMPORTED

                            && stats.equals(

                            statisticsDAO
                                    .getOrCreateStatistics(
                                            playerId
                                    )
                    ),

                    "Duplicate import modified statistics"
            );

            pass("Duplicate import prevented");

            // TEST 9: SOURCE FILE

            check(

                    Files.isRegularFile(legacyFile),

                    "Original legacy file removed"
            );

            pass("Original legacy file preserved");

            // TEST 10: ACHIEVEMENTS

            var achievementDAO =
                    new utils.JDBC.DAO.AchievementDAO(
                            database
                    );

            check(

                    achievementDAO.getProgress(

                                    playerId,

                                    "WIN_GAMES"

                            ).orElseThrow()

                            .level().equals("BRONZE"),

                    "Achievement synchronization failed"
            );

            pass("Imported statistics unlock achievements");

            // TEST 11: RESTART

            database.shutdown();

            database.initialize();

            LegacyStatisticsMigrator restarted =
                    new LegacyStatisticsMigrator(

                            database,

                            legacyFile
                    );

            check(

                    stats.equals(

                            statisticsDAO
                                    .getOrCreateStatistics(
                                            playerId
                                    )
                    )

                            && restarted.migrate(playerId)

                            == LegacyStatisticsMigrator
                            .MigrationResult.ALREADY_IMPORTED,

                    "Restart duplicate protection failed"
            );

            pass("Import protection survives restart");

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            try {

                if (database.isInitialized()) {

                    database.execute(db -> {

                        db.executeUpdate("""
                                DELETE FROM player_achievements
                                WHERE player_id = ?
                                """,
                                playerId.toString()
                        );

                        db.executeUpdate("""
                                DELETE FROM player_statistics
                                WHERE player_id = ?
                                """,
                                playerId.toString()
                        );

                        return null;
                    });

                    if (playerDAO.playerExists(playerId)) {

                        playerDAO.deletePlayer(playerId);
                    }
                }

            } finally {

                database.shutdown();

                Files.deleteIfExists(legacyFile);

                Files.deleteIfExists(directory);
            }
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

        System.out.println(
                "[PASS] " + message
        );
    }
}