package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Cart extends JFrame {
    private DefaultTableModel tableModel;

    public Cart() {
        setTitle("🛒 Your Cart - BookMart");
        setSize(600,400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"Title","Qty","Price","Subtotal"},0);
        JTable table = new JTable(tableModel);
        loadCart();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton orderBtn = new JButton("✅ Place Order");
        orderBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Order placed! (SP later)"));
        add(orderBtn, BorderLayout.SOUTH);
    }

    private void loadCart() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT b.title, c.quantity, b.price, (c.quantity * b.price) AS subtotal
                FROM cart_items c JOIN books b ON c.book_id = b.book_id
                WHERE c.user_id = ?
            """);
            stmt.setInt(1, LoginScreen.loggedInUserId);
            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0);
            while(rs.next())
                tableModel.addRow(new Object[]{
                    rs.getString(1), rs.getInt(2),
                    rs.getDouble(3), rs.getDouble(4)});
        } catch(SQLException e){ e.printStackTrace(); }
    }
}
