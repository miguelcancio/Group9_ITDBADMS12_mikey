package main;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.Border;

public class StyleLoader {
    private HashMap<String, String> styles = new HashMap<>();

    public StyleLoader(String filepath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        styles.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Color getColor(String key) {
        String hex = styles.get(key);
        if (hex != null && hex.startsWith("#")) {
            return Color.decode(hex);
        }
        return null;
    }

    public Font getFont(String key) {
        String val = styles.get(key);
        if (val != null) {
            String name = "Segoe UI";
            int style = Font.PLAIN;
            int size = 12;

            if (val.contains("bold")) style = Font.BOLD;
            if (val.contains("font-size:")) {
                try {
                    size = Integer.parseInt(val.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    size = 12;
                }
            }

            return new Font(name, style, size);
        }
        return new Font("SansSerif", Font.PLAIN, 12);
    }

    public Border getRoundedBorder(int radius) {
        return new javax.swing.border.AbstractBorder() {
            public Insets getBorderInsets(Component c) {
                return new Insets(8, 20, 8, 20);
            }

            public boolean isBorderOpaque() {
                return false;
            }

            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(c.getBackground());
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            }
        };
    }

    public void applyStyle(JButton button, String styleName) {
        if (styleName.equals("round")) {
            button.setBackground(getColor("button.round.background-color"));
            button.setForeground(getColor("button.round.color"));
            button.setFont(getFont("button.round"));

            button.setBorder(getRoundedBorder(30));
            button.setFocusPainted(false);



            button.setMargin(new Insets(8, 20, 8, 20));
        }
    }

}