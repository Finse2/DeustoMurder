
package mockClasses;

import game.statistics.StatisticsSnapshot;

import utils.JDBC.DatabaseService;

import utils.JDBC.dao.PlayerDAO;
import utils.JDBC.dao.StatisticsDAO;

import java.sql.SQLException;
import java.util.UUID;

public class StatisticsDAOMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== STATISTICS DAO TEST ==="
        );

        DatabaseService service =
                DatabaseService.getInstance();

        PlayerDAO playerDAO =
                new PlayerDAO(service);

        StatisticsDAO dao =
                new StatisticsDAO(service);

        UUID playerId = UUID.randomUUID();

        UUID unknownPlayer = UUID.randomUUID();

        try {

            service.initialize();

            playerDAO.createPlayer(
                    playerId,
                    "Statistics Test Player"
            );

            // TEST 1: INITIALIZATION

            dao.initializeStatistics(playerId);

            StatisticsSnapshot stats =
                    getStatistics(dao, playerId);

            check(
                    stats.gamesPlayed() == 0
                            && stats.wins() == 0
                            && stats.losses() == 0
                            && stats.totalPoints() == 0,

                    "Initial statistics"
            );

            pass("Statistics initialization");

            // TEST 2: EMPTY STATISTICS

            check(
                    stats.getWinRate() == 0.0
                            && stats.getAccusationAccuracy() == 0.0
                            && stats.getAverageGameDurationMillis() == 0,

                    "Empty statistics calculations"
            );

            pass("Empty statistics calculations");

            // TEST 3: RECORD WIN

            dao.recordWin(
                    playerId,
                    1_800_000L,
                    100
            );

            stats = getStatistics(dao, playerId);

            check(
                    stats.gamesPlayed() == 1
                            && stats.wins() == 1
                            && stats.losses() == 0
                            && stats.currentWinStreak() == 1
                            && stats.bestWinStreak() == 1,

                    "Record win"
            );

            pass("Record win");

            // TEST 4: CONSECUTIVE WINS

            for (int i = 0; i < 4; i++) {

                dao.recordWin(
                        playerId,
                        1_800_000L,
                        100
                );
            }

            stats = getStatistics(dao, playerId);

            check(
                    stats.wins() == 5
                            && stats.currentWinStreak() == 5
                            && stats.bestWinStreak() == 5,

                    "Winning streak"
            );

            pass("Five consecutive wins");

            // TEST 5: RECORD LOSS

            dao.recordLoss(
                    playerId,
                    1_800_000L,
                    10
            );

            stats = getStatistics(dao, playerId);

            check(
                    stats.gamesPlayed() == 6
                            && stats.wins() == 5
                            && stats.losses() == 1
                            && stats.currentWinStreak() == 0
                            && stats.bestWinStreak() == 5,

                    "Record loss"
            );

            pass("Record loss and reset streak");

            // TEST 6: WIN AFTER LOSS

            dao.recordWin(
                    playerId,
                    1_800_000L,
                    100
            );

            stats = getStatistics(dao, playerId);

            check(
                    stats.currentWinStreak() == 1
                            && stats.bestWinStreak() == 5,

                    "Restart winning streak"
            );

            pass("Restart winning streak");

            // TEST 7: DRAW

            dao.recordDraw(
                    playerId,
                    1_800_000L,
                    0
            );

            stats = getStatistics(dao, playerId);

            check(
                    stats.gamesPlayed() == 8
                            && stats.wins() == 6
                            && stats.losses() == 1
                            && stats.currentWinStreak() == 0
                            && stats.bestWinStreak() == 5,

                    "Record draw"
            );

            pass("Record draw");

            // TEST 8: ACCUSATIONS

            dao.recordAccusation(playerId, true);
            dao.recordAccusation(playerId, true);
            dao.recordAccusation(playerId, false);

            stats = getStatistics(dao, playerId);

            check(
                    stats.accusationsMade() == 3
                            && stats.correctAccusations() == 2
                            && stats.incorrectAccusations() == 1,

                    "Accusation recording"
            );

            pass("Accusation recording");

            // TEST 9: WIN RATE

            check(
                    Math.abs(
                            stats.getWinRate() - 75.0
                    ) < 0.00001,

                    "Win rate"
            );

            pass("Win rate calculation");

            // TEST 10: ACCUSATION ACCURACY

            check(
                    Math.abs(
                            stats.getAccusationAccuracy()
                                    - (200.0 / 3)
                    ) < 0.00001,

                    "Accusation accuracy"
            );

            pass("Accusation accuracy");

            // TEST 11: GAME DURATION

            check(
                    stats.totalGameDurationMillis()
                            == 14_400_000L

                            && stats.getAverageGameDurationMillis()
                            == 1_800_000L

                            && stats.getFormattedTotalGameDuration()
                            .equals("04:00:00")

                            && stats.getFormattedAverageGameDuration()
                            .equals("00:30:00"),

                    "Game duration"
            );

            pass("Game duration calculations");

            // TEST 12: POINTS

            check(
                    stats.totalPoints() == 610,

                    "Total points"
            );

            pass("Points calculation");

            // TEST 13: REPEATED INITIALIZATION

            StatisticsSnapshot before =
                    getStatistics(dao, playerId);

            dao.initializeStatistics(playerId);
            dao.initializeStatistics(playerId);

            StatisticsSnapshot after =
                    dao.getOrCreateStatistics(playerId);

            check(
                    before.equals(after),

                    "Repeated initialization"
            );

            pass("Repeated initialization");

            // TEST 14: GET ALL STATISTICS

            check(
                    dao.getAllStatistics()
                            .stream()
                            .anyMatch(
                                    s -> s.playerId()
                                            .equals(playerId)
                            ),

                    "Get all statistics"
            );

            pass("Get all statistics");

            // TEST 15: LEADERBOARD

            check(
                    dao.getLeaderboard(Integer.MAX_VALUE)
                            .stream()
                            .anyMatch(
                                    entry ->
                                            entry.statistics()
                                                    .playerId()
                                                    .equals(playerId)
                            ),

                    "Leaderboard"
            );

            pass("Leaderboard retrieval");

            // TEST 16: NEGATIVE VALUES

            boolean rejected = false;

            try {

                dao.recordWin(
                        playerId,
                        -1000,
                        100
                );

            } catch (IllegalArgumentException e) {

                rejected = true;
            }

            check(
                    rejected,
                    "Negative duration validation"
            );

            rejected = false;

            try {

                dao.recordWin(
                        playerId,
                        1000,
                        -100
                );

            } catch (IllegalArgumentException e) {

                rejected = true;
            }

            check(
                    rejected,
                    "Negative points validation"
            );

            check(
                    before.equals(
                            getStatistics(dao, playerId)
                    ),

                    "Invalid operations modified statistics"
            );

            pass("Invalid values rejected");

            // TEST 17: UNKNOWN PLAYER

            rejected = false;

            try {

                dao.recordWin(
                        unknownPlayer,
                        1000,
                        100
                );

            } catch (SQLException e) {

                rejected = true;
            }

            check(
                    rejected
                            && dao.getStatistics(unknownPlayer)
                            .isEmpty(),

                    "Unknown player validation"
            );

            pass("Unknown player rejected");

            // TEST 18: 64-BIT DURATION

            long largeDuration = 3_000_000_000L;

            dao.recordWin(
                    playerId,
                    largeDuration,
                    0
            );

            stats = getStatistics(dao, playerId);

            check(
                    stats.totalGameDurationMillis()
                            == 3_014_400_000L,

                    "64-bit duration support"
            );

            pass("64-bit duration support");

            // TEST 19: SERVER RESTART PERSISTENCE

            StatisticsSnapshot saved =
                    getStatistics(dao, playerId);

            service.shutdown();

            service.initialize();

            StatisticsSnapshot restored =
                    getStatistics(dao, playerId);

            check(
                    saved.equals(restored),

                    "Persistence after service restart"
            );

            pass("Persistence after service restart");

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            try {

                if (service.isInitialized()) {

                    service.execute(db -> {

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

                service.shutdown();
            }
        }
    }

    private static StatisticsSnapshot getStatistics(
            StatisticsDAO dao,
            UUID playerId
    ) throws Exception {

        return dao.getStatistics(playerId)
                .orElseThrow(() ->
                        new AssertionError(
                                "Statistics not found."
                        )
                );
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
