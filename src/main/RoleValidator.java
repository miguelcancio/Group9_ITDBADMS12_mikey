package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class for role validation.
 */
public class RoleValidator {
    /**
     * Checks if the given user ID is an admin. Throws SecurityException if not.
     * @param userId User ID to check
     * @throws SecurityException if user is not an admin
     */
    public static void validateAdmin(int userId) throws SecurityException {
        if (!isAdmin(userId)) {
            throw new SecurityException("Access denied: User is not an admin.");
        }
    }

    /**
     * Returns true if the user is an admin, false otherwise.
     * @param userId User ID to check
     * @return true if admin, false otherwise
     */
    public static boolean isAdmin(int userId) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Admin".equalsIgnoreCase(rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 