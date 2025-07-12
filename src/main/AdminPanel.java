package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminPanel extends JFrame {
    private DefaultTableModel tableModel;

    public AdminPanel() {
        setTitle("📚 Admin Dashboard - BookMart");
        setSize(700,400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID","Title","Genre","Price","Stock"},0);
        JTable table = new JTable(tableModel);
        loadBooks();

        JButton addBtn = new JButton("➕ Add Book");
        addBtn.addActionListener(e -> addBookDialog());

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(addBtn, BorderLayout.SOUTH);
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

    private void addBookDialog() {
        JTextField t=new JTextField(), g=new JTextField(),
                   p=new JTextField(), s=new JTextField();
        JPanel panel=new JPanel(new GridLayout(4,2));
        panel.add(new JLabel("Title:")); panel.add(t);
        panel.add(new JLabel("Genre:")); panel.add(g);
        panel.add(new JLabel("Price:")); panel.add(p);
        panel.add(new JLabel("Stock:")); panel.add(s);

        if(JOptionPane.showConfirmDialog(this,panel,"Add Book",
            JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
            addBook(t.getText(),g.getText(),
                    Double.parseDouble(p.getText()),Integer.parseInt(s.getText()));
            loadBooks();
        }
    }

    private void addBook(String title, String genre, double price, int stock) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO books (title, genre, price, stock_quantity) VALUES (?,?,?,?)");
            stmt.setString(1, title);
            stmt.setString(2, genre);
            stmt.setDouble(3, price);
            stmt.setInt(4, stock);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this,"✅ Book added.");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
