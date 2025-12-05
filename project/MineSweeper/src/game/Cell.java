package game;

public class Cell {
    private final int row;
    private final int col;
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;

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

    // Getters
    public boolean isMine() { return isMine; }
    public boolean isRevealed() { return isRevealed; }
    public boolean isFlagged() { return isFlagged; }
    public int getAdjacentMines() { return adjacentMines; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    // Setters (only what's needed)
    public void setMine(boolean mine) { isMine = mine; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }
    public void setAdjacentMines(int adjacentMines) { this.adjacentMines = adjacentMines; }
}