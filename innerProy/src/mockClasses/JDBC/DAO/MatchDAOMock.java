
package mockClasses;

import game.statistics.MatchSnapshot;
import game.statistics.MatchPlayerSnapshot;
import game.statistics.PlayerStatisticsService;
import game.statistics.StatisticsSnapshot;

import utils.JDBC.DatabaseService;

import utils.JDBC.DAO.MatchDAO;
import utils.JDBC.DAO.PlayerDAO;
import utils.JDBC.DAO.StatisticsDAO;
import utils.JDBC.DAO.AchievementDAO;

import java.sql.SQLException;
import java.util.UUID;

public class MatchDAOMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== MATCH DAO TEST ==="
        );

        DatabaseService database =
                DatabaseService.getInstance();

        PlayerDAO playerDAO =
                new PlayerDAO(database);

        MatchDAO matchDAO =
                new MatchDAO(database);

        StatisticsDAO statisticsDAO =
                new StatisticsDAO(database);

        AchievementDAO achievementDAO =
                new AchievementDAO(database);

        PlayerStatisticsService service =
                new PlayerStatisticsService(database);

        UUID player1 = UUID.randomUUID();

        UUID player2 = UUID.randomUUID();

        UUID player3 = UUID.randomUUID();

        UUID matchId = UUID.randomUUID();

        UUID cancelledMatch = UUID.randomUUID();

        UUID secondMatch = UUID.randomUUID();

        try {

            database.initialize();

            service.initialize();

            // TEST 1: SCHEMA

            check(

                    database.getSchemaVersion() == 3,

                    "Schema version 3 required"
            );

            pass("Schema version 3");

            // TEST 2: CREATE PLAYERS

            playerDAO.createPlayer(
                    player1, "Match Test Player 1"
            );

            playerDAO.createPlayer(
                    player2, "Match Test Player 2"
            );

            playerDAO.createPlayer(
                    player3, "Match Test Player 3"
            );

            pass("Player initialization");

            // TEST 3: CREATE MATCH

            matchDAO.createMatch(
                    matchId, player1
            );

            MatchSnapshot match =
                    matchDAO.getMatch(matchId)
                            .orElseThrow();

            check(

                    match.isInProgress()

                            && match.hostId().equals(player1)

                            && match.winnerId() == null

                            && match.finishedAt() == null,

                    "Match initialization"
            );

            pass("Match creation");

            // TEST 4: DUPLICATE MATCH

            matchDAO.createMatch(
                    matchId, player1
            );

            check(

                    matchDAO.getMatch(matchId)
                            .orElseThrow()
                            .equals(match),

                    "Duplicate match changed its data"
            );

            pass("Duplicate match creation protection");

            // TEST 5: REGISTER PARTICIPANTS

            matchDAO.addPlayer(
                    matchId, player1, "Character A"
            );

            matchDAO.addPlayer(
                    matchId, player2, "Character B"
            );

            matchDAO.addPlayer(
                    matchId, player3, "Character C"
            );

            check(

                    matchDAO.getMatchPlayers(matchId)
                            .size() == 3,

                    "Participant registration"
            );

            pass("Participant registration");

            // TEST 6: DUPLICATE PARTICIPANT

            boolean added = matchDAO.addPlayer(

                    matchId,

                    player1,

                    "Character A"
            );

            check(

                    !added

                            && matchDAO.getMatchPlayers(matchId)
                            .size() == 3,

                    "Duplicate participant"
            );

            pass("Duplicate participant protection");

            // TEST 7: SAVE SOLUTION

            matchDAO.setSolution(

                    matchId,

                    "Character B",

                    "Candlestick",

                    "Library"
            );

            match = matchDAO.getMatch(matchId)
                    .orElseThrow();

            check(

                    "Character B".equals(
                            match.solutionActor()
                    )

                            && "Candlestick".equals(
                            match.solutionWeapon()
                    )

                            && "Library".equals(
                            match.solutionRoom()
                    ),

                    "Match solution"
            );

            pass("Match solution persistence");

            // TEST 8: GAMEPLAY COUNTERS

            for (int i = 0; i < 3; i++) {

                matchDAO.recordTurn(
                        matchId, player1
                );

                matchDAO.recordDiceRoll(
                        matchId, player1
                );
            }

            matchDAO.recordMovement(
                    matchId, player1, 15
            );

            matchDAO.recordSuggestion(
                    matchId, player1
            );

            matchDAO.recordSuggestion(
                    matchId, player1
            );

            matchDAO.recordAccusation(
                    matchId, player1, true
            );

            matchDAO.recordAccusation(
                    matchId, player2, false
            );

            matchDAO.addPoints(
                    matchId, player1, 100
            );

            matchDAO.addPoints(
                    matchId, player2, 20
            );

            matchDAO.addPoints(
                    matchId, player3, 10
            );

            MatchPlayerSnapshot participant =
                    matchDAO.getMatchPlayer(

                            matchId,

                            player1

                    ).orElseThrow();

            check(

                    participant.turnsPlayed() == 3

                            && participant.diceRolls() == 3

                            && participant.movesMade() == 15

                            && participant.suggestionsMade() == 2

                            && participant.correctAccusations() == 1

                            && participant.pointsEarned() == 100,

                    "Gameplay counters"
            );

            pass("Gameplay counter tracking");

            // TEST 9: NO PREMATURE STATISTICS

            check(

                    getStats(
                            statisticsDAO, player1
                    ).gamesPlayed() == 0

                            && getStats(
                            statisticsDAO, player1
                    ).wins() == 0

                            && getStats(
                            statisticsDAO, player2
                    ).accusationsMade() == 0,

                    "Statistics applied prematurely"
            );

            pass("Statistics remain unchanged during match");

            // TEST 10: INVALID WINNER

            boolean rejected = false;

            try {

                matchDAO.completeMatch(

                        matchId,

                        UUID.randomUUID()
                );

            } catch (SQLException e) {

                rejected = true;
            }

            check(

                    rejected

                            && matchDAO.getMatch(matchId)
                            .orElseThrow()
                            .isInProgress()

                            && getStats(
                            statisticsDAO, player1
                    ).gamesPlayed() == 0,

                    "Invalid winner accepted"
            );

            pass("Invalid winner rejected");

            // TEST 11: COMPLETE MATCH

            var unlocks = service.completeMatch(

                    matchId,

                    player1
            );

            match = matchDAO.getMatch(matchId)
                    .orElseThrow();

            check(

                    match.isCompleted()

                            && match.winnerId().equals(player1)

                            && match.finishedAt() != null,

                    "Match completion"
            );

            pass("Match completion");

            // TEST 12: PLAYER RESULTS

            check(

                    matchDAO.getMatchPlayer(
                            matchId, player1
                    ).orElseThrow().hasWon()

                            && matchDAO.getMatchPlayer(
                            matchId, player2
                    ).orElseThrow().hasLost()

                            && matchDAO.getMatchPlayer(
                            matchId, player3
                    ).orElseThrow().hasLost(),

                    "Participant results"
            );

            pass("Winner and loser assignment");

            // TEST 13: AUTOMATIC STATISTICS

            StatisticsSnapshot winnerStats =
                    getStats(statisticsDAO, player1);

            StatisticsSnapshot loserStats =
                    getStats(statisticsDAO, player2);

            StatisticsSnapshot thirdStats =
                    getStats(statisticsDAO, player3);

            check(

                    winnerStats.gamesPlayed() == 1

                            && winnerStats.wins() == 1

                            && winnerStats.losses() == 0

                            && winnerStats.currentWinStreak() == 1

                            && winnerStats.bestWinStreak() == 1

                            && winnerStats.totalPoints() == 100

                            && winnerStats.correctAccusations() == 1

                            && winnerStats.totalGameDurationMillis()
                            == match.durationMillis()

                            && loserStats.gamesPlayed() == 1

                            && loserStats.losses() == 1

                            && loserStats.incorrectAccusations() == 1

                            && loserStats.totalPoints() == 20

                            && thirdStats.gamesPlayed() == 1

                            && thirdStats.losses() == 1

                            && thirdStats.totalPoints() == 10,

                    "Automatic statistics processing"
            );

            pass("Automatic player statistics processing");

            // TEST 14: ACHIEVEMENT INTEGRATION

            check(

                    achievementDAO.getProgress(

                                    player1,

                                    "WIN_GAMES"

                            ).orElseThrow()

                            .level().equals("BRONZE")

                            && unlocks.stream().anyMatch(

                            event ->
                                    event.playerId().equals(player1)

                                            && event.achievementCode()
                                            .equals("WIN_GAMES")
                    ),

                    "Achievement integration"
            );

            pass("Automatic achievement unlock");

            // TEST 15: DUPLICATE COMPLETION

            boolean completedAgain =
                    matchDAO.completeMatch(

                            matchId,

                            player1
                    );

            check(

                    !completedAgain

                            && winnerStats.equals(
                            getStats(statisticsDAO, player1)
                    )

                            && loserStats.equals(
                            getStats(statisticsDAO, player2)
                    )

                            && thirdStats.equals(
                            getStats(statisticsDAO, player3)
                    ),

                    "Duplicate completion inflated statistics"
            );

            pass("Duplicate match completion protection");

            // TEST 16: CONFLICTING WINNER

            rejected = false;

            try {

                matchDAO.completeMatch(

                        matchId,

                        player2
                );

            } catch (SQLException e) {

                rejected = true;
            }

            check(

                    rejected

                            && matchDAO.getMatch(matchId)
                            .orElseThrow()
                            .winnerId()
                            .equals(player1),

                    "Conflicting winner accepted"
            );

            pass("Conflicting winner rejected");

            // TEST 17: COMPLETED MATCH IMMUTABILITY

            rejected = false;

            try {

                matchDAO.recordTurn(

                        matchId,

                        player1
                );

            } catch (SQLException e) {

                rejected = true;
            }

            check(

                    rejected

                            && matchDAO.getMatchPlayer(

                                    matchId,

                                    player1

                            ).orElseThrow()

                            .turnsPlayed() == 3,

                    "Completed match was modified"
            );

            pass("Completed match counter protection");

            // TEST 18: CANCEL MATCH

            matchDAO.createMatch(
                    cancelledMatch, player1
            );

            matchDAO.addPlayer(
                    cancelledMatch, player1, "Character A"
            );

            matchDAO.addPlayer(
                    cancelledMatch, player2, "Character B"
            );

            matchDAO.recordTurn(
                    cancelledMatch, player1
            );

            matchDAO.recordAccusation(
                    cancelledMatch, player1, false
            );

            matchDAO.cancelMatch(cancelledMatch);

            check(

                    matchDAO.getMatch(cancelledMatch)
                            .orElseThrow()
                            .isCancelled()

                            && winnerStats.equals(
                            getStats(statisticsDAO, player1)
                    ),

                    "Cancelled match affected statistics"
            );

            pass("Cancelled matches do not award statistics");

            // TEST 19: CANCELLED MATCH COMPLETION

            rejected = false;

            try {

                matchDAO.completeMatch(

                        cancelledMatch,

                        player1
                );

            } catch (SQLException e) {

                rejected = true;
            }

            check(

                    rejected,

                    "Cancelled match could be completed"
            );

            pass("Cancelled match completion rejected");

            // TEST 20: SECOND COMPLETED MATCH

            matchDAO.createMatch(
                    secondMatch, player1
            );

            matchDAO.addPlayer(
                    secondMatch, player1, "Character A"
            );

            matchDAO.addPlayer(
                    secondMatch, player2, "Character B"
            );

            matchDAO.addPoints(
                    secondMatch, player1, 100
            );

            service.completeMatch(

                    secondMatch,

                    player1
            );

            check(

                    getStats(statisticsDAO, player1)
                            .wins() == 2

                            && getStats(statisticsDAO, player1)
                            .currentWinStreak() == 2

                            && getStats(statisticsDAO, player1)
                            .bestWinStreak() == 2

                            && achievementDAO.getProgress(

                                    player1,

                                    "CONSECUTIVE_WINS"

                            ).orElseThrow()

                            .level().equals("BRONZE"),

                    "Second match statistics"
            );

            pass("Consecutive match statistics and achievements");

            // TEST 21: MATCH HISTORY

            var history =
                    matchDAO.getMatchHistory(
                            player1, 20
                    );

            check(

                    history.size() == 3

                            && matchDAO.getCompletedMatchCount(
                            player1
                    ) == 2,

                    "Match history"
            );

            pass("Match history retrieval");

            // TEST 22: RESTART PERSISTENCE

            var savedStats =
                    getStats(statisticsDAO, player1);

            database.shutdown();

            database.initialize();

            service.initialize();

            check(

                    matchDAO.getMatch(matchId)
                            .orElseThrow()
                            .isCompleted()

                            && savedStats.equals(
                            getStats(statisticsDAO, player1)
                    )

                            && matchDAO.getMatchHistory(
                            player1, 20
                    ).size() == 3,

                    "Match restart persistence"
            );

            pass("Match persistence after server restart");

            // TEST 23: DUPLICATE AFTER RESTART

            completedAgain =
                    matchDAO.completeMatch(

                            matchId,

                            player1
                    );

            var repeatedUnlocks =
                    service.completeMatch(

                            matchId,

                            player1
                    );

            check(

                    !completedAgain

                            && savedStats.equals(
                            getStats(statisticsDAO, player1)
                    )

                            && repeatedUnlocks.isEmpty(),

                    "Duplicate match after restart"
            );

            pass("Duplicate protection survives server restart");

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            try {

                if (database.isInitialized()) {

                    database.execute(db -> {

                        for (UUID id : new UUID[] {

                                matchId,

                                cancelledMatch,

                                secondMatch

                        }) {

                            db.executeUpdate("""
                                    DELETE FROM match_players

                                    WHERE match_id = ?
                                    """,

                                    id.toString()
                            );

                            db.executeUpdate("""
                                    DELETE FROM matches

                                    WHERE match_id = ?
                                    """,

                                    id.toString()
                            );
                        }

                        for (UUID id : new UUID[] {

                                player1,

                                player2,

                                player3

                        }) {

                            db.executeUpdate("""
                                    DELETE FROM player_achievements

                                    WHERE player_id = ?
                                    """,

                                    id.toString()
                            );

                            db.executeUpdate("""
                                    DELETE FROM player_statistics

                                    WHERE player_id = ?
                                    """,

                                    id.toString()
                            );
                        }

                        return null;
                    });

                    for (UUID id : new UUID[] {

                            player1,

                            player2,

                            player3

                    }) {

                        if (playerDAO.playerExists(id)) {

                            playerDAO.deletePlayer(id);
                        }
                    }
                }

            } finally {

                database.shutdown();
            }
        }
    }

    // TEST UTILITIES

    private static StatisticsSnapshot getStats(

            StatisticsDAO dao,

            UUID playerId

    ) throws Exception {

        return dao.getOrCreateStatistics(playerId);
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
