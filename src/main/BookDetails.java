package main;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BookDetails extends JFrame {
    private int bookId;
    private String currency;

    public BookDetails(int bookId, String currency) {
        this.bookId = bookId;
        this.currency = currency;

        setTitle("📖 Book Details - BookMart");
        setSize(400, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0,1));
        add(panel);

        JLabel titleLabel = new JLabel();
        JLabel genreLabel = new JLabel();
        JLabel priceLabel = new JLabel();
        JLabel stockLabel = new JLabel();
        JTextField qtyField = new JTextField("1");
        JButton addToCartBtn = new JButton("Add to Cart");

        panel.add(titleLabel);
        panel.add(genreLabel);
        panel.add(priceLabel);
        panel.add(stockLabel);
        panel.add(new JLabel("Quantity:"));
        panel.add(qtyField);
        panel.add(addToCartBtn);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT b.title, b.genre, b.stock_quantity,
                ROUND(b.price * c.exchange_rate_to_php, 2) AS converted_price
                FROM books b
                JOIN currencies c ON c.currency_code = ?
                WHERE b.book_id = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currency);
            stmt.setInt(2, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                titleLabel.setText("Title: " + rs.getString("title"));
                genreLabel.setText("Genre: " + rs.getString("genre"));
                stockLabel.setText("Stock: " + rs.getInt("stock_quantity"));
                priceLabel.setText("Price ("+currency+"): " + rs.getDouble("converted_price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addToCartBtn.addActionListener(e -> {
            int qty = Integer.parseInt(qtyField.getText());
            // Here you'd insert into cart table or session variable
            JOptionPane.showMessageDialog(this, "✅ Added " + qty + " to cart.");
        });
    }
}
