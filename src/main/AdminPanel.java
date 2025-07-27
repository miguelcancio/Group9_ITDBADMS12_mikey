package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

import main.StyleLoader1;



public class AdminPanel extends JFrame {
    private final AdminService adminService = new AdminService();
    private final int adminUserId = LoginScreen.loggedInUserId;

    // Book Management
    private DefaultTableModel bookTableModel;
    // Orders
    private DefaultTableModel orderTableModel; 
    // Transactions 
    // private DefaultTableModel transactionTableModel;
    // Users
    private DefaultTableModel userTableModel;
    // Currencies
    private DefaultTableModel currencyTableModel;
    
    private JPanel contentPanel;
    private JPanel cardPanel;
    private JTabbedPane tabbedPane;

    
    

    public AdminPanel() {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(this, "Access denied: Admins only.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        setTitle("📚 Admin Dashboard - BookMart");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        headerPanel.setBackground(Color.decode("#003059"));

        String[] sections = { "Books", "Orders", "Users", "Currencies" };
        JButton[] navButtons = new JButton[sections.length];

        // Shared font style
        Font headerFont = new Font("Segoe UI", Font.BOLD, 14);

        for (int i = 0; i < sections.length; i++) {
            String section = sections[i];
            JButton button = new JButton(section);
            button.setFocusPainted(false);
            button.setForeground(Color.WHITE);
            button.setBackground(Color.decode("#003059"));
            button.setFont(headerFont);
            button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            int index = i; // required for lambda
            button.addActionListener(e -> switchPanel(section));
            navButtons[i] = button;
            headerPanel.add(button);
        }

        // Add header to top
        add(headerPanel, BorderLayout.NORTH);

        // Add content panel (the main area that changes)
        contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Initially show Books panel
        switchPanel("Books");

    }
    
    private void switchPanel(String section) {
        contentPanel.removeAll();

        switch (section) {
            case "Books":
                contentPanel.add(createBookPanel(), BorderLayout.CENTER);
                break;
            case "Orders":
                contentPanel.add(createOrderPanel(), BorderLayout.CENTER);
                break;

            case "Users":
                contentPanel.add(createUserPanel(), BorderLayout.CENTER);
                break;
            case "Currencies":
                contentPanel.add(createCurrencyPanel(), BorderLayout.CENTER);
                break;
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private boolean isAdmin() {
        // In a real app, check role from DB or session
        // Here, assume user is admin if they reached this panel
        return adminUserId > 0;
    }
    

    // BOOK MANAGEMENT TAB
    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        gridPanel.setBackground(StyleLoader1.BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        panel.setBackground(StyleLoader1.BG_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add Book");
        btnPanel.add(StyleLoader1.styleButton(addBtn));
        panel.add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            addOrEditBookDialog(false, -1);
            loadBooksAsCards(gridPanel);
        });

        loadBooksAsCards(gridPanel);
        return panel;
    }
    private void loadBooksAsCards(JPanel gridPanel) {
        gridPanel.removeAll();
        List<AdminService.BookInfo> books = adminService.getAllBooks(adminUserId);
        for (AdminService.BookInfo book : books) {
            JPanel card = new JPanel(new BorderLayout(10, 5));
            card.setBackground(StyleLoader1.CARD_COLOR);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            JLabel title = new JLabel(book.title);
            title.setFont(StyleLoader1.TITLE_FONT);

            JLabel genre = new JLabel("Genre: " + book.genre);
            JLabel price = new JLabel("₱" + book.price);
            JLabel stock = new JLabel("Stock: " + book.stockQuantity);

            JPanel info = new JPanel(new GridLayout(0, 1));
            info.setOpaque(false);
            info.add(title);
            info.add(genre);
            info.add(price);
            info.add(stock);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnRow.setOpaque(false);
            JButton editBtn = new JButton("Edit");
            JButton delBtn = new JButton("Delete");
            StyleLoader1.styleButton(editBtn);
            StyleLoader1.styleButton(delBtn);
            btnRow.add(editBtn);
            btnRow.add(delBtn);

            editBtn.addActionListener(e -> {
                addOrEditBookDialog(true, book.bookId);
                loadBooksAsCards(gridPanel);
            });

            delBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete book \"" + book.title + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = adminService.deleteBook(adminUserId, book.bookId);
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Book deleted.");
                        loadBooksAsCards(gridPanel);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete book.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            card.add(info, BorderLayout.CENTER);
            card.add(btnRow, BorderLayout.SOUTH);
            gridPanel.add(card);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }




    private void addOrEditBookDialog(boolean isEdit, int bookId) {
        JTextField t = new JTextField(), g = new JTextField(), p = new JTextField(), s = new JTextField();
        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Title:")); panel.add(t);
        panel.add(new JLabel("Genre:")); panel.add(g);
        panel.add(new JLabel("Price:")); panel.add(p);
        panel.add(new JLabel("Stock:")); panel.add(s);

        if (isEdit) {
            // Optional: Pre-fill fields if needed
        }

        int result = JOptionPane.showConfirmDialog(this, panel,
                (isEdit ? "Edit" : "Add") + " Book", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String title = t.getText().trim();
            String genre = g.getText().trim();
            String priceStr = p.getText().trim();
            String stockStr = s.getText().trim();

            if (title.isEmpty() || genre.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);

                boolean ok;
                if (isEdit) {
                    ok = adminService.updateBook(adminUserId, bookId, title, genre, price, stock);
                } else {
                    ok = adminService.addBook(adminUserId, title, genre, price, stock);
                }

                if (ok) {
                    JOptionPane.showMessageDialog(this, (isEdit ? "Book updated." : "Book added."));

                    // ✅ Real-time GUI refresh
                    SwingUtilities.invokeLater(() -> {
                        // Find the current book panel and refresh it
                        JPanel bookPanel = createBookPanel();
                        contentPanel.removeAll();
                        contentPanel.add(bookPanel, BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    });



                } else {
                    JOptionPane.showMessageDialog(this, "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values for Price and Stock.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }






 // ORDERS TAB
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleLoader1.BG_COLOR);

        orderTableModel = new DefaultTableModel(new String[]{
            "Order ID", "User", "Date", "Amount", "Currency", "Status", "Books"
        }, 0);

        JTable table = new JTable(orderTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            // Ensure multi-line display in "Books" column
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 6) { // "Books" column
                    return new JTextAreaRenderer();
                }
                return super.getCellRenderer(row, column);
            }
        };

        table.setFont(StyleLoader1.TEXT_FONT);
        table.setRowHeight(60); // taller to allow space for multi-line text
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 10)); // space between rows
        table.setSelectionBackground(new Color(220, 235, 245));

        // Default renderer with padding and alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // internal padding
                setFont(StyleLoader1.TEXT_FONT);
                return c;
            }
        });

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(0x4A90E2)); // Light Blue
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(StyleLoader1.CARD_COLOR);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.add(scrollPane, BorderLayout.CENTER);

        panel.add(StyleLoader1.cardWrap(card), BorderLayout.CENTER);

        loadOrders(); // Load the formatted data
        return panel;
    }

    // Renderer to support multi-line text (especially for the "Books" column)
    class JTextAreaRenderer extends JTextArea implements TableCellRenderer {
        public JTextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setFont(StyleLoader1.TEXT_FONT);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // same padding
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            setBackground(isSelected ? table.getSelectionBackground()
                                     : row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
            setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
            return this;
        }
    }

    private void loadOrders() {
        orderTableModel.setRowCount(0);
        List<AdminService.OrderInfo> orders = adminService.getAllOrders(adminUserId);

        for (AdminService.OrderInfo o : orders) {
            // Ensure newline per book, trimming extra whitespace
            String booksFormatted = Arrays.stream(o.bookList.split(","))
                                          .map(String::trim)
                                          .collect(Collectors.joining("\n"));

            orderTableModel.addRow(new Object[]{
                o.orderId, o.userId, o.orderDate, o.totalAmount,
                o.currencyCode, o.status, booksFormatted
            });
        }
    }

