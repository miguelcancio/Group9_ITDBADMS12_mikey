package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerCatalog extends JFrame {
    private JComboBox<String> currencySelector;
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private String currentCurrency = "PHP";
    private JTable bookTable;
    private int selectedBookId = -1;

    public CustomerCatalog() {
        setTitle("📚 BookMart Online - Browse Books");
        setSize(850, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton homeBtn = new JButton("Home");
        JButton cartBtn = new JButton("Cart");
        JButton logoutBtn = new JButton("Logout");

        currencySelector = new JComboBox<>(new String[]{"PHP", "USD", "KRW"});
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");

        topPanel.add(homeBtn); topPanel.add(cartBtn); topPanel.add(logoutBtn);
        topPanel.add(new JLabel("Currency:")); topPanel.add(currencySelector);
        topPanel.add(searchField); topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID","Title","Genre","Price","Stock"},0);
        bookTable = new JTable(tableModel);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton viewDetailsBtn = new JButton("View Details");
        JButton addToCartBtn = new JButton("Add to Cart");

        viewDetailsBtn.setEnabled(false);
        addToCartBtn.setEnabled(false);

        bottomPanel.add(viewDetailsBtn);
        bottomPanel.add(addToCartBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        loadBooks("", currentCurrency);

        currencySelector.addActionListener(e -> { currentCurrency=(String)currencySelector.getSelectedItem(); loadBooks(searchField.getText(), currentCurrency); });
        searchBtn.addActionListener(e -> loadBooks(searchField.getText(), currentCurrency));
        homeBtn.addActionListener(e -> loadBooks("", currentCurrency));
        cartBtn.addActionListener(e -> new Cart().setVisible(true));
        logoutBtn.addActionListener(e -> { dispose(); new LoginScreen().setVisible(true); });

        bookTable.getSelectionModel().addListSelectionListener(e -> {
            selectedBookId = bookTable.getSelectedRow()>=0 ? (int)tableModel.getValueAt(bookTable.getSelectedRow(),0) : -1;
            boolean enabled = selectedBookId!=-1;
            viewDetailsBtn.setEnabled(enabled);
            addToCartBtn.setEnabled(enabled);
        });

        viewDetailsBtn.addActionListener(e -> {
            if(selectedBookId!=-1)
                new BookDetails(selectedBookId, currentCurrency).setVisible(true);
        });

        addToCartBtn.addActionListener(e -> {
            if (selectedBookId != -1) {
                String input = JOptionPane.showInputDialog(this, "Enter quantity:", "1");

                if (input != null) { // Cancel not pressed
                    try {
                        int qty = Integer.parseInt(input);

                        if (qty < 1) {
                            JOptionPane.showMessageDialog(this, "❌ Quantity must be at least 1.");
                            return;
                        }

                        // Get current stock from the selected row in the table
                        int selectedRow = bookTable.getSelectedRow();
                        int stock = (int) tableModel.getValueAt(selectedRow, 4); // Column 4 = stock

                        if (qty > stock) {
                            JOptionPane.showMessageDialog(this, "❌ Not enough stock available. Only " + stock + " left.");
                            return;
                        }

                        // Proceed to add to cart
                        try (Connection conn = DBConnection.getConnection()) {
                            PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO cart_items (user_id, book_id, quantity) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)");
                            stmt.setInt(1, LoginScreen.loggedInUserId);
                            stmt.setInt(2, selectedBookId);
                            stmt.setInt(3, qty);
                            stmt.executeUpdate();
                            JOptionPane.showMessageDialog(this, "✅ Added to cart!");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "❌ Please enter a valid number.");
                    }
                }
                // else: Cancel pressed — do nothing
            }
        });


    }

    private void loadBooks(String keyword, String currency) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT b.book_id, b.title, b.genre,
                ROUND(b.price * c.exchange_rate_to_php,2) AS converted_price, b.stock_quantity
                FROM books b JOIN currencies c ON c.currency_code=?
                WHERE b.title LIKE ? OR b.genre LIKE ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currency); stmt.setString(2,"%"+keyword+"%"); stmt.setString(3,"%"+keyword+"%");
            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0);
            while(rs.next())
                tableModel.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getDouble(4),rs.getInt(5)});
        } catch(SQLException e){ e.printStackTrace(); }
    }
}
