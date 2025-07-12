package main;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignUpScreen extends JFrame {
    private JTextField nameField, emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public SignUpScreen() {
        setTitle("✍️ Sign Up - BookMart Online");
        setSize(400, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
        gbc.gridx = x; gbc.gridy = y;
        gbc.gridwidth = w; gbc.gridheight = h;
        return gbc;
    }

    private void signUp() {
        String name = nameField.getText();
        String email = emailField.getText();
        String pass = String.valueOf(passwordField.getPassword());

        if(name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("❌ Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                statusLabel.setText("❌ DB connection failed.");
                return;
            }

            // Check if email already exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE email=?");
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                statusLabel.setText("❌ Email already registered.");
                return;
            }

            // Insert new customer
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'Customer')");
            insertStmt.setString(1, name);
            insertStmt.setString(2, email);
            insertStmt.setString(3, pass);
            insertStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Account created! Please log in.");
            this.dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("❌ DB error.");
        }
    }
}
