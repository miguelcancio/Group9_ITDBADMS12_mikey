package main;

import javax.swing.*;
import java.awt.*;

public class StaffPanel extends JFrame {
    public StaffPanel() {
        setTitle("👨‍💼 Staff Dashboard - BookMart");
        setSize(400,300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("<html>Staff can view orders<br>and assist customers here.</html>", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }
}
