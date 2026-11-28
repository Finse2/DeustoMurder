
package utils.JDBC.DAO;

import model.PlayerProfile;
import utils.JDBC.DatabaseService;
import utils.JDBC.DBManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PlayerDAO {

    private final DatabaseService database;

    // CONSTRUCTORS

    public PlayerDAO() {

        this(DatabaseService.getInstance());
    }

    public PlayerDAO(DatabaseService database) {

        this.database = Objects.requireNonNull(
                database, "database"
        );
    }

    // CREATE PLAYER

    public PlayerProfile createPlayer(
            String displayName
    ) throws Exception {

        return createPlayer(
                UUID.randomUUID(),
                displayName
        );
    }

    public PlayerProfile createPlayer(
            UUID playerId,
            String displayName
    ) throws Exception {

        Objects.requireNonNull(playerId);

        String name = validateName(displayName);

        long now = System.currentTimeMillis();

        return database.execute(db -> {

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
                    name,
                    now,
                    now
            );

            return new PlayerProfile(
                    playerId,
                    name,
                    null,
                    now,
                    now
            );
        });
    }

    // GET OR CREATE

    public PlayerProfile getOrCreatePlayer(
            UUID playerId,
            String displayName
    ) throws Exception {

        Objects.requireNonNull(playerId);

        String name = validateName(displayName);

        long now = System.currentTimeMillis();

        return database.execute(db -> {

            db.executeUpdate("""
                    INSERT INTO players (
                        player_id,
                        display_name,
                        created_at,
                        last_seen
                    )
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT(player_id) DO NOTHING
                    """,

                    playerId.toString(),
                    name,
                    now,
                    now
            );

            return findPlayer(db, playerId)
                    .orElseThrow(() ->
                            new SQLException(
                                    "Player could not be retrieved."
                            )
                    );
        });
    }

    // GET PLAYER

    public Optional<PlayerProfile> getPlayer(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(
                db -> findPlayer(db, playerId)
        );
    }

    private Optional<PlayerProfile> findPlayer(
            DBManager db,
            UUID playerId
    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT
                    player_id,
                    display_name,
                    profile_image,
                    created_at,
                    last_seen
                FROM players
                WHERE player_id = ?
                """,

                playerId.toString()
        )) {

            if (rs.next()) {

                return Optional.of(
                        mapPlayer(rs)
                );
            }

            return Optional.empty();
        }
    }

    // GET ALL PLAYERS

    public List<PlayerProfile> getAllPlayers()
            throws Exception {

        return database.execute(db -> {

            List<PlayerProfile> players =
                    new ArrayList<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT *
                    FROM players
                    ORDER BY
                        display_name COLLATE NOCASE,
                        player_id
                    """)) {

                while (rs.next()) {

                    players.add(
                            mapPlayer(rs)
                    );
                }
            }

            return players;
        });
    }

    // PLAYER EXISTS

    public boolean playerExists(UUID playerId)
            throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db -> {

            try (ResultSet rs = db.executeQuery("""
                    SELECT 1
                    FROM players
                    WHERE player_id = ?
                    """,

                    playerId.toString()
            )) {

                return rs.next();
            }
        });
    }

    // UPDATE DISPLAY NAME

    public boolean updateName(
            UUID playerId,
            String displayName
    ) throws Exception {

        Objects.requireNonNull(playerId);

        String name = validateName(displayName);

        return database.execute(db ->

                db.executeUpdate("""
                        UPDATE players
                        SET display_name = ?
                        WHERE player_id = ?
                        """,

                        name,
                        playerId.toString()
                ) > 0
        );
    }

    // UPDATE PROFILE IMAGE

    public boolean updateProfileImage(
            UUID playerId,
            String imagePath
    ) throws Exception {

        Objects.requireNonNull(playerId);

        if (imagePath == null || imagePath.isBlank()) {
            return removeProfileImage(playerId);
        }

        return database.execute(db ->

                db.executeUpdate("""
                        UPDATE players
                        SET profile_image = ?
                        WHERE player_id = ?
                        """,

                        imagePath,
                        playerId.toString()
                ) > 0
        );
    }

    public boolean removeProfileImage(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db ->

                db.executeUpdate("""
                        UPDATE players
                        SET profile_image = NULL
                        WHERE player_id = ?
                        """,

                        playerId.toString()
                ) > 0
        );
    }

    // UPDATE LAST SEEN

    public boolean updateLastSeen(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db ->

                db.executeUpdate("""
                        UPDATE players
                        SET last_seen = MAX(last_seen, ?)
                        WHERE player_id = ?
                        """,

                        System.currentTimeMillis(),
                        playerId.toString()
                ) > 0
        );
    }

    // SEARCH PLAYERS

    public List<PlayerProfile> searchPlayers(
            String search
    ) throws Exception {

        Objects.requireNonNull(search);

        String escaped = search.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        String pattern = "%" + escaped + "%";

        return database.execute(db -> {

            List<PlayerProfile> players =
                    new ArrayList<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT *
                    FROM players
                    WHERE display_name LIKE ?
                        ESCAPE '\\'
                    ORDER BY display_name COLLATE NOCASE
                    """,

                    pattern
            )) {

                while (rs.next()) {

                    players.add(
                            mapPlayer(rs)
                    );
                }
            }

            return players;
        });
    }

    // PLAYER COUNT

    public long getPlayerCount() throws Exception {

        return database.execute(db -> {

            try (ResultSet rs = db.executeQuery("""
                    SELECT COUNT(*) AS total
                    FROM players
                    """)) {

                if (!rs.next()) {

                    throw new SQLException(
                            "Could not count players."
                    );
                }

                return rs.getLong("total");
            }
        });
    }

    // DELETE PLAYER

    public boolean deletePlayer(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db -> {

            String id = playerId.toString();

            // Do not delete players who have
            // dependent historical records.

            try (ResultSet rs = db.executeQuery("""
                    SELECT
                        EXISTS(
                            SELECT 1
                            FROM player_statistics
                            WHERE player_id = ?
                        )
                        OR EXISTS(
                            SELECT 1
                            FROM player_achievements
                            WHERE player_id = ?
                        )
                        OR EXISTS(
                            SELECT 1
                            FROM player_settings
                            WHERE player_id = ?
                        )
                        OR EXISTS(
                            SELECT 1
                            FROM match_players
                            WHERE player_id = ?
                        )
                        OR EXISTS(
                            SELECT 1
                            FROM matches
                            WHERE host_id = ?
                               OR winner_id = ?
                        ) AS has_dependencies
                    """,

                    id, id, id, id, id, id
            )) {

                if (rs.next()
                        && rs.getInt("has_dependencies") != 0) {

                    throw new SQLException(
                            "Cannot delete player with "
                                    + "dependent records."
                    );
                }
            }

            return db.executeUpdate("""
                    DELETE FROM players
                    WHERE player_id = ?
                    """,

                    id
            ) > 0;
        });
    }

    // RESULT SET MAPPING

    private PlayerProfile mapPlayer(
            ResultSet rs
    ) throws SQLException {

        return new PlayerProfile(

                UUID.fromString(
                        rs.getString("player_id")
                ),

                rs.getString("display_name"),

                rs.getString("profile_image"),

                rs.getLong("created_at"),

                rs.getLong("last_seen")
        );
    }

    // VALIDATION

    private static String validateName(
            String displayName
    ) {

        Objects.requireNonNull(
                displayName, "displayName"
        );

        String name = displayName.trim();

        if (name.isBlank()) {

            throw new IllegalArgumentException(
                    "Display name cannot be empty."
            );
        }

        return name;
    }
}
