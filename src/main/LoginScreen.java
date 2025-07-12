package main;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginScreen extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginScreen() {
        setTitle("📚 BookMart Online - Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(new JLabel("Email:"), position(gbc,0,0));
        emailField = new JTextField(20);
        panel.add(emailField, position(gbc,1,0));

        panel.add(new JLabel("Password:"), position(gbc,0,1));
        passwordField = new JPasswordField(20);
        panel.add(passwordField, position(gbc,1,1));

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> login());
        panel.add(loginBtn, position(gbc,0,2));

        JButton signUpBtn = new JButton("Sign Up");
        signUpBtn.addActionListener(e -> new SignUpScreen().setVisible(true));
        panel.add(signUpBtn, position(gbc,1,2));

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        panel.add(statusLabel, position(gbc,0,3,2,1));

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

    private void login() {
        String email = emailField.getText();
        String pass = String.valueOf(passwordField.getPassword());

        if(email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("❌ Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                statusLabel.setText("❌ Cannot connect to DB.");
                return;
            }
            PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE email=? AND password=?");
            stmt.setString(1, email);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                openRoleDashboard(role);
                dispose();
            } else {
                statusLabel.setText("❌ Wrong email or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("❌ DB error.");
        }
    }

    private void openRoleDashboard(String role) {
        switch(role.toLowerCase()) {
            case "admin":
                new AdminPanel().setVisible(true); break;
            case "staff":
                new StaffPanel().setVisible(true); break;
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
