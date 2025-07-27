package main;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginScreen extends JFrame {
    public static int loggedInUserId = -1;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    private final Color BG_COLOR = new Color(0x003059);
    private final Color ACCENT_COLOR = new Color(0xFFFFFF);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public LoginScreen() {
        setTitle("📚 BookMart Online - Login");
        setSize(420, 340);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Welcome to BookMart");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridwidth = 2;
        panel.add(titleLabel, position(gbc, 0, 0));

        gbc.gridwidth = 1;

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(LABEL_FONT);
        emailLabel.setForeground(ACCENT_COLOR);
        panel.add(emailLabel, position(gbc, 0, 1));

        emailField = new JTextField(20);
        styleInput(emailField);
        panel.add(emailField, position(gbc, 1, 1));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(LABEL_FONT);
        passwordLabel.setForeground(ACCENT_COLOR);
        panel.add(passwordLabel, position(gbc, 0, 2));

        passwordField = new JPasswordField(20);
        styleInput(passwordField);
        panel.add(passwordField, position(gbc, 1, 2));

        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn);
        loginBtn.addActionListener(e -> login());
        panel.add(loginBtn, position(gbc, 0, 3));

        JButton signUpBtn = new JButton("Sign Up");
        styleButton(signUpBtn);
        signUpBtn.addActionListener(e -> new SignUpScreen().setVisible(true));
        panel.add(signUpBtn, position(gbc, 1, 3));

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridwidth = 2;
        panel.add(statusLabel, position(gbc, 0, 4));

        add(panel);
    }

    private void styleInput(JTextField field) {
        field.setFont(LABEL_FONT);
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(0x00548F));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(BUTTON_FONT);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private GridBagConstraints position(GridBagConstraints gbc, int x, int y) {
        return position(gbc, x, y, 1, 1);
    }

    private GridBagConstraints position(GridBagConstraints gbc, int x, int y, int w, int h) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        return gbc;
    }

    private void login() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT user_id, role FROM users WHERE email=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emailField.getText());
            stmt.setString(2, String.valueOf(passwordField.getPassword()));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                loggedInUserId = rs.getInt("user_id");
                String role = rs.getString("role");
                openDashboard(role);
                dispose();
            } else {
                statusLabel.setText("❌ Invalid credentials.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("❌ Database error.");
        }
    }

    private void openDashboard(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                new AdminPanel().setVisible(true);
                break;
            case "staff":
                new StaffPanel().setVisible(true);
                break;
            default:
                new CustomerCatalog().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
