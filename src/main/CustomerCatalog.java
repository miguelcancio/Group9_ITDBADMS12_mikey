package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class CustomerCatalog extends JFrame {
    private JComboBox<String> currencySelector;
    private JTextField searchField;
    private String currentCurrency = "PHP";
    private int selectedBookId = -1;
    private JPanel bookPanel;
    private JButton viewDetailsBtn;
    private JButton addToCartBtn;

    public CustomerCatalog() {
        setTitle("📖 BookMart Online - Browse Books");
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xfffdd6));

        StyleLoader style = new StyleLoader("src/css/customercatalog.css");

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0x003059));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("BookMart");
        title.setFont(style.getFont("button.font"));
        title.setForeground(Color.WHITE);
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
            btn.setBorder(style.getRoundedBorder(30)); // 👈 oblong
        }

        JLabel currencyLabel = new JLabel("Currency:");
        currencyLabel.setForeground(Color.WHITE);
        currencyLabel.setFont(style.getFont("button.font"));

        rightPanel.add(currencyLabel);
        rightPanel.add(currencySelector);
        rightPanel.add(searchField);
        rightPanel.add(searchBtn);
        rightPanel.add(cartBtn);
        rightPanel.add(ordersBtn);
        rightPanel.add(logoutBtn);

        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Book Cards Panel
        bookPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        bookPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        bookPanel.setBackground(new Color(0xfbdeb7));
        JScrollPane scrollPane = new JScrollPane(bookPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0xfbdeb7));

        viewDetailsBtn = new JButton("View Details");
        addToCartBtn = new JButton("Add to Cart");

        JButton[] bottomButtons = {viewDetailsBtn, addToCartBtn};
        for (JButton btn : bottomButtons) {
            btn.setBackground(style.getColor("button.bg"));
            btn.setForeground(style.getColor("button.fg"));
            btn.setFont(style.getFont("button.font"));
            btn.setFocusPainted(false);
            btn.setBorder(style.getRoundedBorder(30));
        }

        viewDetailsBtn.setEnabled(false);
        addToCartBtn.setEnabled(false);

        bottomPanel.add(viewDetailsBtn);
        bottomPanel.add(addToCartBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Events
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

                JPanel card = new JPanel() {
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    }
                };

                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(new EmptyBorder(10, 10, 10, 10));
                card.setBackground(Color.WHITE);

                JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
                titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
                titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel genreLbl = new JLabel("Genre: " + genre);
                genreLbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
                genreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel priceLbl = new JLabel("Price: " + price + " " + currency);
                priceLbl.setForeground(new Color(0xc0722c));
                priceLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel stockLbl = new JLabel("Stock: " + stock);
                stockLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                card.add(titleLbl);
                card.add(Box.createVerticalStrut(5));
                card.add(genreLbl);
                card.add(Box.createVerticalStrut(5));
                card.add(priceLbl);
                card.add(Box.createVerticalStrut(5));
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
