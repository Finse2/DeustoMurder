
package mockClasses;

import achievements.AchievementManager;

import game.statistics.PlayerStatisticsService;

import utils.JDBC.DatabaseService;

import utils.JDBC.DAO.AchievementDAO;
import utils.JDBC.DAO.PlayerDAO;

import java.util.UUID;

public class AchievementStatisticsMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== ACHIEVEMENT STATISTICS INTEGRATION TEST ==="
        );

        DatabaseService database =
                DatabaseService.getInstance();

        PlayerDAO playerDAO =
                new PlayerDAO(database);

        AchievementDAO achievementDAO =
                new AchievementDAO(database);

        PlayerStatisticsService service =
                new PlayerStatisticsService(database);

        UUID playerId = UUID.randomUUID();

        try {

            database.initialize();

            service.initialize();

            AchievementManager manager =
                    service.getAchievementManager();

            // CREATE TEST PLAYER

            playerDAO.createPlayer(

                    playerId,

                    "Achievement Statistics Test Player"
            );

            pass("Player initialization");

            // TEST 1: INITIAL SYNCHRONIZATION

            manager.synchronizeStatistics(playerId);

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "WIN_GAMES"
                    ).equals("LOCKED"),

                    "Initial achievement state"
            );

            pass("Initial achievements locked");

            // TEST 2: FIRST WIN

            var unlocks = service.recordWin(

                    playerId,

                    1_800_000L,

                    100
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "WIN_GAMES"
                    ).equals("BRONZE"),

                    "First victory achievement"
            );

            check(

                    unlocks.stream().anyMatch(

                            event ->
                                    event.achievementCode()
                                            .equals("WIN_GAMES")

                                            && event.newLevel()
                                            .equals("BRONZE")
                    ),

                    "Missing achievement notification"
            );

            pass("First victory unlocks BRONZE");

            // TEST 3: WINNING STREAK

            service.recordWin(
                    playerId, 1000, 100
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "CONSECUTIVE_WINS"
                    ).equals("BRONZE"),

                    "Two consecutive victories"
            );

            service.recordWin(
                    playerId, 1000, 100
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "CONSECUTIVE_WINS"
                    ).equals("GOLD"),

                    "Three consecutive victories"
            );

            pass("Winning streak progression");

            // TEST 4: HIDDEN ACHIEVEMENT

            service.recordWin(
                    playerId, 1000, 100
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "LUCK_OF_THE_GODS"
                    ).equals("LOCKED"),

                    "Hidden achievement unlocked early"
            );

            service.recordWin(
                    playerId, 1000, 100
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "LUCK_OF_THE_GODS"
                    ).equals("CHAMP"),

                    "Five consecutive victories"
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "CONSECUTIVE_WINS"
                    ).equals("DIAMOND"),

                    "Diamond winning streak"
            );

            pass("Luck of the Gods unlocked");

            // TEST 5: LOSS

            service.recordLoss(

                    playerId,

                    1000,

                    10
            );

            var statistics =
                    service.getStatistics(playerId);

            check(

                    statistics.currentWinStreak() == 0

                            && statistics.bestWinStreak() == 5,

                    "Winning streak reset"
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "LUCK_OF_THE_GODS"
                    ).equals("CHAMP"),

                    "Earned achievement removed"
            );

            pass("Loss preserves earned achievements");

            // TEST 6: INCORRECT ACCUSATION

            service.recordAccusation(

                    playerId,

                    false
            );

            check(

                    getLevel(

                            achievementDAO,

                            playerId,

                            "MAKE_INCORRECT_FINAL_ACCUSATIONS"

                    ).equals("BRONZE"),

                    "Incorrect accusation achievement"
            );

            pass("Accusation achievement unlocked");

            // TEST 7: TOTAL ACCUSATIONS

            for (int i = 0; i < 4; i++) {

                service.recordAccusation(

                        playerId,

                        true
                );
            }

            check(

                    getLevel(

                            achievementDAO,

                            playerId,

                            "TOTAL_FINAL_ACCUSATIONS"

                    ).equals("BRONZE"),

                    "Total accusation achievement"
            );

            pass("Total accusation achievement unlocked");

            // TEST 8: TEN VICTORIES

            for (int i = 0; i < 5; i++) {

                service.recordWin(

                        playerId,

                        1000,

                        100
                );
            }

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "WIN_GAMES"
                    ).equals("GOLD"),

                    "Ten victories"
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "TOTAL_COMPLETED_MATCHES"
                    ).equals("GOLD"),

                    "Total completed games"
            );

            pass("Gold achievement progression");

            // TEST 9: SYNCHRONIZATION IDEMPOTENCE

            var previous =
                    achievementDAO.getProgress(

                            playerId,

                            "WIN_GAMES"

                    ).orElseThrow();

            var repeatedUnlocks =
                    manager.synchronizeStatistics(
                            playerId
                    );

            var updated =
                    achievementDAO.getProgress(

                            playerId,

                            "WIN_GAMES"

                    ).orElseThrow();

            check(

                    previous.progress()
                            == updated.progress()

                            && previous.level()
                            .equals(updated.level())

                            && repeatedUnlocks.isEmpty(),

                    "Repeated synchronization"
            );

            pass("Repeated synchronization is safe");

            // TEST 10: SERVER RESTART

            database.shutdown();

            database.initialize();

            service.initialize();

            check(

                    service.getStatistics(playerId)
                            .wins() == 10,

                    "Statistics persistence"
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "WIN_GAMES"
                    ).equals("GOLD"),

                    "Achievement persistence"
            );

            check(

                    getLevel(
                            achievementDAO,
                            playerId,
                            "LUCK_OF_THE_GODS"
                    ).equals("CHAMP"),

                    "Hidden achievement persistence"
            );

            pass("Statistics and achievements survive restart");

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
            }
        }
    }

    private static String getLevel(

            AchievementDAO dao,

            UUID playerId,

            String achievementCode

    ) throws Exception {

        return dao.getProgress(

                playerId,

                achievementCode

        ).orElseThrow().level();
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
