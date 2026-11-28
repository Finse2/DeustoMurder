
package utils.JDBC.DAO;

import game.statistics.MatchSnapshot;
import game.statistics.MatchPlayerSnapshot;

import utils.JDBC.DBManager;
import utils.JDBC.DatabaseService;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class MatchDAO {

    private final DatabaseService database;

    // GAMEPLAY COUNTERS

    private enum Counter {

        TURNS("turns_played"),

        DICE_ROLLS("dice_rolls"),

        MOVES("moves_made"),

        SUGGESTIONS("suggestions_made"),

        CORRECT_ACCUSATIONS("correct_accusations"),

        INCORRECT_ACCUSATIONS("incorrect_accusations"),

        POINTS("points_earned");

        private final String column;

        Counter(String column) {

            this.column = column;
        }
    }

    // CONSTRUCTORS

    public MatchDAO() {

        this(DatabaseService.getInstance());
    }

    public MatchDAO(DatabaseService database) {

        this.database = Objects.requireNonNull(
                database,
                "database"
        );
    }

    // CREATE MATCH

    public UUID createMatch(
            UUID hostId
    ) throws Exception {

        UUID matchId = UUID.randomUUID();

        createMatch(matchId, hostId);

        return matchId;
    }

    public void createMatch(

            UUID matchId,

            UUID hostId

    ) throws Exception {

        Objects.requireNonNull(matchId);

        Objects.requireNonNull(hostId);

        database.execute(db -> {

            long now = System.currentTimeMillis();

            int affected = db.executeUpdate("""
                    INSERT INTO matches (

                        match_id,

                        host_id,

                        started_at,

                        status
                    )

                    SELECT

                        ?,

                        player_id,

                        ?,

                        'IN_PROGRESS'

                    FROM players

                    WHERE player_id = ?

                    ON CONFLICT(match_id)

                    DO NOTHING
                    """,

                    matchId.toString(),

                    now,

                    hostId.toString()
            );

            if (affected == 0) {

                MatchSnapshot existing =
                        requireMatch(db, matchId);

                if (!hostId.equals(existing.hostId())) {

                    throw new SQLException(
                            "Conflicting match identifier."
                    );
                }
            }

            return null;
        });
    }

    // ADD MATCH PARTICIPANT

    public boolean addPlayer(

            UUID matchId,

            UUID playerId,

            String characterName

    ) throws Exception {

        Objects.requireNonNull(matchId);

        Objects.requireNonNull(playerId);

        Objects.requireNonNull(characterName);

        if (characterName.isBlank()) {

            throw new IllegalArgumentException(
                    "Character name cannot be empty."
            );
        }

        return database.execute(db -> {

            int affected = db.executeUpdate("""
                    INSERT INTO match_players (

                        match_id,

                        player_id,

                        character_name,

                        result
                    )

                    SELECT

                        m.match_id,

                        p.player_id,

                        ?,

                        'PENDING'

                    FROM matches m

                    CROSS JOIN players p

                    WHERE m.match_id = ?

                      AND p.player_id = ?

                      AND m.status = 'IN_PROGRESS'

                    ON CONFLICT(

                        match_id,

                        player_id

                    ) DO NOTHING
                    """,

                    characterName,

                    matchId.toString(),

                    playerId.toString()
            );

            if (affected == 1) {

                return true;
            }

            Optional<MatchPlayerSnapshot> existing =
                    findMatchPlayer(

                            db,

                            matchId,

                            playerId
                    );

            if (existing.isPresent()

                    && characterName.equals(
                    existing.get().characterName()
            )) {

                return false;
            }

            throw new SQLException(
                    "Could not register match participant."
            );
        });
    }

    // SAVE SECRET SOLUTION

    public void setSolution(

            UUID matchId,

            String actor,

            String weapon,

            String room

    ) throws Exception {

        Objects.requireNonNull(matchId);

        Objects.requireNonNull(actor);

        Objects.requireNonNull(weapon);

        Objects.requireNonNull(room);

        database.execute(db -> {

            int affected = db.executeUpdate("""
                    UPDATE matches

                    SET

                        solution_actor = ?,

                        solution_weapon = ?,

                        solution_room = ?

                    WHERE match_id = ?

                      AND status = 'IN_PROGRESS'
                    """,

                    actor,

                    weapon,

                    room,

                    matchId.toString()
            );

            if (affected != 1) {

                throw new SQLException(
                        "Could not save match solution."
                );
            }

            return null;
        });
    }

    // RECORD TURN

    public void recordTurn(

            UUID matchId,

            UUID playerId

    ) throws Exception {

        incrementCounter(

                matchId,

                playerId,

                Counter.TURNS,

                1
        );
    }

    // RECORD DICE ROLL

    public void recordDiceRoll(

            UUID matchId,

            UUID playerId

    ) throws Exception {

        incrementCounter(

                matchId,

                playerId,

                Counter.DICE_ROLLS,

                1
        );
    }

    // RECORD MOVEMENT

    public void recordMovement(

            UUID matchId,

            UUID playerId,

            long tilesMoved

    ) throws Exception {

        incrementCounter(

                matchId,

                playerId,

                Counter.MOVES,

                tilesMoved
        );
    }

    // RECORD SUGGESTION

    public void recordSuggestion(

            UUID matchId,

            UUID playerId

    ) throws Exception {

        incrementCounter(

                matchId,

                playerId,

                Counter.SUGGESTIONS,

                1
        );
    }

    // RECORD ACCUSATION

    public void recordAccusation(

            UUID matchId,

            UUID playerId,

            boolean correct

    ) throws Exception {

        incrementCounter(

                matchId,

                playerId,

                correct

                        ? Counter.CORRECT_ACCUSATIONS

                        : Counter.INCORRECT_ACCUSATIONS,

                1
        );
    }

    // ADD POINTS

    public void addPoints(

            UUID matchId,

            UUID playerId,

            long points

    ) throws Exception {

        incrementCounter(

                matchId,

                playerId,

                Counter.POINTS,

                points
        );
    }

    // SHARED COUNTER UPDATE

    private void incrementCounter(

            UUID matchId,

            UUID playerId,

            Counter counter,

            long amount

    ) throws Exception {

        Objects.requireNonNull(matchId);

        Objects.requireNonNull(playerId);

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Counter increment cannot be negative."
            );
        }

        database.execute(db -> {

            // The column name comes exclusively
            // from our internal Counter enum.

            String column = counter.column;

            String sql = """

                    UPDATE match_players

                    SET %s = %s + ?

                    WHERE match_id = ?

                      AND player_id = ?

                      AND EXISTS (

                          SELECT 1

                          FROM matches

                          WHERE match_id = ?

                            AND status = 'IN_PROGRESS'
                      )

                    """.formatted(column, column);

            int affected = db.executeUpdate(

                    sql,

                    amount,

                    matchId.toString(),

                    playerId.toString(),

                    matchId.toString()
            );

            if (affected != 1) {

                throw new SQLException(

                        "Could not update match counter: "

                                + column
                );
            }

            return null;
        });
    }

    // COMPLETE MATCH

    public boolean completeMatch(

            UUID matchId,

            UUID winnerId

    ) throws Exception {

        Objects.requireNonNull(matchId);

        Objects.requireNonNull(winnerId);

        return database.execute(db -> {

            long now = System.currentTimeMillis();

            // The V3 triggers automatically:
            //
            // 1. Validate the winner.
            // 2. Validate the participants.
            // 3. Assign WIN and LOSS results.
            // 4. Update persistent statistics.
            //
            // All happen within this SQL statement.

            int affected = db.executeUpdate("""
                    UPDATE matches

                    SET

                        status = 'COMPLETED',

                        winner_id = ?,

                        finished_at = MAX(
                            started_at,
                            ?
                        ),

                        duration_millis = MAX(
                            0,
                            ? - started_at
                        )

                    WHERE match_id = ?

                      AND status = 'IN_PROGRESS'
                    """,

                    winnerId.toString(),

                    now,

                    now,

                    matchId.toString()
            );

            if (affected == 1) {

                return true;
            }

            MatchSnapshot existing =
                    requireMatch(db, matchId);

            // Duplicate completion with the same winner.

            if (existing.isCompleted()

                    && winnerId.equals(
                    existing.winnerId()
            )) {

                return false;
            }

            if (existing.isCompleted()) {

                throw new SQLException(
                        "Match already completed "
                                + "with a different winner."
                );
            }

            throw new SQLException(
                    "Match cannot be completed."
            );
        });
    }

    // CANCEL MATCH

    public boolean cancelMatch(
            UUID matchId
    ) throws Exception {

        Objects.requireNonNull(matchId);

        return database.execute(db -> {

            long now = System.currentTimeMillis();

            int affected = db.executeUpdate("""
                    UPDATE matches

                    SET

                        status = 'CANCELLED',

                        finished_at = MAX(
                            started_at,
                            ?
                        ),

                        duration_millis = MAX(
                            0,
                            ? - started_at
                        )

                    WHERE match_id = ?

                      AND status = 'IN_PROGRESS'
                    """,

                    now,

                    now,

                    matchId.toString()
            );

            if (affected == 1) {

                return true;
            }

            MatchSnapshot existing =
                    requireMatch(db, matchId);

            if (existing.isCancelled()) {

                return false;
            }

            throw new SQLException(
                    "Completed matches cannot be cancelled."
            );
        });
    }

    // GET MATCH

    public Optional<MatchSnapshot> getMatch(
            UUID matchId
    ) throws Exception {

        Objects.requireNonNull(matchId);

        return database.execute(
                db -> findMatch(db, matchId)
        );
    }

    private Optional<MatchSnapshot> findMatch(

            DBManager db,

            UUID matchId

    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT *

                FROM matches

                WHERE match_id = ?
                """,

                matchId.toString()
        )) {

            if (rs.next()) {

                return Optional.of(
                        mapMatch(rs)
                );
            }

            return Optional.empty();
        }
    }

    private MatchSnapshot requireMatch(

            DBManager db,

            UUID matchId

    ) throws SQLException {

        return findMatch(

                db,

                matchId

        ).orElseThrow(() ->

                new SQLException(

                        "Unknown match: " + matchId
                )
        );
    }

    // GET MATCH PARTICIPANT

    public Optional<MatchPlayerSnapshot> getMatchPlayer(

            UUID matchId,

            UUID playerId

    ) throws Exception {

        Objects.requireNonNull(matchId);

        Objects.requireNonNull(playerId);

        return database.execute(db ->

                findMatchPlayer(

                        db,

                        matchId,

                        playerId
                )
        );
    }

    private Optional<MatchPlayerSnapshot> findMatchPlayer(

            DBManager db,

            UUID matchId,

            UUID playerId

    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT *

                FROM match_players

                WHERE match_id = ?

                  AND player_id = ?
                """,

                matchId.toString(),

                playerId.toString()
        )) {

            if (rs.next()) {

                return Optional.of(
                        mapMatchPlayer(rs)
                );
            }

            return Optional.empty();
        }
    }

    // GET ALL MATCH PARTICIPANTS

    public List<MatchPlayerSnapshot> getMatchPlayers(
            UUID matchId
    ) throws Exception {

        Objects.requireNonNull(matchId);

        return database.execute(db -> {

            List<MatchPlayerSnapshot> players =
                    new ArrayList<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT *

                    FROM match_players

                    WHERE match_id = ?

                    ORDER BY

                        CASE result

                            WHEN 'WIN' THEN 0

                            ELSE 1

                        END,

                        player_id
                    """,

                    matchId.toString()
            )) {

                while (rs.next()) {

                    players.add(
                            mapMatchPlayer(rs)
                    );
                }
            }

            return List.copyOf(players);
        });
    }

    // GET MATCH HISTORY

    public List<MatchSnapshot> getMatchHistory(

            UUID playerId,

            int limit

    ) throws Exception {

        Objects.requireNonNull(playerId);

        validateLimit(limit);

        return database.execute(db -> {

            List<MatchSnapshot> matches =
                    new ArrayList<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT m.*

                    FROM matches m

                    INNER JOIN match_players mp

                        ON mp.match_id = m.match_id

                    WHERE mp.player_id = ?

                      AND m.status != 'IN_PROGRESS'

                    ORDER BY

                        m.started_at DESC,

                        m.match_id DESC

                    LIMIT ?
                    """,

                    playerId.toString(),

                    limit
            )) {

                while (rs.next()) {

                    matches.add(
                            mapMatch(rs)
                    );
                }
            }

            return List.copyOf(matches);
        });
    }

    // GET ACTIVE MATCHES

    public List<MatchSnapshot> getActiveMatches()
            throws Exception {

        return database.execute(db -> {

            List<MatchSnapshot> matches =
                    new ArrayList<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT *

                    FROM matches

                    WHERE status = 'IN_PROGRESS'

                    ORDER BY started_at DESC
                    """)) {

                while (rs.next()) {

                    matches.add(
                            mapMatch(rs)
                    );
                }
            }

            return List.copyOf(matches);
        });
    }

    // COMPLETED MATCH COUNT

    public long getCompletedMatchCount(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db -> {

            try (ResultSet rs = db.executeQuery("""
                    SELECT COUNT(*) AS total

                    FROM matches m

                    INNER JOIN match_players mp

                        ON mp.match_id = m.match_id

                    WHERE mp.player_id = ?

                      AND m.status = 'COMPLETED'
                    """,

                    playerId.toString()
            )) {

                return rs.next()

                        ? rs.getLong("total")

                        : 0L;
            }
        });
    }

    // MATCH MAPPING

    private MatchSnapshot mapMatch(
            ResultSet rs
    ) throws SQLException {

        return new MatchSnapshot(

                UUID.fromString(
                        rs.getString("match_id")
                ),

                nullableUUID(rs, "host_id"),

                nullableUUID(rs, "winner_id"),

                rs.getLong("started_at"),

                nullableLong(rs, "finished_at"),

                rs.getLong("duration_millis"),

                MatchSnapshot.Status.valueOf(
                        rs.getString("status")
                ),

                rs.getString("solution_actor"),

                rs.getString("solution_weapon"),

                rs.getString("solution_room")
        );
    }

    // PARTICIPANT MAPPING

    private MatchPlayerSnapshot mapMatchPlayer(
            ResultSet rs
    ) throws SQLException {

        return new MatchPlayerSnapshot(

                UUID.fromString(
                        rs.getString("match_id")
                ),

                UUID.fromString(
                        rs.getString("player_id")
                ),

                rs.getString("character_name"),

                MatchPlayerSnapshot.Result.valueOf(
                        rs.getString("result")
                ),

                rs.getLong("turns_played"),

                rs.getLong("dice_rolls"),

                rs.getLong("moves_made"),

                rs.getLong("suggestions_made"),

                rs.getLong("correct_accusations"),

                rs.getLong("incorrect_accusations"),

                rs.getLong("points_earned")
        );
    }

    // UTILITIES

    private static UUID nullableUUID(

            ResultSet rs,

            String column

    ) throws SQLException {

        String value = rs.getString(column);

        return value == null

                ? null

                : UUID.fromString(value);
    }

    private static Long nullableLong(

            ResultSet rs,

            String column

    ) throws SQLException {

        long value = rs.getLong(column);

        return rs.wasNull()

                ? null

                : value;
    }

    private static void validateLimit(
            int limit
    ) {

        if (limit <= 0) {

            throw new IllegalArgumentException(
                    "Limit must be positive."
            );
        }
    }
}
