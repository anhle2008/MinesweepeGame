public class Cell {
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;
    private int row;
    private int col;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }

    // Copy constructor
    public Cell(Cell other) {
        this.row = other.row;
        this.col = other.col;
        this.isMine = other.isMine;
        this.isRevealed = other.isRevealed;
        this.isFlagged = other.isFlagged;
        this.adjacentMines = other.adjacentMines;
    }

    // Getters and setters
    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { isMine = mine; }

    public boolean isRevealed() { return isRevealed; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public int getAdjacentMines() { return adjacentMines; }
    public void setAdjacentMines(int adjacentMines) { this.adjacentMines = adjacentMines; }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public String getDisplayText() {
        if (!isRevealed) {
            return ""; // No text for hidden cells
        }
        if (isMine) return ""; // No text for mines (we use images)
        return adjacentMines == 0 ? "" : String.valueOf(adjacentMines);
    }

    public int getDisplayColor() {
        if (!isRevealed) return -1; // Default color

        if (isMine) return 0xFF0000; // Red for mines

        return switch (adjacentMines) {
            case 1 -> 0x0000FF; // Blue
            case 2 -> 0x008000; // Green
            case 3 -> 0xFF0000; // Red
            case 4 -> 0x800080; // Purple
            case 5 -> 0x800000; // Maroon
            case 6 -> 0x008080; // Teal
            case 7 -> 0x000000; // Black
            case 8 -> 0x808080; // Gray
            default -> 0x000000; // Black
        };
    }

    @Override
    public String toString() {
        return String.format("Cell[%d,%d mine=%s revealed=%s flagged=%s adjacent=%d]",
                row, col, isMine, isRevealed, isFlagged, adjacentMines);
    }
}