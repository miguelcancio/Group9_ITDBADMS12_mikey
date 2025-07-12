package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminPanel extends JFrame {
    private JTable bookTable;
    private DefaultTableModel tableModel;

    public AdminPanel() {
        setTitle("📚 BookMart Admin Panel");
        setSize(600,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID","Title","Genre","Price","Stock"},0);
        bookTable = new JTable(tableModel);
        loadBooks();

        JButton addBtn = new JButton("Add Book");
        addBtn.addActionListener(e -> showAddDialog());

        add(new JScrollPane(bookTable), BorderLayout.CENTER);
        add(addBtn, BorderLayout.SOUTH);
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

    private void showAddDialog() {
        JTextField title = new JTextField(), genre = new JTextField();
        JTextField price = new JTextField(), stock = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4,2));
        panel.add(new JLabel("Title:")); panel.add(title);
        panel.add(new JLabel("Genre:")); panel.add(genre);
        panel.add(new JLabel("Price:")); panel.add(price);
        panel.add(new JLabel("Stock:")); panel.add(stock);

        if (JOptionPane.showConfirmDialog(this, panel, "Add Book",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            addBook(title.getText(), genre.getText(),
                    Double.parseDouble(price.getText()), Integer.parseInt(stock.getText()));
            loadBooks();
        }
    }

    private void addBook(String title, String genre, double price, int stock) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO books (title, genre, price, stock_quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, genre);
            stmt.setDouble(3, price);
            stmt.setInt(4, stock);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Book Added!");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
