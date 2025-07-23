package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import main.StyleLoader;

public class CustomerCatalog extends JFrame {
    private JComboBox<String> currencySelector;
    private JTextField searchField;
    private String currentCurrency = "PHP";
    private int selectedBookId = -1;
    private JPanel bookPanel;
    private JButton viewDetailsBtn;
    private JButton addToCartBtn;

    public CustomerCatalog() {
        setTitle("📚 BookMart Online - Browse Books");
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Load Styles
        StyleLoader style = new StyleLoader("src/css/customercatalog.css");

        // 🔝 Header Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(style.getColor("panel.top.bg"));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("BookMart");
        title.setFont(style.getFont("header.font"));
        title.setForeground(style.getColor("header.fg"));
        topPanel.add(title, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        JButton cartBtn = new JButton("Cart");
        JButton ordersBtn = new JButton("Order History");
        JButton logoutBtn = new JButton("Logout");
        currencySelector = new JComboBox<>(new String[]{"PHP", "USD", "KRW"});
        searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search");

        JButton[] rightButtons = {cartBtn, ordersBtn, logoutBtn, searchBtn};
        for (JButton btn : rightButtons) {
            btn.setBackground(style.getColor("button.bg"));
            btn.setForeground(style.getColor("button.fg"));
            btn.setFont(style.getFont("button.font"));
            btn.setFocusPainted(false);
        }

        rightPanel.add(new JLabel("Currency:"));
        rightPanel.add(currencySelector);
        rightPanel.add(searchField);
        rightPanel.add(searchBtn);
        rightPanel.add(cartBtn);
        rightPanel.add(ordersBtn);
        rightPanel.add(logoutBtn);

        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 📚 Book Panel (Center)
        bookPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        bookPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JScrollPane scrollPane = new JScrollPane(bookPanel);
        add(scrollPane, BorderLayout.CENTER);

        // 🔻 Bottom Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(style.getColor("panel.bottom.bg"));

        viewDetailsBtn = new JButton("View Details");
        addToCartBtn = new JButton("Add to Cart");

        JButton[] bottomButtons = {viewDetailsBtn, addToCartBtn};
        for (JButton btn : bottomButtons) {
            btn.setBackground(style.getColor("button.bg"));
            btn.setForeground(style.getColor("button.fg"));
            btn.setFont(style.getFont("button.font"));
            btn.setFocusPainted(false);
        }

        viewDetailsBtn.setEnabled(false);
        addToCartBtn.setEnabled(false);

        bottomPanel.add(viewDetailsBtn);
        bottomPanel.add(addToCartBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // 🔁 Actions
        currencySelector.addActionListener(e -> loadBooks(searchField.getText(), (String) currencySelector.getSelectedItem()));
        searchBtn.addActionListener(e -> loadBooks(searchField.getText(), (String) currencySelector.getSelectedItem()));
        cartBtn.addActionListener(e -> new Cart().setVisible(true));
        ordersBtn.addActionListener(e -> new OrderHistory().setVisible(true));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginScreen().setVisible(true);
        });

        viewDetailsBtn.addActionListener(e -> {
            if (selectedBookId != -1)
                new BookDetails(selectedBookId, currentCurrency).setVisible(true);
        });

        addToCartBtn.addActionListener(e -> {
            if (selectedBookId != -1) {
                String input = JOptionPane.showInputDialog(this, "Enter quantity:", "1");
                if (input != null) {
                    try {
                        int qty = Integer.parseInt(input);
                        if (qty < 1) {
                            JOptionPane.showMessageDialog(this, "❌ Quantity must be at least 1.");
                            return;
                        }
                        try (Connection conn = DBConnection.getConnection()) {
                            PreparedStatement stockStmt = conn.prepareStatement("SELECT stock_quantity FROM books WHERE book_id = ?");
                            stockStmt.setInt(1, selectedBookId);
                            ResultSet rs = stockStmt.executeQuery();
                            if (rs.next()) {
                                int stock = rs.getInt(1);
                                if (qty > stock) {
                                    JOptionPane.showMessageDialog(this, "❌ Not enough stock. Only " + stock + " left.");
                                    return;
                                }
                            }

                            PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO cart_items (user_id, book_id, quantity) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)");
                            stmt.setInt(1, LoginScreen.loggedInUserId);
                            stmt.setInt(2, selectedBookId);
                            stmt.setInt(3, qty);
                            stmt.executeUpdate();
                            JOptionPane.showMessageDialog(this, "✅ Added to cart!");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "❌ Please enter a valid number.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        loadBooks("", currentCurrency);
    }

    private void loadBooks(String keyword, String currency) {
        bookPanel.removeAll();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT b.book_id, b.title, b.genre,
                ROUND(b.price / c.exchange_rate_to_php, 2) AS converted_price, b.stock_quantity
                FROM books b JOIN currencies c ON c.currency_code=?
                WHERE b.title LIKE ? OR b.genre LIKE ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currency);
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String genre = rs.getString("genre");
                double price = rs.getDouble("converted_price");
                int stock = rs.getInt("stock_quantity");

                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    new EmptyBorder(10, 10, 10, 10)));
                card.setBackground(Color.WHITE);

                JLabel titleLbl = new JLabel(title);
                titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));

                JLabel genreLbl = new JLabel("Genre: " + genre);
                genreLbl.setFont(new Font("SansSerif", Font.ITALIC, 12));

                JLabel priceLbl = new JLabel("Price: " + price + " " + currency);
                JLabel stockLbl = new JLabel("Stock: " + stock);

                card.add(titleLbl);
                card.add(genreLbl);
                card.add(priceLbl);
                card.add(stockLbl);

                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        selectedBookId = bookId;
                        viewDetailsBtn.setEnabled(true);
                        addToCartBtn.setEnabled(true);
                    }
                });

                bookPanel.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        bookPanel.revalidate();
        bookPanel.repaint();
    }
} 