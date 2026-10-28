
package ui.match.mockClasses;

import utils.JDBC.DBManager;

import java.sql.ResultSet;

public class BDManagerMock {

    public static void main(String[] args) {


        try (DBManager db = new DBManager("jdbc:rqlite:http://localhost:4001")) {

            db.executeQuery("DROP TABLE IF EXISTS person");
            db.executeQuery("CREATE TABLE person (age INTEGER, name TEXT)");
            db.executeQuery("INSERT INTO person VALUES (15, 'Antal')");
            db.executeQuery("INSERT INTO person VALUES(20, 'Luka'");
            db.executeQuery("INSERT INTO person VALUES(30, 'Antonio')");
            db.executeQuery("INSERT INTO person VALUES(19, 'Andrada')");
            db.executeQuery("INSERT INTO person VALUES(19, 'David')");
           ResultSet rs =  db.executeQuery("SELECT * FROM person");

            while (rs != null && rs.next()) {
                String name = rs.getString("name");
                int age = rs.getInt("age");

                System.out.println(name + " - " + age);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
