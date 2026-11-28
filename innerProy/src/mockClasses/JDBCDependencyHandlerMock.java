
package mockClasses;

import utils.JDBC.DBManager;
import utils.JDBC.JDBCDependencyHandler;

import java.sql.ResultSet;

public class JDBCDependencyHandlerMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println("=== RQLITE DEPENDENCY TEST ===");

        try {

            JDBCDependencyHandler.startServer();

            if (!JDBCDependencyHandler.isRunning()) {

                throw new AssertionError(
                        "Server failed to start."
                );
            }

            boolean managed =
                    JDBCDependencyHandler.isManagedServer();

            System.out.println(
                    "Server URL: "
                            + JDBCDependencyHandler.getServerUrl()
            );

            System.out.println(
                    "Managed server: " + managed
            );

            System.out.println(
                    "Process ID: "
                            + JDBCDependencyHandler.getProcessId()
            );

            System.out.println(
                    "Data directory: "
                            + JDBCDependencyHandler.getDataDirectory()
            );

            System.out.println(
                    "Log file: "
                            + JDBCDependencyHandler.getLogFile()
            );

            try (DBManager db = new DBManager(
                    JDBCDependencyHandler.getJdbcUrl()
            )) {

                if (!db.isConnected()) {

                    throw new AssertionError(
                            "Database connection failed."
                    );
                }

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
            }

            // Closing a DBManager should not stop the server.

            if (!JDBCDependencyHandler.isRunning()) {

                throw new AssertionError(
                        "Closing DBManager stopped the server."
                );
            }

            System.out.println(
                    "Connection lifecycle test passed."
            );

            JDBCDependencyHandler.stopServer();

            if (managed
                    && JDBCDependencyHandler.isRunning()) {

                throw new AssertionError(
                        "Managed server failed to stop."
                );
            }

            if (!managed
                    && !JDBCDependencyHandler.isRunning()) {

                throw new AssertionError(
                        "External server was incorrectly stopped."
                );
            }

            System.out.println(
                    "Process ownership test passed."
            );

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            JDBCDependencyHandler.stopServer();
        }
    }
}
