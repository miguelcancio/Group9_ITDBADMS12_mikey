package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookmartdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Dlsu1234!";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
