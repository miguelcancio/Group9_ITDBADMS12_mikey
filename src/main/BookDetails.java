package main;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BookDetails extends JFrame {
    public BookDetails(int bookId, String currency) {
        setTitle("📖 Book Details");
        setSize(400,400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0,1));
        JLabel titleLabel = new JLabel();
        JLabel genreLabel = new JLabel();
        JLabel priceLabel = new JLabel();
        JLabel stockLabel = new JLabel();
        panel.add(titleLabel); panel.add(genreLabel);
        panel.add(priceLabel); panel.add(stockLabel);

        add(panel);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT b.title, b.genre, b.stock_quantity,
                ROUND(b.price / c.exchange_rate_to_php, 2) AS converted_price
                FROM books b JOIN currencies c ON c.currency_code=?
                WHERE b.book_id=?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currency);
            stmt.setInt(2, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                titleLabel.setText("Title: " + rs.getString(1));
                genreLabel.setText("Genre: " + rs.getString(2));
                stockLabel.setText("Stock: " + rs.getInt(3));
                priceLabel.setText("Price ("+currency+"): " + rs.getDouble(4));
            }
        } catch(SQLException e){ e.printStackTrace(); }
    }
}
