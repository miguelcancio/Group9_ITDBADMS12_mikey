package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StaffPanel extends JFrame {
    private JComboBox<String> currencySelector;
    private String currentCurrency = "PHP";
    private int selectedBookId = -1;
    private JPanel bookPanel;
    private JButton addBookBtn;
    private JButton removeBookBtn;
    private final int staffUserId = LoginScreen.loggedInUserId;
    private final StaffService staffService = new StaffService();

    public StaffPanel() {
        setTitle("👨‍💼 Staff Dashboard - BookMart");
        setSize(950, 700);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xf4f6fa));

        StyleLoader style = new StyleLoader("src/css/customercatalog.css");

     // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0x003059));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("<html>Staff Panel</html>");
        title.setFont(style.getFont("button.font"));
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.LEFT);
        topPanel.add(title, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        rightPanel.setOpaque(false);

        JButton logoutBtn = new JButton("Logout");


        // Staff-specific buttons
        addBookBtn = new JButton("➕ Add Book");
        removeBookBtn = new JButton("🗑️ Remove Book");




        rightPanel.add(Box.createHorizontalStrut(8));

        rightPanel.add(addBookBtn);
        rightPanel.add(removeBookBtn);
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


        add(bottomPanel, BorderLayout.SOUTH);

        // Event Listeners
        logoutBtn.addActionListener(e -> logout());
        
        // Staff-specific event listeners
        addBookBtn.addActionListener(e -> showAddBookDialog());
        removeBookBtn.addActionListener(e -> removeSelectedBook());
        


        // Load initial books
        loadBooks("", "PHP");
        
        // Add window resize listener
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                topPanel.revalidate();
                topPanel.repaint();
            }
        });
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
        

        
        // Book info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel genreLabel = new JLabel(genre);
        genreLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        genreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        genreLabel.setForeground(Color.GRAY);
        
        String currencySymbol = currency.equals("PHP") ? "₱" : currency.equals("USD") ? "$" : "₩";
        JLabel priceLabel = new JLabel(currencySymbol + String.format("%.2f", price));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(new Color(0xc0722c));
        
        JLabel stockLabel = new JLabel("Stock: " + stock);
        stockLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        
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
                    loadBooks("", currentCurrency); // Refresh after adding


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
                loadBooks("", currentCurrency); // Refresh after removing

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



    private void logout() {
        LoginScreen.loggedInUserId = -1;
        dispose();
        new LoginScreen().setVisible(true);
    }
}
