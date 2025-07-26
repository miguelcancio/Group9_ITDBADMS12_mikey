package main;

import java.util.List;

public class AdminBackendTest {
    public static void main(String[] args) {
        AdminService adminService = new AdminService();
        int adminUserId = 1; // Use a valid admin user ID from your DB

        System.out.println("=== Admin Backend Test ===");

        // Test: Add Book
        System.out.println("\nAdding a new book...");
        boolean addBookResult = adminService.addBook(adminUserId, "Test Book", "Test Genre", 123.45, 10);
        System.out.println("Add book result: " + addBookResult);

        // Test: Update Book
        System.out.println("\nUpdating the book...");
        // You may need to adjust the book ID based on your DB
        boolean updateBookResult = adminService.updateBook(adminUserId, 1, "Updated Book", "Updated Genre", 150.00, 20);
        System.out.println("Update book result: " + updateBookResult);

        // Test: Delete Book
        System.out.println("\nDeleting the book...");
        // You may need to adjust the book ID based on your DB
        boolean deleteBookResult = adminService.deleteBook(adminUserId, 1);
        System.out.println("Delete book result: " + deleteBookResult);

        // Test: Update User Role
        System.out.println("\nUpdating user role...");
        // You may need to adjust the user ID based on your DB
        boolean updateUserRoleResult = adminService.updateUserRole(adminUserId, 2, "Staff");
        System.out.println("Update user role result: " + updateUserRoleResult);

        // Test: Get All Orders
        System.out.println("\nRetrieving all orders...");
        List<AdminService.OrderInfo> orders = adminService.getAllOrders(adminUserId);
        for (AdminService.OrderInfo order : orders) {
            System.out.printf("OrderID: %d, UserID: %d, Date: %s, Amount: %.2f, Currency: %s, Status: %s\n",
                order.orderId, order.userId, order.orderDate, order.totalAmount, order.currencyCode, order.status);
        }

        // Test: Get All Transaction Logs
        System.out.println("\nRetrieving all transaction logs...");
        List<AdminService.TransactionLog> logs = adminService.getAllTransactionLogs(adminUserId);
        for (AdminService.TransactionLog log : logs) {
            System.out.printf("TransactionID: %d, OrderID: %d, Method: %s, Status: %s, Amount: %.2f, Timestamp: %s\n",
                log.transactionId, log.orderId, log.paymentMethod, log.paymentStatus, log.amount, log.timestamp);
        }

        // Test: Update Exchange Rate
        System.out.println("\nUpdating exchange rate for USD...");
        boolean updateRateResult = adminService.updateExchangeRate(adminUserId, "USD", 56.00);
        System.out.println("Update exchange rate result: " + updateRateResult);

        System.out.println("\n=== Admin Backend Test Complete ===");
    }
} 