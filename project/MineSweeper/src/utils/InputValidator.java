package utils;

public class InputValidator {
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

    public static boolean isValidCustomValue(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static int getMaxMines(int rows, int cols) {
        return (rows * cols) / 3;
    }
}