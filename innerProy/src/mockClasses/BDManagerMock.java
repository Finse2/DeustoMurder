
package mockClasses;

import utils.JDBC.DBManager;
import java.sql.ResultSet;

public class BDManagerMock {

    public static void main(String[] args) {

        try (DBManager db = new DBManager("jdbc:rqlite:http://localhost:4001")) {

            db.execute("DROP TABLE IF EXISTS person");

            db.execute("""
                CREATE TABLE person (
                    age INTEGER,
                    name TEXT
                )
                """);

            db.executeUpdate("INSERT INTO person VALUES (15, 'Antal')");

            db.executeUpdate("INSERT INTO person VALUES (20, 'Luka')");

            db.executeUpdate("INSERT INTO person VALUES (30, 'Antonio')");

            db.executeUpdate("INSERT INTO person VALUES (19, 'Andrada')");

            db.executeUpdate("INSERT INTO person VALUES (19, 'David')");

            try (ResultSet rs = db.executeQuery("SELECT * FROM person")) {

                while (rs.next()) {
                    String name = rs.getString("name");
                    int age = rs.getInt("age");

                    System.out.println(name + " - " + age);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
