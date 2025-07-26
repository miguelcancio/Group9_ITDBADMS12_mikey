package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StaffPanel extends JFrame {
    private JComboBox<String> currencySelector;
    private JTextField searchField;
    private String currentCurrency = "PHP";
    private int selectedBookId = -1;
    private JPanel bookPanel;
    private JButton viewDetailsBtn;
    private JButton addToCartBtn;
    private JButton addBookBtn;
    private JButton removeBookBtn;
    private final int staffUserId = LoginScreen.loggedInUserId;
    private final StaffService staffService = new StaffService();

    public StaffPanel() {
        setTitle("👨‍💼 Staff Dashboard - BookMart");
        setSize(950, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xf4f6fa));

        StyleLoader style = new StyleLoader("src/css/customercatalog.css");

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0x003059));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("📘 BookMart - Staff Panel");
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
        
        // Staff-specific buttons
        addBookBtn = new JButton("➕ Add Book");
        removeBookBtn = new JButton("🗑️ Remove Book");
        
        // Apply styles
        style.applyStyle(searchBtn, "round");
        style.applyStyle(cartBtn, "round");
        style.applyStyle(ordersBtn, "round");
        style.applyStyle(logoutBtn, "round");
        style.applyStyle(addBookBtn, "round");
        style.applyStyle(removeBookBtn, "round");

        JLabel currencyLabel = new JLabel("Currency:");
        currencyLabel.setForeground(Color.WHITE);
        currencyLabel.setFont(style.getFont("button.font"));

        rightPanel.add(currencyLabel);
        rightPanel.add(Box.createHorizontalStrut(8)); // Add spacing
        rightPanel.add(currencySelector);
        rightPanel.add(searchField);
        rightPanel.add(searchBtn);
        rightPanel.add(addBookBtn);
        rightPanel.add(removeBookBtn);
        rightPanel.add(cartBtn);
        rightPanel.add(ordersBtn);
        rightPanel.add(logoutBtn);

        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Book Cards Panel
        bookPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        bookPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        bookPanel.setBackground(new Color(0xf4f6fa));
        JScrollPane scrollPane = new JScrollPane(bookPanel);
        scrollPane.getViewport().setBackground(new Color(0xf4f6fa));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(0xf4f6fa));
        bottomPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

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

        // Event Listeners
        searchBtn.addActionListener(e -> loadBooks(searchField.getText(), (String) currencySelector.getSelectedItem()));
        currencySelector.addActionListener(e -> loadBooks(searchField.getText(), (String) currencySelector.getSelectedItem()));
        cartBtn.addActionListener(e -> new Cart().setVisible(true));
        ordersBtn.addActionListener(e -> new OrderHistory().setVisible(true));
        logoutBtn.addActionListener(e -> logout());
        
        // Staff-specific event listeners
        addBookBtn.addActionListener(e -> showAddBookDialog());
        removeBookBtn.addActionListener(e -> removeSelectedBook());
        
        viewDetailsBtn.addActionListener(e -> showBookDetails());
        addToCartBtn.addActionListener(e -> addToCart());

        // Load initial books
        loadBooks("", "PHP");
    }

    private void loadBooks(String keyword, String currency) {
        bookPanel.removeAll();
        currentCurrency = currency;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT b.book_id, b.title, b.genre, b.price, b.stock_quantity, c.exchange_rate_to_php
                FROM books b
                CROSS JOIN currencies c
                WHERE c.currency_code = ?
                AND (b.title LIKE ? OR b.genre LIKE ? OR ? = '')
                ORDER BY b.title
                """;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currency);
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");
            stmt.setString(4, keyword);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String genre = rs.getString("genre");
                double pricePHP = rs.getDouble("price");
                int stock = rs.getInt("stock_quantity");
                double exchangeRate = rs.getDouble("exchange_rate_to_php");
                
                double convertedPrice = pricePHP * exchangeRate;
                
                JPanel card = createBookCard(bookId, title, genre, convertedPrice, stock, currency);
                bookPanel.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
        
        bookPanel.revalidate();
        bookPanel.repaint();
    }

    private JPanel createBookCard(int bookId, String title, String genre, double price, int stock, String currency) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Border
                g2d.setColor(new Color(0xe0e0e0));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                
                g2d.dispose();
            }
        };
        
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setPreferredSize(new Dimension(250, 200));
        
        // Book icon
        JLabel iconLabel = new JLabel("📖", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        card.add(iconLabel, BorderLayout.NORTH);
        
        // Book info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel genreLabel = new JLabel(genre);
        genreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        genreLabel.setForeground(Color.GRAY);
        
        String currencySymbol = currency.equals("PHP") ? "₱" : currency.equals("USD") ? "$" : "₩";
        JLabel priceLabel = new JLabel(currencySymbol + String.format("%.2f", price));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(new Color(0x2e7d32));
        
        JLabel stockLabel = new JLabel("Stock: " + stock);
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stockLabel.setForeground(stock > 0 ? Color.GREEN : Color.RED);
        
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(genreLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(stockLabel);
        
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Click listener
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedBookId = bookId;
                viewDetailsBtn.setEnabled(true);
                addToCartBtn.setEnabled(stock > 0);
                
                // Highlight selected card
                for (java.awt.Component comp : bookPanel.getComponents()) {
                    if (comp instanceof JPanel) {
                        ((JPanel) comp).setBorder(new EmptyBorder(15, 15, 15, 15));
                    }
                }
                card.setBorder(BorderFactory.createLineBorder(new Color(0x2196f3), 2));
            }
        });
        
        return card;
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField titleField = new JTextField(20);
        JTextField genreField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JTextField stockField = new JTextField(20);
        
        dialog.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        dialog.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1;
        dialog.add(genreField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Price (PHP):"), gbc);
        gbc.gridx = 1;
        dialog.add(priceField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        dialog.add(stockField, gbc);
        
        JButton addBtn = new JButton("Add Book");
        JButton cancelBtn = new JButton("Cancel");
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, gbc);
        
        addBtn.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String genre = genreField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                
                if (title.isEmpty() || genre.isEmpty() || price <= 0 || stock < 0) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all fields with valid values.");
                    return;
                }
                
                boolean success = addBook(title, genre, price, stock);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Book added successfully!");
                    dialog.dispose();
                    loadBooks(searchField.getText(), (String) currencySelector.getSelectedItem());
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add book.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for price and stock.");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }

    private boolean addBook(String title, String genre, double price, int stock) {
        return staffService.addBook(title, genre, price, stock);
    }

    private void removeSelectedBook() {
        if (selectedBookId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to remove.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to remove this book?", 
            "Confirm Removal", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = removeBook(selectedBookId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Book removed successfully!");
                selectedBookId = -1;
                viewDetailsBtn.setEnabled(false);
                addToCartBtn.setEnabled(false);
                loadBooks(searchField.getText(), (String) currencySelector.getSelectedItem());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove book.");
            }
        }
    }

    private boolean removeBook(int bookId) {
        return staffService.removeBook(bookId);
    }

    private void showBookDetails() {
        if (selectedBookId == -1) return;
        
        StaffService.BookDetails book = staffService.getBookDetails(selectedBookId);
        if (book != null) {
            String details = String.format("""
                Book Details:
                
                Title: %s
                Genre: %s
                Price: ₱%.2f
                Stock: %d
                """, book.title, book.genre, book.price, book.stockQuantity);
            
            JOptionPane.showMessageDialog(this, details, "Book Details", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Error loading book details.");
        }
    }

    private void addToCart() {
        if (selectedBookId == -1) return;
        
        String input = JOptionPane.showInputDialog(this, "Enter quantity:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(input.trim());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Please enter a positive quantity.");
                    return;
                }
                
                boolean success = addToCart(selectedBookId, quantity);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Added to cart successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add to cart.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        }
    }

    private boolean addToCart(int bookId, int quantity) {
        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL addToCart(?, ?, ?)}");
            stmt.setInt(1, staffUserId);
            stmt.setInt(2, bookId);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void logout() {
        LoginScreen.loggedInUserId = -1;
        dispose();
        new LoginScreen().setVisible(true);
    }
}
