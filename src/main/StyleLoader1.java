package main;

import java.awt.*;
import javax.swing.*;

public class StyleLoader1 {
    public static final Color PRIMARY_COLOR = Color.decode("#003059");
    public static final Color BG_COLOR = Color.decode("#f4f6f8");
    public static final Color CARD_COLOR = Color.white;
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);

    public static JButton styleButton(JButton btn) {
        btn.setBackground(new Color(33, 150, 243));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }


    public static JPanel cardWrap(JPanel card) {
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        return card;
    }
}
