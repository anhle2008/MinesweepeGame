package game;

public class Cell {
    // Cell position on the board
    private final int row;
    private final int col;

    // Cell state flags
    private boolean isMine;           // Whether this cell contains a mine
    private boolean isRevealed;       // Whether this cell has been revealed/clicked
    private boolean isFlagged;        // Whether this cell has a flag placed on it
    private int adjacentMines;        // Number of mines in adjacent cells (0-8)

    // Constructor to create a new cell at specified position
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.isMine = false;           // Default: not a mine
        this.isRevealed = false;       // Default: not revealed
        this.isFlagged = false;        // Default: not flagged
        this.adjacentMines = 0;        // Default: no adjacent mines
    }

    // Copy constructor - creates a deep copy of another cell
    // Used for saving/restoring game state (undo functionality)
    public Cell(Cell other) {
        this.row = other.row;
        this.col = other.col;
        this.isMine = other.isMine;
        this.isRevealed = other.isRevealed;
        this.isFlagged = other.isFlagged;
        this.adjacentMines = other.adjacentMines;
    }

    // Getter methods - provide read-only access to cell properties
    public boolean isMine() { return isMine; }
    public boolean isRevealed() { return isRevealed; }
    public boolean isFlagged() { return isFlagged; }
    public int getAdjacentMines() { return adjacentMines; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    // Setter methods - allow controlled modification of cell properties
    public void setMine(boolean mine) { isMine = mine; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }
    public void setAdjacentMines(int adjacentMines) { this.adjacentMines = adjacentMines; }
}