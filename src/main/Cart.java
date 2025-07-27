package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Cart extends JFrame {
    private DefaultTableModel tableModel;
    private ArrayList<CartItem> cartItems = new ArrayList<>();
    private double total = 0.0;
    private int selectedCurrencyId = 1; // default: PHP
    private CustomerCatalog catalog; // Reference to refresh book list

    // Constructor accepting CustomerCatalog reference
    public Cart(CustomerCatalog catalog) {
        this.catalog = catalog;

        setTitle("🛒 Your Cart - BookMart");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"Title", "Qty", "Price", "Subtotal"}, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadCart();

        JButton orderBtn = new JButton("✅ Place Order");
        orderBtn.addActionListener(e -> placeOrder());
        add(orderBtn, BorderLayout.SOUTH);
    }

    // Optional: default constructor (if needed elsewhere)
    public Cart() {
        this(null);
    }

    private void loadCart() {
        cartItems.clear();
        total = 0.0;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT c.book_id, b.title, c.quantity, b.price, (c.quantity * b.price) AS subtotal
                FROM cart_items c
                JOIN books b ON c.book_id = b.book_id
                WHERE c.user_id = ?
            """);
            stmt.setInt(1, LoginScreen.loggedInUserId);
            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0);
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double subtotal = rs.getDouble("subtotal");
                cartItems.add(new CartItem(bookId, qty, price));
                total += subtotal;

                tableModel.addRow(new Object[]{title, qty, price, subtotal});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void placeOrder() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "🛑 Cart is empty.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Get currency_id of PHP
            PreparedStatement currencyStmt = conn.prepareStatement("SELECT currency_id FROM currencies WHERE currency_code = 'PHP'");
            ResultSet cr = currencyStmt.executeQuery();
            if (cr.next()) selectedCurrencyId = cr.getInt("currency_id");

            // Insert into orders
            PreparedStatement orderStmt = conn.prepareStatement(
                "INSERT INTO orders (user_id, total_amount, currency_id, status) VALUES (?, ?, ?, 'Pending')",
                Statement.RETURN_GENERATED_KEYS
            );
            orderStmt.setInt(1, LoginScreen.loggedInUserId);
            orderStmt.setDouble(2, total);
            orderStmt.setInt(3, selectedCurrencyId);
            orderStmt.executeUpdate();

            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = -1;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

            // Insert into order_items
            PreparedStatement itemStmt = conn.prepareStatement(
                "INSERT INTO order_items (order_id, book_id, quantity, price_each) VALUES (?, ?, ?, ?)"
            );
            for (CartItem item : cartItems) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.bookId);
                itemStmt.setInt(3, item.quantity);
                itemStmt.setDouble(4, item.price);
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            // Deduct stock safely
            PreparedStatement updateStockStmt = conn.prepareStatement(
                "UPDATE books SET stock_quantity = stock_quantity - ? WHERE book_id = ? AND stock_quantity >= ?"
            );
            for (CartItem item : cartItems) {
                updateStockStmt.setInt(1, item.quantity);
                updateStockStmt.setInt(2, item.bookId);
                updateStockStmt.setInt(3, item.quantity);
                updateStockStmt.addBatch();
            }
            int[] updateCounts = updateStockStmt.executeBatch();
            for (int count : updateCounts) {
                if (count == 0) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "❌ Failed to update stock. Possibly insufficient stock.");
                    return;
                }
            }

            // Clear cart
            PreparedStatement clearCartStmt = conn.prepareStatement("DELETE FROM cart_items WHERE user_id = ?");
            clearCartStmt.setInt(1, LoginScreen.loggedInUserId);
            clearCartStmt.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "✅ Order placed!");

            if (catalog != null) {
                catalog.refreshBooks(); // 🔁 Update CustomerCatalog display
            }

            this.dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to place order.");
        }
    }

    // Helper class
    private static class CartItem {
        int bookId;
        int quantity;
        double price;

        public CartItem(int bookId, int quantity, double price) {
            this.bookId = bookId;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
