
package utils.JDBC.migrations;

import utils.JDBC.DBManager;
import utils.JDBC.DatabaseMigrationManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Logger;

public final class MigrationV2
        implements DatabaseMigrationManager.Migration {

    private static final Logger LOGGER =
            Logger.getLogger(
                    MigrationV2.class.getName()
            );

    // MIGRATION INFORMATION

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String description() {

        return "Update achievement hierarchy to "
                + "Bronze, Gold, Diamond and Champ";
    }

    // APPLY MIGRATION

    @Override
    public void apply(DBManager db)
            throws SQLException {

        LOGGER.info(
                "Updating achievement schema..."
        );

        // Ensure that no existing achievement
        // definitions or player progress will be lost.

        verifySafeMigration(db);

        migrateAchievementDefinitions(db);

        migratePlayerAchievements(db);

        recreateIndexes(db);

        LOGGER.info(
                "Achievement schema updated."
        );
    }

    // SAFETY CHECK

    private void verifySafeMigration(
            DBManager db
    ) throws SQLException {

        if (!db.tableExists("achievements")) {

            throw new SQLException(
                    "Missing achievements table."
            );
        }

        if (countRows(db, "achievements") > 0) {

            throw new SQLException(
                    "Migration requires empty achievement "
                            + "definitions. Existing definitions "
                            + "must be migrated explicitly."
            );
        }

        // The table may be missing if a previous
        // migration was interrupted after DROP TABLE.

        if (db.tableExists("player_achievements")
                && countRows(
                db,
                "player_achievements"
        ) > 0) {

            throw new SQLException(
                    "Existing player achievement progress "
                            + "detected. Migration aborted."
            );
        }
    }

    // UPDATE ACHIEVEMENT REQUIREMENTS

    private void migrateAchievementDefinitions(
            DBManager db
    ) throws SQLException {

        LOGGER.info(
                "Updating achievement requirement columns..."
        );

        // Gold must be renamed first because the
        // old silver column will become gold.

        renameColumnIfRequired(
                db,
                "achievements",
                "gold_requirement",
                "diamond_requirement"
        );

        renameColumnIfRequired(
                db,
                "achievements",
                "silver_requirement",
                "gold_requirement"
        );

        renameColumnIfRequired(
                db,
                "achievements",
                "platinum_requirement",
                "champ_requirement"
        );
    }

    private void renameColumnIfRequired(
            DBManager db,
            String table,
            String oldColumn,
            String newColumn
    ) throws SQLException {

        boolean oldExists =
                DatabaseMigrationManager.columnExists(
                        db, table, oldColumn
                );

        boolean newExists =
                DatabaseMigrationManager.columnExists(
                        db, table, newColumn
                );

        // Already migrated.

        if (newExists) {
            return;
        }

        if (!oldExists) {

            throw new SQLException(
                    "Neither " + oldColumn
                            + " nor " + newColumn
                            + " exists in " + table
            );
        }

        db.execute(
                "ALTER TABLE " + table
                        + " RENAME COLUMN "
                        + oldColumn
                        + " TO "
                        + newColumn
        );

        LOGGER.info(
                "Renamed " + oldColumn
                        + " to " + newColumn
        );
    }

    // UPDATE PLAYER ACHIEVEMENT LEVELS

    private void migratePlayerAchievements(
            DBManager db
    ) throws SQLException {

        LOGGER.info(
                "Updating player achievement constraints..."
        );

        // SQLite does not directly support modifying
        // an existing CHECK constraint.

        if (db.tableExists("player_achievements")
                && !hasCorrectLevelConstraint(db)) {

            db.execute("""
                    DROP TABLE player_achievements
                    """);
        }

        db.execute("""
                CREATE TABLE IF NOT EXISTS
                player_achievements (

                    player_id TEXT NOT NULL,

                    achievement_code TEXT NOT NULL,

                    progress INTEGER NOT NULL DEFAULT 0
                        CHECK (progress >= 0),

                    level TEXT NOT NULL DEFAULT 'LOCKED'
                        CHECK (
                            level IN (
                                'LOCKED',
                                'BRONZE',
                                'GOLD',
                                'DIAMOND',
                                'CHAMP'
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
                        REFERENCES achievements(
                            achievement_code
                        )
                        ON DELETE CASCADE
                )
                """);
    }

    // RECREATE INDEXES

    private void recreateIndexes(
            DBManager db
    ) throws SQLException {

        db.execute("""
                CREATE INDEX IF NOT EXISTS
                idx_player_achievements_code

                ON player_achievements(
                    achievement_code
                )
                """);

        db.execute("""
                CREATE INDEX IF NOT EXISTS
                idx_player_achievements_level

                ON player_achievements(level)
                """);
    }

    // VERIFY MIGRATION

    @Override
    public void verify(DBManager db)
            throws SQLException {

        LOGGER.info(
                "Verifying achievement migration..."
        );

        String[] requiredColumns = {

                "bronze_requirement",
                "gold_requirement",
                "diamond_requirement",
                "champ_requirement"
        };

        for (String column : requiredColumns) {

            if (!DatabaseMigrationManager.columnExists(
                    db,
                    "achievements",
                    column
            )) {

                throw new SQLException(
                        "Missing achievement column: "
                                + column
                );
            }
        }

        String[] obsoleteColumns = {

                "silver_requirement",
                "platinum_requirement"
        };

        for (String column : obsoleteColumns) {

            if (DatabaseMigrationManager.columnExists(
                    db,
                    "achievements",
                    column
            )) {

                throw new SQLException(
                        "Obsolete achievement column: "
                                + column
                );
            }
        }

        if (!db.tableExists("player_achievements")) {

            throw new SQLException(
                    "Missing player_achievements table."
            );
        }

        if (!hasCorrectLevelConstraint(db)) {

            throw new SQLException(
                    "Incorrect achievement level constraint."
            );
        }

        LOGGER.info(
                "Achievement migration verified successfully."
        );
    }

    // INTERNAL UTILITIES

    private boolean hasCorrectLevelConstraint(
            DBManager db
    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT sql
                FROM sqlite_master
                WHERE type = 'table'
                  AND name = 'player_achievements'
                """)) {

            if (!rs.next()) {
                return false;
            }

            String sql = rs.getString("sql");

            if (sql == null) {
                return false;
            }

            String normalized = sql
                    .toUpperCase(Locale.ROOT)
                    .replaceAll("\\s+", "");

            return normalized.contains(
                    "CHECK(LEVELIN("
                            + "'LOCKED',"
                            + "'BRONZE',"
                            + "'GOLD',"
                            + "'DIAMOND',"
                            + "'CHAMP'))"
            );
        }
    }

    private long countRows(
            DBManager db,
            String table
    ) throws SQLException {

        if (!table.equals("achievements")
                && !table.equals("player_achievements")) {

            throw new IllegalArgumentException(
                    "Unexpected table: " + table
            );
        }

        try (ResultSet rs = db.executeQuery(
                "SELECT COUNT(*) AS total FROM " + table
        )) {

            if (!rs.next()) {

                throw new SQLException(
                        "Could not count records in " + table
                );
            }

            return rs.getLong("total");
        }
    }
}
