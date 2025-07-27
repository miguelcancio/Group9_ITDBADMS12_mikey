package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Cart extends JFrame {
    private DefaultTableModel tableModel;
    private ArrayList<CartItem> cartItems = new ArrayList<>();
    private double total = 0.0;
    private int selectedCurrencyId = 1;
    private CustomerCatalog catalog;

    public Cart(CustomerCatalog catalog) {
        this.catalog = catalog;

        setTitle("🛒 Your Cart - BookMart");
        setSize(700, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xf4f6fa));

        StyleLoader style = new StyleLoader("src/css/customercatalog.css");

     // Unified Header Design
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0x003059)); 
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Your Cart");
        title.setFont(new Font("Arial", Font.BOLD, 20)); 
        title.setForeground(Color.WHITE);
        headerPanel.add(title);

        add(headerPanel, BorderLayout.NORTH);


        // Table
        tableModel = new DefaultTableModel(new String[]{"Title", "Quantity", "Price", "Subtotal"}, 0);
        JTable table = new JTable(tableModel);

        // Set custom header background color
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                            boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setOpaque(true);
                label.setBackground(Color.decode("#4a90e2")); // Header background
                label.setForeground(Color.WHITE);             // Header text color
                label.setFont(new Font("SansSerif", Font.BOLD, 13));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        table.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(scrollPane, BorderLayout.CENTER);

        loadCart();

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        bottomPanel.setBackground(new Color(0xf4f6fa));

        JLabel totalLabel = new JLabel("Total: ₱ " + String.format("%.2f", total));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalLabel.setForeground(new Color(0xc0722c));

        JButton orderBtn = new JButton("✅ Place Order");
        orderBtn.setFont(style.getFont("button.font"));
        orderBtn.setBackground(style.getColor("button.bg"));
        orderBtn.setForeground(style.getColor("button.fg"));
        orderBtn.setFocusPainted(false);
        orderBtn.setBorder(style.getRoundedBorder(30));
        orderBtn.setPreferredSize(new Dimension(160, 35));

        orderBtn.addActionListener(e -> placeOrder());

        bottomPanel.add(totalLabel, BorderLayout.WEST);
        bottomPanel.add(orderBtn, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public Cart() {
        this(null);
    }

    private void loadCart() {
        cartItems.clear();
        total = 0.0;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT c.book_id, b.title, c.quantity, b.price, (c.quantity * b.price) AS subtotal
                FROM cart_items c
                JOIN books b ON c.book_id = b.book_id
                WHERE c.user_id = ?
            """);
            stmt.setInt(1, LoginScreen.loggedInUserId);
            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0);

            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double subtotal = rs.getDouble("subtotal");
                cartItems.add(new CartItem(bookId, qty, price));
                total += subtotal;

                tableModel.addRow(new Object[]{title, qty, price, subtotal});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void placeOrder() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "🛑 Cart is empty.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement currencyStmt = conn.prepareStatement("SELECT currency_id FROM currencies WHERE currency_code = 'PHP'");
            ResultSet cr = currencyStmt.executeQuery();
            if (cr.next()) selectedCurrencyId = cr.getInt("currency_id");

            PreparedStatement orderStmt = conn.prepareStatement(
                "INSERT INTO orders (user_id, total_amount, currency_id, status) VALUES (?, ?, ?, 'Pending')",
                Statement.RETURN_GENERATED_KEYS
            );
            orderStmt.setInt(1, LoginScreen.loggedInUserId);
            orderStmt.setDouble(2, total);
            orderStmt.setInt(3, selectedCurrencyId);
            orderStmt.executeUpdate();

            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = -1;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

            PreparedStatement itemStmt = conn.prepareStatement(
                "INSERT INTO order_items (order_id, book_id, quantity, price_each) VALUES (?, ?, ?, ?)"
            );
            for (CartItem item : cartItems) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.bookId);
                itemStmt.setInt(3, item.quantity);
                itemStmt.setDouble(4, item.price);
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            PreparedStatement updateStockStmt = conn.prepareStatement(
                "UPDATE books SET stock_quantity = stock_quantity - ? WHERE book_id = ? AND stock_quantity >= ?"
            );
            for (CartItem item : cartItems) {
                updateStockStmt.setInt(1, item.quantity);
                updateStockStmt.setInt(2, item.bookId);
                updateStockStmt.setInt(3, item.quantity);
                updateStockStmt.addBatch();
            }
            int[] updateCounts = updateStockStmt.executeBatch();
            for (int count : updateCounts) {
                if (count == 0) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "❌ Failed to update stock. Possibly insufficient stock.");
                    return;
                }
            }

            PreparedStatement clearCartStmt = conn.prepareStatement("DELETE FROM cart_items WHERE user_id = ?");
            clearCartStmt.setInt(1, LoginScreen.loggedInUserId);
            clearCartStmt.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "✅ Order placed!");

            if (catalog != null) {
                catalog.refreshBooks();
            }

            this.dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to place order.");
        }
    }

    private static class CartItem {
        int bookId;
        int quantity;
        double price;

        public CartItem(int bookId, int quantity, double price) {
            this.bookId = bookId;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
