package utils.JDBC.migrations;

import utils.JDBC.DBManager;
import utils.JDBC.DatabaseMigrationManager;

import java.sql.SQLException;

public final class MigrationV4
        implements DatabaseMigrationManager.Migration {

    @Override
    public int version() {
        return 4;
    }

    @Override
    public String description() {
        return "Add persistent legacy statistics import marker";
    }

    @Override
    public void apply(DBManager db) throws SQLException {

        if (!DatabaseMigrationManager.columnExists(
                db,
                "player_statistics",
                "legacy_imported"
        )) {

            db.execute("""
                    ALTER TABLE player_statistics

                    ADD COLUMN legacy_imported INTEGER

                    NOT NULL DEFAULT 0

                    CHECK (legacy_imported IN (0, 1))
                    """);
        }
    }

    @Override
    public void verify(DBManager db) throws SQLException {

        if (!DatabaseMigrationManager.columnExists(
                db,
                "player_statistics",
                "legacy_imported"
        )) {

            throw new SQLException(
                    "Missing legacy_imported column."
            );
        }
    }
}