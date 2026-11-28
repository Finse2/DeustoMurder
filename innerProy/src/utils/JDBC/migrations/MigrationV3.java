
package utils.JDBC.migrations;

import utils.JDBC.DBManager;
import utils.JDBC.DatabaseMigrationManager;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.PreparedStatement;

public final class MigrationV3
        implements DatabaseMigrationManager.Migration {

    @Override
    public int version() {
        return 3;
    }

    @Override
    public String description() {

        return "Add duplicate-safe match completion "
                + "and automatic statistics processing";
    }

    @Override
    public void apply(DBManager db)
            throws SQLException {

        createCompletionValidationTrigger(db);

        createStatisticsTrigger(db);
    }

    // VALIDATE MATCH COMPLETION

    private void createCompletionValidationTrigger(
            DBManager db
    ) throws SQLException {

        executeTrigger(db, """
                CREATE TRIGGER IF NOT EXISTS
                trg_validate_match_completion

                BEFORE UPDATE OF status ON matches

                WHEN NEW.status = 'COMPLETED'
                  AND OLD.status != 'COMPLETED'

                BEGIN

                    SELECT CASE

                        WHEN OLD.status != 'IN_PROGRESS'

                        THEN RAISE(
                            ABORT,
                            'Match is not in progress'
                        )

                    END;

                    SELECT CASE

                        WHEN NEW.winner_id IS NULL

                        THEN RAISE(
                            ABORT,
                            'Completed match requires a winner'
                        )

                    END;

                    SELECT CASE

                        WHEN NEW.finished_at IS NULL

                        THEN RAISE(
                            ABORT,
                            'Missing match completion timestamp'
                        )

                    END;

                    SELECT CASE

                        WHEN NOT EXISTS (

                            SELECT 1

                            FROM match_players

                            WHERE match_id = NEW.match_id

                              AND player_id = NEW.winner_id

                        )

                        THEN RAISE(
                            ABORT,
                            'Winner is not a match participant'
                        )

                    END;

                    SELECT CASE

                        WHEN (

                            SELECT COUNT(*)

                            FROM match_players

                            WHERE match_id = NEW.match_id

                        ) < 2

                        THEN RAISE(
                            ABORT,
                            'Match requires at least two players'
                        )

                    END;

                    SELECT CASE

                        WHEN EXISTS (

                            SELECT 1

                            FROM match_players mp

                            LEFT JOIN players p
                                ON p.player_id = mp.player_id

                            WHERE mp.match_id = NEW.match_id

                              AND p.player_id IS NULL

                        )

                        THEN RAISE(
                            ABORT,
                            'Match contains an unknown player'
                        )

                    END;

                END;
                """);
    }

    // AUTOMATIC STATISTICS PROCESSING

    private void createStatisticsTrigger(
            DBManager db
    ) throws SQLException {

        executeTrigger(db, """
                CREATE TRIGGER IF NOT EXISTS
                trg_apply_match_statistics

                AFTER UPDATE OF status ON matches

                WHEN OLD.status = 'IN_PROGRESS'

                  AND NEW.status = 'COMPLETED'

                BEGIN

                    UPDATE match_players

                    SET result = CASE

                        WHEN player_id = NEW.winner_id
                        THEN 'WIN'

                        ELSE 'LOSS'

                    END

                    WHERE match_id = NEW.match_id;


                    INSERT INTO player_statistics (

                        player_id,

                        games_played,

                        wins,
                        losses,

                        accusations_made,
                        correct_accusations,
                        incorrect_accusations,

                        current_win_streak,
                        best_win_streak,

                        total_game_duration_millis,

                        total_points

                    )

                    SELECT

                        player_id,

                        1,

                        CASE
                            WHEN result = 'WIN'
                            THEN 1 ELSE 0
                        END,

                        CASE
                            WHEN result = 'LOSS'
                            THEN 1 ELSE 0
                        END,

                        correct_accusations
                            + incorrect_accusations,

                        correct_accusations,

                        incorrect_accusations,

                        CASE
                            WHEN result = 'WIN'
                            THEN 1 ELSE 0
                        END,

                        CASE
                            WHEN result = 'WIN'
                            THEN 1 ELSE 0
                        END,

                        NEW.duration_millis,

                        points_earned

                    FROM match_players

                    WHERE match_id = NEW.match_id

                    ON CONFLICT(player_id) DO UPDATE SET

                        games_played =
                            player_statistics.games_played
                            + 1,

                        wins =
                            player_statistics.wins
                            + excluded.wins,

                        losses =
                            player_statistics.losses
                            + excluded.losses,

                        accusations_made =
                            player_statistics.accusations_made
                            + excluded.accusations_made,

                        correct_accusations =
                            player_statistics.correct_accusations
                            + excluded.correct_accusations,

                        incorrect_accusations =
                            player_statistics.incorrect_accusations
                            + excluded.incorrect_accusations,

                        current_win_streak = CASE

                            WHEN excluded.wins = 1

                            THEN
                                player_statistics.current_win_streak
                                + 1

                            ELSE 0

                        END,

                        best_win_streak = MAX(

                            player_statistics.best_win_streak,

                            CASE

                                WHEN excluded.wins = 1

                                THEN
                                    player_statistics.current_win_streak
                                    + 1

                                ELSE 0

                            END
                        ),

                        total_game_duration_millis =
                            player_statistics.total_game_duration_millis
                            + excluded.total_game_duration_millis,

                        total_points =
                            player_statistics.total_points
                            + excluded.total_points;

                END;
                """);
    }

    // VERIFY MIGRATION

    @Override
    public void verify(DBManager db)
            throws SQLException {

        verifyTrigger(
                db,
                "trg_validate_match_completion"
        );

        verifyTrigger(
                db,
                "trg_apply_match_statistics"
        );
    }

    private void executeTrigger(
            DBManager db,
            String sql
    ) throws SQLException {

        try (PreparedStatement statement =
                     db.getConnection().prepareStatement(sql)) {

            statement.executeUpdate();
        }
    }

    private void verifyTrigger(
            DBManager db,
            String name
    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT name

                FROM sqlite_master

                WHERE type = 'trigger'

                  AND name = ?
                """,

                name
        )) {

            if (!rs.next()) {

                throw new SQLException(
                        "Missing database trigger: " + name
                );
            }
        }
    }
}
