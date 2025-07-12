package main;

import javax.swing.*;
import java.awt.*;

public class OrderHistory extends JFrame {
    public OrderHistory() {
        setTitle("📝 Order History - BookMart");
        setSize(500,400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel historyLabel = new JLabel("<html>This will list:<br>"
                + "Order ID, Date, Total, Status<br>"
                + "Expand to show books per order.</html>", SwingConstants.CENTER);
        add(historyLabel, BorderLayout.CENTER);
    }
}
