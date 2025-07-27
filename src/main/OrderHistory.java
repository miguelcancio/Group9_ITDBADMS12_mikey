package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class OrderHistory extends JFrame {
    private JTable orderTable;
    private DefaultTableModel orderModel;

    public OrderHistory() {
        setTitle("📜 Order History - BookMart");
        setSize(800, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header panel (same as cart)
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0x003059));
        JLabel headerLabel = new JLabel("Your Order History");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Table setup
        orderModel = new DefaultTableModel(new String[]{
            "Order ID", "Date", "Total Price",  "Currency", "Book Titles"
        }, 0);
        orderTable = new JTable(orderModel);
        orderTable.setRowHeight(24);

        // Header styling
        JTableHeader tableHeader = orderTable.getTableHeader();
        tableHeader.setBackground(new Color(0x4a90e2));
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(scrollPane, BorderLayout.CENTER);

        // View button
        JButton viewItemsBtn = new JButton("📦 View Order Items");
        viewItemsBtn.setBackground(new Color(0x4a90e2));
        viewItemsBtn.setForeground(Color.WHITE);
        viewItemsBtn.setFocusPainted(false);
        viewItemsBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        viewItemsBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        viewItemsBtn.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow >= 0) {
                int orderId = (int) orderModel.getValueAt(selectedRow, 0);
                showOrderItems(orderId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an order to view items.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btnPanel.add(viewItemsBtn);
        add(btnPanel, BorderLayout.SOUTH);

        loadOrders();
    }

    private void loadOrders() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT o.order_id, o.order_date, o.total_amount, c.currency_code,
                       GROUP_CONCAT(DISTINCT b.title ORDER BY b.title SEPARATOR ', ') AS book_titles
                FROM orders o
                JOIN currencies c ON o.currency_id = c.currency_id
                LEFT JOIN order_items oi ON o.order_id = oi.order_id
                LEFT JOIN books b ON oi.book_id = b.book_id
                WHERE o.user_id = ?
                GROUP BY o.order_id, o.order_date, o.total_amount, c.currency_code
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
