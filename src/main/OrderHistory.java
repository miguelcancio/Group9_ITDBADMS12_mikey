package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OrderHistory extends JFrame {
    private JTable orderTable;
    private DefaultTableModel orderModel;

    public OrderHistory() {
        setTitle("📜 Order History - BookMart");
        setSize(700, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        orderModel = new DefaultTableModel(new String[]{
            "Order ID", "Date", "Total Price", "Status", "Currency", "Book Titles"
        }, 0);
        orderTable = new JTable(orderModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);

        JButton viewItemsBtn = new JButton("📦 View Order Items");

        viewItemsBtn.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow >= 0) {
                int orderId = (int) orderModel.getValueAt(selectedRow, 0);
                showOrderItems(orderId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an order to view items.");
            }
        });

        add(scrollPane, BorderLayout.CENTER);
        add(viewItemsBtn, BorderLayout.SOUTH);

        loadOrders();
    }

    private void loadOrders() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT o.order_id, o.order_date, o.total_amount, o.status, c.currency_code,
                       GROUP_CONCAT(DISTINCT b.title ORDER BY b.title SEPARATOR ', ') AS book_titles
                FROM orders o
                JOIN currencies c ON o.currency_id = c.currency_id
                LEFT JOIN order_items oi ON o.order_id = oi.order_id
                LEFT JOIN books b ON oi.book_id = b.book_id
                WHERE o.user_id = ?
                GROUP BY o.order_id, o.order_date, o.total_amount, o.status, c.currency_code
                ORDER BY o.order_date DESC
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, LoginScreen.loggedInUserId);
            ResultSet rs = stmt.executeQuery();

            orderModel.setRowCount(0);
            while (rs.next()) {
                orderModel.addRow(new Object[]{
                    rs.getInt("order_id"),
                    rs.getTimestamp("order_date"),
                    rs.getDouble("total_amount"),
                    rs.getString("status"),
                    rs.getString("currency_code"),
                    rs.getString("book_titles")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showOrderItems(int orderId) {
        StringBuilder itemsText = new StringBuilder("<html><b>Order ID:</b> " + orderId + "<br><br>");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT b.title, oi.quantity, oi.price_each
                FROM order_items oi
                JOIN books b ON oi.book_id = b.book_id
                WHERE oi.order_id = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price_each");
                itemsText.append("- ").append(title)
                         .append(" × ").append(qty)
                         .append(" @ ").append(price)
                         .append("<br>");
            }

            itemsText.append("</html>");
            JOptionPane.showMessageDialog(this, new JLabel(itemsText.toString()), "Order Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
