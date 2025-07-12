package main;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignUpScreen extends JFrame {
    private JTextField nameField, emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public SignUpScreen() {
        setTitle("✍️ Sign Up - BookMart");
        setSize(400,300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);

        panel.add(new JLabel("Name:"), position(gbc,0,0));
        nameField = new JTextField(20);
        panel.add(nameField, position(gbc,1,0));

        panel.add(new JLabel("Email:"), position(gbc,0,1));
        emailField = new JTextField(20);
        panel.add(emailField, position(gbc,1,1));

        panel.add(new JLabel("Password:"), position(gbc,0,2));
        passwordField = new JPasswordField(20);
        panel.add(passwordField, position(gbc,1,2));

        JButton signUpBtn = new JButton("Create Account");
        signUpBtn.addActionListener(e -> signUp());
        panel.add(signUpBtn, position(gbc,0,3,2,1));

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        panel.add(statusLabel, position(gbc,0,4,2,1));

        add(panel);
    }

    private GridBagConstraints position(GridBagConstraints gbc, int x, int y) {
        return position(gbc,x,y,1,1);
    }

    private GridBagConstraints position(GridBagConstraints gbc, int x, int y, int w, int h) {
        gbc.gridx = x; gbc.gridy = y; gbc.gridwidth = w; gbc.gridheight = h;
        return gbc;
    }

    private void signUp() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT * FROM users WHERE email=?");
            check.setString(1, emailField.getText());
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                statusLabel.setText("❌ Email already exists.");
                return;
            }
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'Customer')");
            stmt.setString(1, nameField.getText());
            stmt.setString(2, emailField.getText());
            stmt.setString(3, String.valueOf(passwordField.getPassword()));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Account created. Please login.");
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("❌ DB error.");
        }
    }
}
