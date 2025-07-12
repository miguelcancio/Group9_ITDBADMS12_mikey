package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerCatalog extends JFrame {
    private JTable bookTable;
    private DefaultTableModel tableModel;

    public CustomerCatalog() {
        setTitle("📚 BookMart - Browse Books");
        setSize(600,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID","Title","Genre","Price","Stock"},0);
        bookTable = new JTable(tableModel);
        loadBooks();

        JButton cartBtn = new JButton("🛒 View Cart / Place Order");
        cartBtn.addActionListener(e -> new Cart().setVisible(true));

        add(new JScrollPane(bookTable), BorderLayout.CENTER);
        add(cartBtn, BorderLayout.SOUTH);
    }

    private void loadBooks() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT book_id, title, genre, price, stock_quantity FROM books");
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
