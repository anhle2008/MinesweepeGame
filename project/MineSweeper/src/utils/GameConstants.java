package utils;

import java.awt.Color;

public class GameConstants {
    // Colors
    public static final Color COVERED_COLOR = new Color(192, 192, 192);
    public static final Color REVEALED_COLOR = new Color(220, 220, 220);

    // Game settings
    public static final int MIN_ROWS = 5;
    public static final int MAX_ROWS = 30;
    public static final int MIN_COLS = 5;
    public static final int MAX_COLS = 30;
    public static final int CELL_SIZE = 35;
    public static final int ICON_SIZE = 24;

    // Number colors (Minesweeper standard)
    public static Color getNumberColor(int adjacentMines) {
        return switch (adjacentMines) {
            case 1 -> new Color(0, 0, 255);     // Blue
            case 2 -> new Color(0, 128, 0);     // Green
            case 3 -> new Color(255, 0, 0);     // Red
            case 4 -> new Color(0, 0, 128);     // Navy
            case 5 -> new Color(128, 0, 0);     // Maroon
            case 6 -> new Color(0, 128, 128);   // Teal
            case 7 -> new Color(0, 0, 0);       // Black
            case 8 -> new Color(128, 128, 128); // Gray
            default -> Color.BLACK;
        };
    }
}