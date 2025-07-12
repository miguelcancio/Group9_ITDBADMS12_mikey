package main;

import javax.swing.*;
import java.awt.*;

public class Cart extends JFrame {
    public Cart() {
        setTitle("🛒 Your Cart - BookMart Online");
        setSize(400,300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel info = new JLabel("<html>Your cart would show selected books here.<br>Confirm & place order via stored procedure.</html>", SwingConstants.CENTER);
        add(info, BorderLayout.CENTER);

        JButton orderBtn = new JButton("✅ Place Order");
        orderBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Order placed! (link to SP later)"));
        add(orderBtn, BorderLayout.SOUTH);
    }
}
