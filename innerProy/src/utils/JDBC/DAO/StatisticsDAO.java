
package utils.JDBC.DAO;

import game.statistics.StatisticsSnapshot;
import utils.JDBC.DBManager;
import utils.JDBC.DatabaseService;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class StatisticsDAO {

    private final DatabaseService database;

    // MATCH OUTCOMES

    public enum MatchResult {

        WIN,
        LOSS,
        DRAW
    }

    // LEADERBOARD MODEL

    public record LeaderboardEntry(

            int position,
            String displayName,
            StatisticsSnapshot statistics

    ) {}

    // CONSTRUCTORS

    public StatisticsDAO() {

        this(DatabaseService.getInstance());
    }

    public StatisticsDAO(DatabaseService database) {

        this.database = Objects.requireNonNull(
                database, "database"
        );
    }

    // INITIALIZE STATISTICS

    public void initializeStatistics(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        database.execute(db -> {

            initializeStatistics(db, playerId);

            return null;
        });
    }

    private void initializeStatistics(
            DBManager db,
            UUID playerId
    ) throws SQLException {

        db.executeUpdate("""
                INSERT INTO player_statistics (
                    player_id
                )
                SELECT player_id
                FROM players
                WHERE player_id = ?
                ON CONFLICT(player_id) DO NOTHING
                """,

                playerId.toString()
        );

        if (findStatistics(db, playerId).isEmpty()) {

            throw new SQLException(
                    "Cannot initialize statistics "
                            + "for unknown player: "
                            + playerId
            );
        }
    }

    // GET STATISTICS

    public Optional<StatisticsSnapshot> getStatistics(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(
                db -> findStatistics(db, playerId)
        );
    }

    private Optional<StatisticsSnapshot> findStatistics(
            DBManager db,
            UUID playerId
    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT *
                FROM player_statistics
                WHERE player_id = ?
                """,

                playerId.toString()
        )) {

            if (rs.next()) {

                return Optional.of(
                        mapStatistics(rs)
                );
            }

            return Optional.empty();
        }
    }

    // GET OR CREATE

    public StatisticsSnapshot getOrCreateStatistics(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db -> {

            initializeStatistics(db, playerId);

            return findStatistics(db, playerId)
                    .orElseThrow(() ->
                            new SQLException(
                                    "Statistics not found."
                            )
                    );
        });
    }

    // RECORD WIN

    public void recordWin(
            UUID playerId,
            long durationMillis,
            long points
    ) throws Exception {

        recordMatchResult(

                playerId,
                MatchResult.WIN,
                durationMillis,
                points
        );
    }

    // RECORD LOSS

    public void recordLoss(
            UUID playerId,
            long durationMillis,
            long points
    ) throws Exception {

        recordMatchResult(

                playerId,
                MatchResult.LOSS,
                durationMillis,
                points
        );
    }

    // RECORD DRAW

    public void recordDraw(
            UUID playerId,
            long durationMillis,
            long points
    ) throws Exception {

        recordMatchResult(

                playerId,
                MatchResult.DRAW,
                durationMillis,
                points
        );
    }

    // RECORD MATCH RESULT

    public void recordMatchResult(
            UUID playerId,
            MatchResult result,
            long durationMillis,
            long points
    ) throws Exception {

        Objects.requireNonNull(playerId);
        Objects.requireNonNull(result);

        validateNonNegative(
                durationMillis, "durationMillis"
        );

        validateNonNegative(
                points, "points"
        );

        int win = result == MatchResult.WIN ? 1 : 0;

        int loss = result == MatchResult.LOSS ? 1 : 0;

        database.execute(db -> {

            int affected = db.executeUpdate("""
                    INSERT INTO player_statistics (

                        player_id,
                        games_played,
                        wins,
                        losses,
                        current_win_streak,
                        best_win_streak,
                        total_game_duration_millis,
                        total_points

                    )

                    SELECT
                        player_id,
                        1,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?

                    FROM players

                    WHERE player_id = ?

                    ON CONFLICT(player_id) DO UPDATE SET

                        games_played =
                            player_statistics.games_played + 1,

                        wins =
                            player_statistics.wins
                            + excluded.wins,

                        losses =
                            player_statistics.losses
                            + excluded.losses,

                        current_win_streak =
                            CASE
                                WHEN excluded.wins = 1
                                THEN player_statistics.current_win_streak + 1
                                ELSE 0
                            END,

                        best_win_streak =
                            MAX(
                                player_statistics.best_win_streak,

                                CASE
                                    WHEN excluded.wins = 1
                                    THEN player_statistics.current_win_streak + 1
                                    ELSE 0
                                END
                            ),

                        total_game_duration_millis =
                            player_statistics.total_game_duration_millis
                            + excluded.total_game_duration_millis,

                        total_points =
                            player_statistics.total_points
                            + excluded.total_points
                    """,

                    win,
                    loss,
                    win,
                    win,
                    durationMillis,
                    points,
                    playerId.toString()
            );

            if (affected != 1) {

                throw new SQLException(
                        "Could not record match result "
                                + "for player: "
                                + playerId
                );
            }

            return null;
        });
    }

    // RECORD ACCUSATION

    public void recordAccusation(
            UUID playerId,
            boolean correct
    ) throws Exception {

        Objects.requireNonNull(playerId);

        int correctCount = correct ? 1 : 0;

        int incorrectCount = correct ? 0 : 1;

        database.execute(db -> {

            int affected = db.executeUpdate("""
                    INSERT INTO player_statistics (

                        player_id,
                        accusations_made,
                        correct_accusations,
                        incorrect_accusations
                    )

                    SELECT
                        player_id,
                        1,
                        ?,
                        ?

                    FROM players

                    WHERE player_id = ?

                    ON CONFLICT(player_id) DO UPDATE SET

                        accusations_made =
                            player_statistics.accusations_made + 1,

                        correct_accusations =
                            player_statistics.correct_accusations
                            + excluded.correct_accusations,

                        incorrect_accusations =
                            player_statistics.incorrect_accusations
                            + excluded.incorrect_accusations
                    """,

                    correctCount,
                    incorrectCount,
                    playerId.toString()
            );

            if (affected != 1) {

                throw new SQLException(
                        "Could not record accusation "
                                + "for player: "
                                + playerId
                );
            }

            return null;
        });
    }

    // GET ALL STATISTICS

    public List<StatisticsSnapshot> getAllStatistics()
            throws Exception {

        return database.execute(db -> {

            List<StatisticsSnapshot> statistics =
                    new ArrayList<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT *
                    FROM player_statistics
                    ORDER BY
                        wins DESC,
                        player_id ASC
                    """)) {

                while (rs.next()) {

                    statistics.add(
                            mapStatistics(rs)
                    );
                }
            }

            return List.copyOf(statistics);
        });
    }

    // LEADERBOARD

    public List<LeaderboardEntry> getLeaderboard(
            int limit
    ) throws Exception {

        if (limit <= 0) {

            throw new IllegalArgumentException(
                    "Leaderboard limit must be positive."
            );
        }

        return database.execute(db -> {

            List<LeaderboardEntry> entries =
                    new ArrayList<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT
                        p.display_name,
                        s.*
                    FROM player_statistics s

                    INNER JOIN players p
                        ON p.player_id = s.player_id

                    WHERE s.games_played > 0

                    ORDER BY
                        s.wins DESC,
                        (1.0 * s.wins / s.games_played) DESC,
                        s.total_points DESC,
                        s.player_id ASC

                    LIMIT ?
                    """,

                    limit
            )) {

                int position = 1;

                while (rs.next()) {

                    entries.add(

                            new LeaderboardEntry(

                                    position++,

                                    rs.getString(
                                            "display_name"
                                    ),

                                    mapStatistics(rs)
                            )
                    );
                }
            }

            return List.copyOf(entries);
        });
    }

    // RESULT SET MAPPING

    private StatisticsSnapshot mapStatistics(
            ResultSet rs
    ) throws SQLException {

        return new StatisticsSnapshot(

                UUID.fromString(
                        rs.getString("player_id")
                ),

                rs.getLong("games_played"),

                rs.getLong("wins"),

                rs.getLong("losses"),

                rs.getLong("accusations_made"),

                rs.getLong("correct_accusations"),

                rs.getLong("incorrect_accusations"),

                rs.getLong("current_win_streak"),

                rs.getLong("best_win_streak"),

                rs.getLong(
                        "total_game_duration_millis"
                ),

                rs.getLong("total_points")
        );
    }

    // VALIDATION

    private static void validateNonNegative(
            long value,
            String field
    ) {

        if (value < 0) {

            throw new IllegalArgumentException(
                    field + " cannot be negative."
            );
        }
    }
}
