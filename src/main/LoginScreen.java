package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginScreen extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginScreen() {
        setTitle("📚 BookMart Online - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(new JLabel("Email:"), setPosition(gbc,0,0));
        emailField = new JTextField(20);
        panel.add(emailField, setPosition(gbc,1,0));

        panel.add(new JLabel("Password:"), setPosition(gbc,0,1));
        passwordField = new JPasswordField(20);
        panel.add(passwordField, setPosition(gbc,1,1));

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> authenticateUser());
        panel.add(loginButton, setPosition(gbc,0,2,2,1));

        statusLabel = new JLabel("", SwingConstants.CENTER);
        panel.add(statusLabel, setPosition(gbc,0,3,2,1));

        add(panel);
    }

    private GridBagConstraints setPosition(GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        return gbc;
    }

    private GridBagConstraints setPosition(GridBagConstraints gbc, int x, int y, int w, int h) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        return gbc;
    }

    private void authenticateUser() {
        String email = emailField.getText();
        String password = String.valueOf(passwordField.getPassword());

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                statusLabel.setText("❌ DB Connection failed.");
                return;
            }
            String sql = "SELECT role FROM users WHERE email=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                statusLabel.setText("✅ Login: " + role);
                openDashboard(role);
                dispose();
            } else {
                statusLabel.setText("❌ Invalid credentials.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("❌ DB error.");
        }
    }

    private void openDashboard(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                new AdminPanel().setVisible(true); break;
            case "staff":
                JOptionPane.showMessageDialog(this,"✅ Staff Dashboard placeholder.");
                break;
            case "customer":
                new CustomerCatalog().setVisible(true); break;
            default:
                JOptionPane.showMessageDialog(this,"Unknown role: "+role);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
