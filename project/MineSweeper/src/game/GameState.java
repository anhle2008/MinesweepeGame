package game;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    // List of cells that changed in this game state (for undo functionality)
    private final List<Cell> changedCells;

    // Game status flags
    private boolean gameOver;      // Whether the game has ended
    private boolean gameWon;       // Whether the player won the game
    private int flagsPlaced;       // Number of flags currently placed on the board
    private boolean firstMove;     // Whether this is the first move (before mines are placed)
    private String description;    // Description of what caused this game state change

    // Constructor to initialize a new game state
    public GameState() {
        this.changedCells = new ArrayList<>();
        this.gameOver = false;      // Game starts as not over
        this.gameWon = false;       // Game starts as not won
        this.flagsPlaced = 0;       // No flags placed initially
        this.firstMove = false;     // Not the first move by default
        this.description = "";      // Empty description initially
    }

    // Add a cell to the changed cells list (makes a copy of the cell)
    // This is used to track which cells were modified in this game state
    public void addChangedCell(Cell cell) {
        changedCells.add(new Cell(cell));  // Store a copy, not the original
    }

    // Getter and setter methods for game state properties
    public List<Cell> getChangedCells() { return changedCells; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public boolean isGameWon() { return gameWon; }
    public void setGameWon(boolean gameWon) { this.gameWon = gameWon; }
    public int getFlagsPlaced() { return flagsPlaced; }
    public void setFlagsPlaced(int flagsPlaced) { this.flagsPlaced = flagsPlaced; }
    public boolean isFirstMove() { return firstMove; }
    public void setFirstMove(boolean firstMove) { this.firstMove = firstMove; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}