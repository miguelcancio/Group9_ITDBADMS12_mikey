package main;

import java.awt.*;
import java.io.*;
import java.util.HashMap;

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
            String[] parts = val.split("-");
            String name = parts[0];
            int style = Font.PLAIN;
            if (parts.length == 3 && parts[1].equalsIgnoreCase("BOLD")) style = Font.BOLD;
            int size = Integer.parseInt(parts[parts.length - 1]);
            return new Font(name, style, size);
        }
        return new Font("SansSerif", Font.PLAIN, 12); // fallback
    }

}
