
package mockClasses;

import utils.JDBC.DBManager;
import java.sql.ResultSet;

public class BDManagerMock {

    public static void main(String[] args) throws Exception {

        try (DBManager db = new DBManager(
                "jdbc:rqlite:http://localhost:4001")) {

            System.out.println("Connected: " + db.isConnected());

            db.execute("""
                    CREATE TABLE IF NOT EXISTS jdbc_test_person (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        age INTEGER NOT NULL
                    )
                    """);

            if (!db.tableExists("jdbc_test_person")) {
                throw new AssertionError("Table creation failed");
            }
            // TEST: 64-bit integer support

            long timestamp = System.currentTimeMillis();

            try (ResultSet rs = db.executeQuery(
                    "SELECT ? AS timestamp_value",
                    timestamp
            )) {

                if (!rs.next()) {

                    throw new AssertionError(
                            "Missing timestamp result."
                    );
                }

                if (rs.getLong("timestamp_value") != timestamp) {

                    throw new AssertionError(
                            "64-bit integer conversion failed."
                    );
                }

                System.out.println(
                        "[PASS] 64-bit integer support"
                );
            }

            db.executeUpdate("DELETE FROM jdbc_test_person");

            db.executeUpdate(
                    "INSERT INTO jdbc_test_person VALUES (?, ?, ?)",
                    1, "Antal", 15
            );

            db.executeUpdate(
                    "INSERT INTO jdbc_test_person VALUES (?, ?, ?)",
                    2, "Luka", 20
            );

            db.executeUpdate(
                    "INSERT INTO jdbc_test_person VALUES (?, ?, ?)",
                    3, "O'Connor", 30
            );

            db.executeUpdate(
                    "UPDATE jdbc_test_person SET age = ? WHERE id = ?",
                    21, 2
            );

            try (ResultSet rs = db.executeQuery(
                    "SELECT age FROM jdbc_test_person WHERE id = ?",
                    2
            )) {

                if (!rs.next() || rs.getInt("age") != 21) {
                    throw new AssertionError("Update failed");
                }
            }

            try (ResultSet rs = db.executeQuery(
                    "SELECT name FROM jdbc_test_person WHERE id = ?",
                    3
            )) {

                if (!rs.next() ||
                        !"O'Connor".equals(rs.getString("name"))) {
                    throw new AssertionError("Parameter binding failed");
                }
            }

            db.printQuery(
                    "SELECT * FROM jdbc_test_person ORDER BY id"
            );

            db.executeUpdate(
                    "DELETE FROM jdbc_test_person WHERE id = ?",
                    1
            );

            try (ResultSet rs = db.executeQuery(
                    "SELECT COUNT(*) AS total FROM jdbc_test_person"
            )) {

                if (!rs.next() || rs.getInt("total") != 2) {
                    throw new AssertionError("Delete failed");
                }

                System.out.println("Remaining records: 2");
            }

            System.out.println("All assertions passed.");
        }
    }
}
