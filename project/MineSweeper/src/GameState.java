import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<Cell> changedCells;
    private boolean gameOver;
    private boolean gameWon;
    private int flagsPlaced;
    private boolean firstMove;
    private String description;

    public GameState() {
        this.changedCells = new ArrayList<>();
        this.gameOver = false;
        this.gameWon = false;
        this.flagsPlaced = 0;
        this.firstMove = false;
        this.description = "";
    }

    public void addChangedCell(Cell cell) {
        // Create a copy of the cell to preserve its state
        Cell copy = new Cell(cell);
        changedCells.add(copy);
    }

    // Getters and setters
    public List<Cell> getChangedCells() {
        return changedCells;
    }

    public void setChangedCells(List<Cell> changedCells) {
        this.changedCells = changedCells;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public int getFlagsPlaced() {
        return flagsPlaced;
    }

    public void setFlagsPlaced(int flagsPlaced) {
        this.flagsPlaced = flagsPlaced;
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "changedCells=" + changedCells.size() +
                ", gameOver=" + gameOver +
                ", gameWon=" + gameWon +
                ", flagsPlaced=" + flagsPlaced +
                ", firstMove=" + firstMove +
                ", description='" + description + '\'' +
                '}';
    }
}