/*

    // TRANSACTIONS TAB
    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleLoader1.BG_COLOR);

        transactionTableModel = new DefaultTableModel(new String[]{"Transaction ID", "Order ID", "Method", "Status", "Amount", "Timestamp"}, 0);
        JTable table = new JTable(transactionTableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadTransactions();
        return panel;
    }


    private void loadTransactions() {
        transactionTableModel.setRowCount(0);
        List<AdminService.TransactionLog> logs = adminService.getAllTransactionLogs(adminUserId);
        for (AdminService.TransactionLog log : logs) {
            transactionTableModel.addRow(new Object[]{log.transactionId, log.orderId, log.paymentMethod, log.paymentStatus, log.amount, log.timestamp});
        }
    }
*/
    

    // USERS TAB
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleLoader1.BG_COLOR);

        userTableModel = new DefaultTableModel(new String[]{"User ID", "Name", "Role"}, 0);
        JTable table = new JTable(userTableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(StyleLoader1.BG_COLOR);
        
        JButton changeRoleBtn = StyleLoader1.styleButton(new JButton("Change Role"));
        JButton viewOrdersBtn = StyleLoader1.styleButton(new JButton("View Orders"));
        JButton deleteUserBtn = StyleLoader1.styleButton(new JButton("Delete User"));

        btnPanel.add(changeRoleBtn);
        btnPanel.add(viewOrdersBtn);
        btnPanel.add(deleteUserBtn);

        changeRoleBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int userId = (int) userTableModel.getValueAt(row, 0);
                String[] roles = {"Admin", "Staff", "Customer"};
                String newRole = (String) JOptionPane.showInputDialog(this, "Select new role:", "Change Role", JOptionPane.PLAIN_MESSAGE, null, roles, roles[0]);
                if (newRole != null) {
                    boolean ok = adminService.updateUserRole(adminUserId, userId, newRole);
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Role updated.");
                        loadUsers();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update role.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a user to change role.");
            }
        });

        viewOrdersBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int userId = (int) userTableModel.getValueAt(row, 0);
                String userName = (String) userTableModel.getValueAt(row, 1);
                showUserOrderHistory(userId, userName);
            } else {
                JOptionPane.showMessageDialog(this, "Select a user to view order history.");
            }
        });

        deleteUserBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int userId = (int) userTableModel.getValueAt(row, 0);
                String userName = (String) userTableModel.getValueAt(row, 1);
                // Prevent admin from deleting themselves
                if (userId == adminUserId) {
                    JOptionPane.showMessageDialog(this, "You cannot delete your own account.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete user '" + userName + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = adminService.deleteUser(adminUserId, userId);
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "User deleted successfully.");
                        loadUsers();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a user to delete.");
            }
        });
        

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        loadUsers();
        return panel;
    }

    private void loadUsers() {
        userTableModel.setRowCount(0);
        List<AdminService.UserInfo> users = adminService.getAllUsers(adminUserId);
        for (AdminService.UserInfo user : users) {
            userTableModel.addRow(new Object[]{
                user.userId,
                user.name,
                user.role
            });
        }
        
    }
    

 
 // CURRENCIES TAB
    private JPanel createCurrencyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleLoader1.BG_COLOR);

        // Table Model
        currencyTableModel = new DefaultTableModel(new String[]{"Currency", "Rate to PHP"}, 0);
        JTable table = new JTable(currencyTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setFont(StyleLoader1.TEXT_FONT);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 10));
        table.setSelectionBackground(new Color(220, 235, 245));

        // Table Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(0x4A90E2));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Update Button
        JButton updateBtn = StyleLoader1.styleButton(new JButton("Update Rate"));
        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String code = (String) currencyTableModel.getValueAt(row, 0);
                String newRateStr = JOptionPane.showInputDialog(this, "New rate for " + code + ":");
                try {
                    double newRate = Double.parseDouble(newRateStr);
                    boolean ok = adminService.updateExchangeRate(adminUserId, code, newRate);
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Rate updated.");
                        loadCurrencies();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update rate.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid rate.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a currency to update.");
            }
        });

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(StyleLoader1.BG_COLOR);
        btnPanel.add(updateBtn);

        // Combine all panels
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(StyleLoader1.CARD_COLOR);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);

        panel.add(StyleLoader1.cardWrap(card), BorderLayout.CENTER);

        loadCurrencies();
        return panel;
    }




    private void loadCurrencies() {
        currencyTableModel.setRowCount(0);
        List<AdminService.CurrencyInfo> currencies = adminService.getAllCurrencies(adminUserId);
        for (AdminService.CurrencyInfo currency : currencies) {
            currencyTableModel.addRow(new Object[]{
                currency.currencyCode,
                currency.exchangeRate
            });
        }
    }

    // Add this method to display user order history in a dialog
    private void showUserOrderHistory(int userId, String userName) {
        java.util.List<AdminService.OrderInfo> orders = adminService.getUserOrderHistory(adminUserId, userId);
        StringBuilder sb = new StringBuilder();
        sb.append("Order History for ").append(userName).append(":\n\n");
        if (orders.isEmpty()) {
            sb.append("No orders found.");
        } else {
            sb.append(String.format("%-10s %-20s %-10s %-10s %-10s %-40s\n", "OrderID", "Date", "Amount", "Currency", "Status", "Books Ordered"));
            for (AdminService.OrderInfo o : orders) {
                sb.append(String.format("%-10d %-20s %-10.2f %-10s %-10s %-40s\n",
                    o.orderId, o.orderDate, o.totalAmount, o.currencyCode, o.status, o.bookList != null ? o.bookList : ""));
            }
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(900, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "User Order History", JOptionPane.INFORMATION_MESSAGE);
    }
}
