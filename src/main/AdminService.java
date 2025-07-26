package main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for admin operations using admin-secured stored procedures.
 */
public class AdminService {
    /**
     * Adds a new book to the catalog.
     * @param adminUserId Admin's user ID
     * @param title Book title
     * @param genre Book genre
     * @param price Book price
     * @param stock Stock quantity
     * @return true if successful, false otherwise
     */
    public boolean addBook(int adminUserId, String title, String genre, double price, int stock) {
        validateBookInput(title, genre, price, stock);
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL addBooks(?, ?, ?, ?)}");
            stmt.setString(1, sanitize(title));
            stmt.setString(2, sanitize(genre));
            stmt.setDouble(3, price);
            stmt.setInt(4, stock);
            stmt.executeUpdate();
            logAdminAction(conn, adminUserId, "ADD_BOOK", "Added book: " + title);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates book details using admin-secured stored procedure.
     * @param adminUserId Admin's user ID
     * @param bookId Book ID
     * @param title New title
     * @param genre New genre
     * @param price New price
     * @param stock New stock quantity
     * @return true if successful, false otherwise
     */
    public boolean updateBook(int adminUserId, int bookId, String title, String genre, double price, int stock) {
        validateBookInput(title, genre, price, stock);
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL updateBookDetails(?, ?, ?, ?, ?, ?)}");
            stmt.setInt(1, adminUserId);
            stmt.setInt(2, bookId);
            stmt.setString(3, sanitize(title));
            stmt.setString(4, sanitize(genre));
            stmt.setDouble(5, price);
            stmt.setInt(6, stock);
            stmt.executeUpdate();
            logAdminAction(conn, adminUserId, "UPDATE_BOOK", "Updated book ID: " + bookId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a book from the catalog.
     * @param adminUserId Admin's user ID
     * @param bookId Book ID
     * @return true if successful, false otherwise
     */
    public boolean deleteBook(int adminUserId, int bookId) {
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL removeBooks(?)}");
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            logAdminAction(conn, adminUserId, "DELETE_BOOK", "Deleted book ID: " + bookId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates a user's role using admin-secured stored procedure.
     * @param adminUserId Admin's user ID
     * @param userId User ID to update
     * @param newRole New role (Admin, Staff, Customer)
     * @return true if successful, false otherwise
     */
    public boolean updateUserRole(int adminUserId, int userId, String newRole) {
        validateRoleName(newRole);
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL updateUserRole(?, ?, ?)}");
            stmt.setInt(1, adminUserId);
            stmt.setInt(2, userId);
            stmt.setString(3, sanitize(newRole));
            stmt.executeUpdate();
            logAdminAction(conn, adminUserId, "UPDATE_USER_ROLE", "Changed user ID " + userId + " to role " + newRole);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all orders in the system (admin only).
     * @param adminUserId Admin's user ID
     * @return List of OrderInfo objects
     */
    public List<OrderInfo> getAllOrders(int adminUserId) {
        List<OrderInfo> orders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL getAllOrders(?)}");
            stmt.setInt(1, adminUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(new OrderInfo(
                    rs.getInt("order_id"),
                    rs.getInt("user_id"),
                    rs.getTimestamp("order_date"),
                    rs.getDouble("total_amount"),
                    rs.getString("currency_code"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Gets all transaction logs (admin only).
     * @param adminUserId Admin's user ID
     * @return List of TransactionLog objects
     */
    public List<TransactionLog> getAllTransactionLogs(int adminUserId) {
        List<TransactionLog> logs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL getAllTransactionLogs(?)}");
            stmt.setInt(1, adminUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(new TransactionLog(
                    rs.getInt("transaction_id"),
                    rs.getInt("order_id"),
                    rs.getString("payment_method"),
                    rs.getString("payment_status"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("timestambooksusersp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Updates a currency's exchange rate (admin only).
     * @param adminUserId Admin's user ID
     * @param currencyCode Currency code (e.g., USD)
     * @param newRate New exchange rate to PHP
     * @return true if successful, false otherwise
     */
    public boolean updateExchangeRate(int adminUserId, String currencyCode, double newRate) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be empty.");
        }
        if (newRate <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL updateExchangeRate(?, ?, ?)}");
            stmt.setInt(1, adminUserId);
            stmt.setString(2, sanitize(currencyCode));
            stmt.setDouble(3, newRate);
            stmt.executeUpdate();
            logAdminAction(conn, adminUserId, "UPDATE_EXCHANGE_RATE", "Updated " + currencyCode + " to rate " + newRate);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Logs an admin action to the admin_action_log table.
     * @param conn Active SQL connection
     * @param adminUserId Admin's user ID
     * @param action Action type
     * @param details Action details
     */
    private void logAdminAction(Connection conn, int adminUserId, String action, String details) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO admin_action_log (admin_user_id, action, details) VALUES (?, ?, ?)")
        ) {
            stmt.setInt(1, adminUserId);
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Logging failure should not break main operation
            e.printStackTrace();
        }
    }

    // Data classes for return values
    public static class OrderInfo {
        public final int orderId;
        public final int userId;
        public final Timestamp orderDate;
        public final double totalAmount;
        public final String currencyCode;
        public final String status;
        public OrderInfo(int orderId, int userId, Timestamp orderDate, double totalAmount, String currencyCode, String status) {
            this.orderId = orderId;
            this.userId = userId;
            this.orderDate = orderDate;
            this.totalAmount = totalAmount;
            this.currencyCode = currencyCode;
            this.status = status;
        }
    }

    public static class TransactionLog {
        public final int transactionId;
        public final int orderId;
        public final String paymentMethod;
        public final String paymentStatus;
        public final double amount;
        public final Timestamp timestamp;
        public TransactionLog(int transactionId, int orderId, String paymentMethod, String paymentStatus, double amount, Timestamp timestamp) {
            this.transactionId = transactionId;
            this.orderId = orderId;
            this.paymentMethod = paymentMethod;
            this.paymentStatus = paymentStatus;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }

    // --- Input Validation Helpers ---
    private void validateBookInput(String title, String genre, double price, int stock) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty.");
        }
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Book genre cannot be empty.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Book price must be positive.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
    }

    private void validateRoleName(String role) {
        if (role == null) throw new IllegalArgumentException("Role cannot be null.");
        String r = role.trim().toLowerCase();
        if (!(r.equals("admin") || r.equals("staff") || r.equals("customer"))) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    private String sanitize(String input) {
        if (input == null) return "";
        // Basic sanitization: trim and remove dangerous characters
        return input.trim().replaceAll("[<>\"'%;]", "");
    }
} 