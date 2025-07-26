package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JFrame {
    private final AdminService adminService = new AdminService();
    private final int adminUserId = LoginScreen.loggedInUserId;

    // Book Management
    private DefaultTableModel bookTableModel;
    // Orders
    private DefaultTableModel orderTableModel;
    // Transactions
    private DefaultTableModel transactionTableModel;
    // Users
    private DefaultTableModel userTableModel;
    // Currencies
    private DefaultTableModel currencyTableModel;

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

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books", createBookPanel());
        tabs.addTab("Orders", createOrderPanel());
        tabs.addTab("Transactions", createTransactionPanel());
        tabs.addTab("Users", createUserPanel());
        tabs.addTab("Currencies", createCurrencyPanel());
        add(tabs);
    }

    private boolean isAdmin() {
        // In a real app, check role from DB or session
        // Here, assume user is admin if they reached this panel
        return adminUserId > 0;
    }

    // BOOK MANAGEMENT TAB
    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        bookTableModel = new DefaultTableModel(new String[]{"ID", "Title", "Genre", "Price", "Stock"}, 0);
        JTable table = new JTable(bookTableModel);
        loadBooks();

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add Book");
        JButton editBtn = new JButton("Edit Book");
        JButton delBtn = new JButton("Delete Book");
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(delBtn);

        addBtn.addActionListener(e -> addOrEditBookDialog(false, -1));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int bookId = (int) bookTableModel.getValueAt(row, 0);
                addOrEditBookDialog(true, bookId);
            } else {
                JOptionPane.showMessageDialog(this, "Select a book to edit.");
            }
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int bookId = (int) bookTableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete book ID " + bookId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = adminService.deleteBook(adminUserId, bookId);
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Book deleted.");
                        loadBooks();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete book.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a book to delete.");
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadBooks() {
        bookTableModel.setRowCount(0);
        List<AdminService.BookInfo> books = adminService.getAllBooks(adminUserId);
        for (AdminService.BookInfo book : books) {
            bookTableModel.addRow(new Object[]{
                book.bookId,
                book.title,
                book.genre,
                book.price,
                book.stockQuantity
            });
        }
    }

    private void addOrEditBookDialog(boolean isEdit, int bookId) {
        JTextField t = new JTextField(), g = new JTextField(), p = new JTextField(), s = new JTextField();
        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Title:")); panel.add(t);
        panel.add(new JLabel("Genre:")); panel.add(g);
        panel.add(new JLabel("Price:")); panel.add(p);
        panel.add(new JLabel("Stock:")); panel.add(s);

        if (isEdit) {
            // Optionally pre-fill fields with book info (not implemented here)
        }

        if (JOptionPane.showConfirmDialog(this, panel, (isEdit ? "Edit" : "Add") + " Book", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String title = t.getText();
                String genre = g.getText();
                double price = Double.parseDouble(p.getText());
                int stock = Integer.parseInt(s.getText());
                boolean ok;
                if (isEdit) {
                    ok = adminService.updateBook(adminUserId, bookId, title, genre, price, stock);
                } else {
                    ok = adminService.addBook(adminUserId, title, genre, price, stock);
                }
                if (ok) {
                    JOptionPane.showMessageDialog(this, (isEdit ? "Book updated." : "Book added."));
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(this, "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ORDERS TAB
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        orderTableModel = new DefaultTableModel(new String[]{"OrderID", "UserID", "Date", "Amount", "Currency", "Status"}, 0);
        JTable table = new JTable(orderTableModel);
        loadOrders();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadOrders() {
        orderTableModel.setRowCount(0);
        List<AdminService.OrderInfo> orders = adminService.getAllOrders(adminUserId);
        for (AdminService.OrderInfo o : orders) {
            orderTableModel.addRow(new Object[]{o.orderId, o.userId, o.orderDate, o.totalAmount, o.currencyCode, o.status});
        }
    }

    // TRANSACTIONS TAB
    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        transactionTableModel = new DefaultTableModel(new String[]{"TransactionID", "OrderID", "Method", "Status", "Amount", "Timestamp"}, 0);
        JTable table = new JTable(transactionTableModel);
        loadTransactions();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadTransactions() {
        transactionTableModel.setRowCount(0);
        List<AdminService.TransactionLog> logs = adminService.getAllTransactionLogs(adminUserId);
        for (AdminService.TransactionLog log : logs) {
            transactionTableModel.addRow(new Object[]{log.transactionId, log.orderId, log.paymentMethod, log.paymentStatus, log.amount, log.timestamp});
        }
    }

    // USERS TAB
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        userTableModel = new DefaultTableModel(new String[]{"UserID", "Name", "Role"}, 0);
        JTable table = new JTable(userTableModel);
        loadUsers();

        JButton changeRoleBtn = new JButton("Change Role");
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
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(changeRoleBtn, BorderLayout.SOUTH);
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
        currencyTableModel = new DefaultTableModel(new String[]{"Currency", "Rate to PHP"}, 0);
        JTable table = new JTable(currencyTableModel);
        loadCurrencies();

        JButton updateBtn = new JButton("Update Rate");
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
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(updateBtn, BorderLayout.SOUTH);
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
}
