package demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionTest1 {
	public static void main(String[] args) {
		String url = "jdbc:mysql://localhost:3306/sakila";
		String user = "root";
		String password = "Dlsu1234!";
		
		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			System.out.println("Successfully connected MySQL");
			conn.close();
		} catch (SQLException e) {
			System.out.println("Connection failed.");
			e.printStackTrace();
		}
	}
}
