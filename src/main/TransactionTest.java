package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Simple test class to verify transaction control in AdminService
 */
public class TransactionTest {
    
    public static void main(String[] args) {
        AdminService adminService = new AdminService();
        
        // Test updateUserRole with transaction control
        System.out.println("=== Testing updateUserRole Transaction Control ===");
        boolean roleUpdateResult = adminService.updateUserRole(1, 2, "Staff");
        System.out.println("Role update result: " + roleUpdateResult);
        
        // Test deleteUser with transaction control
        System.out.println("\n=== Testing deleteUser Transaction Control ===");
        boolean deleteUserResult = adminService.deleteUser(1, 3);
        System.out.println("Delete user result: " + deleteUserResult);
        
        // Verify admin action logs were created
        System.out.println("\n=== Checking Admin Action Logs ===");
        checkAdminActionLogs();
    }
    
    private static void checkAdminActionLogs() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT admin_user_id, action, details, timestamp FROM admin_action_log " +
                "WHERE action IN ('UPDATE_USER_ROLE', 'DELETE_USER') " +
                "ORDER BY timestamp DESC LIMIT 5"
            );
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                System.out.println("Admin Action: " + rs.getString("action") + 
                                 " - Details: " + rs.getString("details") + 
                                 " - Time: " + rs.getTimestamp("timestamp"));
            }
        } catch (Exception e) {
            System.err.println("Error checking admin logs: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 