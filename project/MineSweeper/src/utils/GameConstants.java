package utils;

import java.awt.Color;

public class GameConstants {
    // Colors for the game UI
    public static final Color COVERED_COLOR = new Color(192, 192, 192);    // Light gray for covered cells
    public static final Color REVEALED_COLOR = new Color(220, 220, 220);   // Slightly darker gray for revealed cells

    // Game settings - limits for custom games
    public static final int MIN_ROWS = 5;    // Minimum allowed rows
    public static final int MAX_ROWS = 30;   // Maximum allowed rows
    public static final int MIN_COLS = 5;    // Minimum allowed columns
    public static final int MAX_COLS = 30;   // Maximum allowed columns
    public static final int CELL_SIZE = 35;  // Size of each cell in pixels
    public static final int ICON_SIZE = 24;  // Size of icons (flags, mines) in pixels

    // Return the standard Minesweeper color for each number
    // Each number 1-8 has a specific color in traditional Minesweeper
    public static Color getNumberColor(int adjacentMines) {
        return switch (adjacentMines) {
            case 1 -> new Color(0, 0, 255);     // Blue for 1
            case 2 -> new Color(0, 128, 0);     // Green for 2
            case 3 -> new Color(255, 0, 0);     // Red for 3
            case 4 -> new Color(0, 0, 128);     // Navy blue for 4
            case 5 -> new Color(128, 0, 0);     // Maroon for 5
            case 6 -> new Color(0, 128, 128);   // Teal for 6
            case 7 -> new Color(0, 0, 0);       // Black for 7
            case 8 -> new Color(128, 128, 128); // Gray for 8
            default -> Color.BLACK;             // Default black for other values
        };
    }
}