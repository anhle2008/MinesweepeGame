package utils;

public class InputValidator {
    // Validate game dimensions and mine count
    public static void validateDimensions(int rows, int cols, int mines) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and columns must be positive");
        }
        if (mines <= 0) {
            throw new IllegalArgumentException("Number of mines must be positive");
        }
        if (mines >= rows * cols) {
            throw new IllegalArgumentException("Too many mines for board size");
        }
    }

    // Check if a custom value is within valid range
    public static boolean isValidCustomValue(int value, int min, int max) {
        return value >= min && value <= max;
    }

    // Calculate maximum allowed mines for a given board size
    // Uses 1/3 of total cells as maximum to ensure playable game
    public static int getMaxMines(int rows, int cols) {
        return (rows * cols) / 3;
    }
}