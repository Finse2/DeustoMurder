
package utils.JDBC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public final class DatabaseInitializer {

    private static final Logger LOGGER =
            Logger.getLogger(
                    DatabaseInitializer.class.getName()
            );

    public static final int SCHEMA_VERSION = 1;

    private final DBManager db;

    public DatabaseInitializer(DBManager db) {

        this.db = Objects.requireNonNull(db, "db");
    }

    // INITIALIZATION

    public synchronized void initialize()
            throws SQLException {

        LOGGER.info("Initializing DeustoMurder database...");

        enableForeignKeys();

        createSchemaVersionTable();

        int currentVersion = getSchemaVersion();

        if (currentVersion > SCHEMA_VERSION) {

            throw new SQLException(
                    "Database schema is newer than "
                            + "the supported application version."
            );
        }

        if (currentVersion != 0
                && currentVersion != SCHEMA_VERSION) {

            throw new SQLException(
                    "Database migration required."
            );
        }

        boolean transactional = db.isSQLite();

        if (transactional) {
            db.beginTransaction();
        }

        try {

            createPlayersTable();
            createStatisticsTable();

            createAchievementsTable();
            createPlayerAchievementsTable();

            createMatchesTable();
            createMatchPlayersTable();

            createSettingsTable();

            createIndexes();

            verifySchema();

            if (currentVersion == 0) {
                saveSchemaVersion(SCHEMA_VERSION);
            }

            if (transactional) {
                db.commit();
            }

            LOGGER.info(
                    "Database initialized successfully. "
                            + "Schema version: "
                            + SCHEMA_VERSION
            );

        } catch (SQLException | RuntimeException e) {

            if (transactional) {

                try {
                    db.rollback();

                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }

            throw e;
        }
    }

    // FOREIGN KEYS

    private void enableForeignKeys()
            throws SQLException {

        if (db.isSQLite()) {
            db.execute("PRAGMA foreign_keys = ON");
        }

        // For rqlite, foreign key enforcement must
        // be configured and verified on the server.
    }

    // SCHEMA VERSION

    private void createSchemaVersionTable()
            throws SQLException {

        db.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    id INTEGER PRIMARY KEY CHECK (id = 1),

                    version INTEGER NOT NULL
                        CHECK (version >= 1),

                    updated_at INTEGER NOT NULL
                )
                """);
    }

    public int getSchemaVersion()
            throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT version
                FROM schema_version
                WHERE id = 1
                """)) {

            return rs.next()
                    ? rs.getInt("version")
                    : 0;
        }
    }

    private void saveSchemaVersion(int version)
            throws SQLException {

        db.executeUpdate("""
                INSERT INTO schema_version (
                    id,
                    version,
                    updated_at
                )
                VALUES (1, ?, ?)
                ON CONFLICT(id) DO NOTHING
                """,
                version,
                System.currentTimeMillis()
        );

        if (getSchemaVersion() != version) {

            throw new SQLException(
                    "Unexpected database schema version."
            );
        }
    }

    // PLAYERS

    private void createPlayersTable()
            throws SQLException {

        db.execute("""
                CREATE TABLE IF NOT EXISTS players (

                    player_id TEXT PRIMARY KEY NOT NULL,

                    display_name TEXT NOT NULL
                        CHECK (length(trim(display_name)) > 0),

                    profile_image TEXT,

                    created_at INTEGER NOT NULL,

                    last_seen INTEGER NOT NULL

                )
                """);
    }

    // PLAYER STATISTICS

    private void createStatisticsTable()
            throws SQLException {

        db.execute("""
                CREATE TABLE IF NOT EXISTS player_statistics (

                    player_id TEXT PRIMARY KEY NOT NULL,

                    games_played INTEGER NOT NULL DEFAULT 0
                        CHECK (games_played >= 0),

                    wins INTEGER NOT NULL DEFAULT 0
                        CHECK (wins >= 0),

                    losses INTEGER NOT NULL DEFAULT 0
                        CHECK (losses >= 0),

                    accusations_made INTEGER NOT NULL DEFAULT 0
                        CHECK (accusations_made >= 0),

                    correct_accusations INTEGER NOT NULL DEFAULT 0
                        CHECK (correct_accusations >= 0),

                    incorrect_accusations INTEGER NOT NULL DEFAULT 0
                        CHECK (incorrect_accusations >= 0),

                    current_win_streak INTEGER NOT NULL DEFAULT 0
                        CHECK (current_win_streak >= 0),

                    best_win_streak INTEGER NOT NULL DEFAULT 0
                        CHECK (best_win_streak >= 0),

                    total_game_duration_millis INTEGER
                        NOT NULL DEFAULT 0
                        CHECK (total_game_duration_millis >= 0),

                    total_points INTEGER NOT NULL DEFAULT 0
                        CHECK (total_points >= 0),

                    CHECK (wins + losses <= games_played),

                    CHECK (
                        correct_accusations
                        + incorrect_accusations
                        <= accusations_made
                    ),

                    CHECK (
                        current_win_streak <= best_win_streak
                    ),

                    CHECK (best_win_streak <= wins),

                    FOREIGN KEY (player_id)
                        REFERENCES players(player_id)
                        ON DELETE CASCADE
                )
                """);
    }

    // ACHIEVEMENT DEFINITIONS

    private void createAchievementsTable()
            throws SQLException {

        db.execute("""
                CREATE TABLE IF NOT EXISTS achievements (

                    achievement_code TEXT PRIMARY KEY NOT NULL,

                    name TEXT NOT NULL,

                    description TEXT NOT NULL,

                    hidden INTEGER NOT NULL DEFAULT 0
                        CHECK (hidden IN (0, 1)),

                    bronze_requirement INTEGER
                        CHECK (bronze_requirement > 0),

                    silver_requirement INTEGER
                        CHECK (silver_requirement > 0),

                    gold_requirement INTEGER
                        CHECK (gold_requirement > 0),

                    platinum_requirement INTEGER
                        CHECK (platinum_requirement > 0),

                    icon_path TEXT

                )
                """);
    }

    // PLAYER ACHIEVEMENTS

    private void createPlayerAchievementsTable()
            throws SQLException {

        db.execute("""
                CREATE TABLE IF NOT EXISTS player_achievements (

                    player_id TEXT NOT NULL,

                    achievement_code TEXT NOT NULL,

                    progress INTEGER NOT NULL DEFAULT 0
                        CHECK (progress >= 0),

                    level TEXT NOT NULL DEFAULT 'LOCKED'
                        CHECK (
                            level IN (
                                'LOCKED',
                                'BRONZE',
                                'SILVER',
                                'GOLD',
                                'PLATINUM'
                            )
                        ),

                    unlocked_at INTEGER,

                    updated_at INTEGER NOT NULL,

                    PRIMARY KEY (
                        player_id,
                        achievement_code
                    ),

                    FOREIGN KEY (player_id)
                        REFERENCES players(player_id)
                        ON DELETE CASCADE,

                    FOREIGN KEY (achievement_code)
                        REFERENCES achievements(achievement_code)
                        ON DELETE CASCADE

                )
                """);
    }

    // MATCH HISTORY

    private void createMatchesTable()
            throws SQLException {

        db.execute("""
                CREATE TABLE IF NOT EXISTS matches (

                    match_id TEXT PRIMARY KEY NOT NULL,

                    host_id TEXT,

                    winner_id TEXT,

                    started_at INTEGER NOT NULL,

                    finished_at INTEGER,

                    duration_millis INTEGER NOT NULL DEFAULT 0
                        CHECK (duration_millis >= 0),

                    status TEXT NOT NULL DEFAULT 'IN_PROGRESS'
                        CHECK (
                            status IN (
                                'IN_PROGRESS',
                                'COMPLETED',
                                'CANCELLED'
                            )
                        ),

                    solution_actor TEXT,

                    solution_weapon TEXT,

                    solution_room TEXT,

                    CHECK (
                        finished_at IS NULL
                        OR finished_at >= started_at
                    ),

                    FOREIGN KEY (host_id)
                        REFERENCES players(player_id)
                        ON DELETE SET NULL,

                    FOREIGN KEY (winner_id)
                        REFERENCES players(player_id)
                        ON DELETE SET NULL

                )
                """);
    }

    // INDIVIDUAL MATCH RESULTS

    private void createMatchPlayersTable()
            throws SQLException {

        db.execute("""
                CREATE TABLE IF NOT EXISTS match_players (

                    match_id TEXT NOT NULL,

                    player_id TEXT NOT NULL,

                    character_name TEXT,

                    result TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK (
                            result IN (
                                'PENDING',
                                'WIN',
                                'LOSS',
                                'DRAW',
                                'DISCONNECTED'
                            )
                        ),

                    turns_played INTEGER NOT NULL DEFAULT 0
                        CHECK (turns_played >= 0),

                    dice_rolls INTEGER NOT NULL DEFAULT 0
                        CHECK (dice_rolls >= 0),

                    moves_made INTEGER NOT NULL DEFAULT 0
                        CHECK (moves_made >= 0),

                    suggestions_made INTEGER NOT NULL DEFAULT 0
                        CHECK (suggestions_made >= 0),

                    correct_accusations INTEGER NOT NULL DEFAULT 0
                        CHECK (correct_accusations >= 0),

                    incorrect_accusations INTEGER NOT NULL DEFAULT 0
                        CHECK (incorrect_accusations >= 0),

                    points_earned INTEGER NOT NULL DEFAULT 0
                        CHECK (points_earned >= 0),

                    PRIMARY KEY (match_id, player_id),

                    FOREIGN KEY (match_id)
                        REFERENCES matches(match_id)
                        ON DELETE CASCADE,

                    FOREIGN KEY (player_id)
                        REFERENCES players(player_id)
                        ON DELETE CASCADE

                )
                """);
    }

    // PLAYER SETTINGS

    private void createSettingsTable()
            throws SQLException {

        db.execute("""
                CREATE TABLE IF NOT EXISTS player_settings (

                    player_id TEXT NOT NULL,

                    setting_key TEXT NOT NULL,

                    setting_value TEXT NOT NULL,

                    updated_at INTEGER NOT NULL,

                    PRIMARY KEY (
                        player_id,
                        setting_key
                    ),

                    FOREIGN KEY (player_id)
                        REFERENCES players(player_id)
                        ON DELETE CASCADE

                )
                """);
    }

    // INDEXES

    private void createIndexes()
            throws SQLException {

        db.execute("""
                CREATE INDEX IF NOT EXISTS idx_players_name
                ON players(display_name)
                """);

        db.execute("""
                CREATE INDEX IF NOT EXISTS idx_statistics_wins
                ON player_statistics(wins DESC)
                """);

        db.execute("""
                CREATE INDEX IF NOT EXISTS idx_matches_date
                ON matches(started_at DESC)
                """);

        db.execute("""
                CREATE INDEX IF NOT EXISTS idx_matches_winner
                ON matches(winner_id)
                """);

        db.execute("""
                CREATE INDEX IF NOT EXISTS idx_matches_host
                ON matches(host_id)
                """);

        db.execute("""
                CREATE INDEX IF NOT EXISTS idx_match_players_player
                ON match_players(player_id)
                """);

        db.execute("""
                CREATE INDEX IF NOT EXISTS idx_player_achievements_code
                ON player_achievements(achievement_code)
                """);

        db.execute("""
                CREATE INDEX IF NOT EXISTS idx_player_achievements_level
                ON player_achievements(level)
                """);

        db.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS idx_single_match_winner
                ON match_players(match_id)
                WHERE result = 'WIN'
                """);
    }

    // SCHEMA VERIFICATION

    public void verifySchema()
            throws SQLException {

        String[] requiredTables = {

                "schema_version",
                "players",
                "player_statistics",
                "achievements",
                "player_achievements",
                "matches",
                "match_players",
                "player_settings"
        };

        for (String table : requiredTables) {

            if (!db.tableExists(table)) {

                throw new SQLException(
                        "Missing database table: " + table
                );
            }
        }

        LOGGER.info(
                "All required database tables exist."
        );
    }
}
