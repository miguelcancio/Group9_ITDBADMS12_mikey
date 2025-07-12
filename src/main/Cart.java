package main;

import javax.swing.*;
import java.awt.*;

public class Cart extends JFrame {
    public Cart() {
        setTitle("🛒 Your Cart - BookMart");
        setSize(400,300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel info = new JLabel("<html>Your selected books<br>and checkout will appear here.<br>Future: use SP to place orders & show history.</html>", SwingConstants.CENTER);
        add(info, BorderLayout.CENTER);

        JButton orderBtn = new JButton("✅ Place Order");
        orderBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Order placed (SP link later)."));
        add(orderBtn, BorderLayout.SOUTH);
    }
}
