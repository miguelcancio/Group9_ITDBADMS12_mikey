package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerCatalog extends JFrame {
    private DefaultTableModel tableModel;

    public CustomerCatalog() {
        setTitle("📚 Browse Books - BookMart");
        setSize(700,400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID","Title","Genre","Price","Stock"},0);
        JTable table = new JTable(tableModel);
        loadBooks();

        JButton cartBtn = new JButton("🛒 View Cart / Place Order");
        cartBtn.addActionListener(e -> new Cart().setVisible(true));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(cartBtn, BorderLayout.SOUTH);
    }

    private void loadBooks() {
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT book_id, title, genre, price, stock_quantity FROM books");
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getDouble(4), rs.getInt(5)
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
