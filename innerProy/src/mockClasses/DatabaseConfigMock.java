
package mockClasses;

import utils.JDBC.DBManager;
import utils.JDBC.DatabaseConfig;
import utils.JDBC.JDBCDependencyHandler;

import java.sql.ResultSet;

public class DatabaseConfigMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== DATABASE CONFIGURATION TEST ==="
        );

        DatabaseConfig config =
                DatabaseConfig.getInstance();

        // Singleton verification

        if (config != DatabaseConfig.getInstance()) {

            throw new AssertionError(
                    "Singleton verification failed."
            );
        }

        System.out.println(
                "Singleton verification passed."
        );

        // Configuration inspection

        System.out.println(
                "Database type: "
                        + config.getDatabaseType()
        );

        System.out.println(
                "JDBC URL: "
                        + config.getJdbcUrl()
        );

        System.out.println(
                "HTTP port: "
                        + config.getHttpPort()
        );

        System.out.println(
                "Raft port: "
                        + config.getRaftPort()
        );

        System.out.println(
                "Startup timeout: "
                        + config.getStartupTimeout()
        );

        System.out.println(
                "SQLite file: "
                        + config.getSQLiteFile()
        );

        System.out.println(
                "Rqlite directory: "
                        + config.getRqliteDirectory()
        );

        // Configuration consistency

        if (config.getHttpPort() !=
                JDBCDependencyHandler.getHttpPort()) {

            throw new AssertionError(
                    "Configuration port mismatch."
            );
        }

        System.out.println(
                "Configuration consistency test passed."
        );

        // DATABASE CONNECTION TEST

        try (DBManager db = new DBManager()) {

            if (!db.isConnected()) {

                throw new AssertionError(
                        "Database connection failed."
                );
            }

            if (db.isSQLite() != config.isSQLite()) {

                throw new AssertionError(
                        "Incorrect database backend."
                );
            }

            System.out.println(
                    "Database connection test passed."
            );

            try (ResultSet rs = db.executeQuery(
                    "SELECT 1 AS result"
            )) {

                if (!rs.next()
                        || rs.getInt("result") != 1) {

                    throw new AssertionError(
                            "SQL execution failed."
                    );
                }
            }

            System.out.println(
                    "SQL execution test passed."
            );

        } finally {

            if (config.isManagedRqlite()) {
                JDBCDependencyHandler.stopServer();
            }
        }

        System.out.println("All assertions passed.");
    }
}
