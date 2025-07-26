package main;

import java.sql.*;

/**
 * Service class for staff operations.
 * Staff can add and remove books from the catalog.
 */
public class StaffService {
    
    /**
     * Adds a new book to the catalog.
     * @param title Book title
     * @param genre Book genre
     * @param price Book price in PHP
     * @param stock Stock quantity
     * @return true if successful, false otherwise
     */
    public boolean addBook(String title, String genre, double price, int stock) {
        validateBookInput(title, genre, price, stock);
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL addBooks(?, ?, ?, ?)}");
            stmt.setString(1, sanitize(title));
            stmt.setString(2, sanitize(genre));
            stmt.setDouble(3, price);
            stmt.setInt(4, stock);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a book from the catalog.
     * @param bookId Book ID to remove
     * @return true if successful, false otherwise
     */
    public boolean removeBook(int bookId) {
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL removeBooks(?)}");
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets book details by ID.
     * @param bookId Book ID
     * @return BookDetails object or null if not found
     */
    public BookDetails getBookDetails(int bookId) {
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL getBookDetails(?)}");
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new BookDetails(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Validates book input parameters.
     * @param title Book title
     * @param genre Book genre
     * @param price Book price
     * @param stock Stock quantity
     * @throws IllegalArgumentException if validation fails
     */
    private void validateBookInput(String title, String genre, double price, int stock) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Book genre cannot be empty");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Book price must be positive");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
    }

    /**
     * Sanitizes input strings to prevent SQL injection.
     * @param input Input string to sanitize
     * @return Sanitized string
     */
    private String sanitize(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[<>\"']", "");
    }

    /**
     * Inner class to represent book details.
     */
    public static class BookDetails {
        public final int bookId;
        public final String title;
        public final String genre;
        public final double price;
        public final int stockQuantity;

        public BookDetails(int bookId, String title, String genre, double price, int stockQuantity) {
            this.bookId = bookId;
            this.title = title;
            this.genre = genre;
            this.price = price;
            this.stockQuantity = stockQuantity;
        }

        @Override
        public String toString() {
            return String.format("BookDetails{bookId=%d, title='%s', genre='%s', price=%.2f, stockQuantity=%d}",
                bookId, title, genre, price, stockQuantity);
        }
    }
} 