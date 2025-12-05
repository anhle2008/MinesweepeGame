package game;

import java.util.Random;
import java.util.Stack;
import java.util.List;

public class MinesweeperGame {
    // Core game components
    private Board board;           // The game board containing all cells
    private int flagsPlaced;       // Number of flags currently on the board
    private boolean gameOver;      // Whether the game has ended
    private boolean gameWon;       // Whether the player won
    private Stack<GameState> undoStack;  // Stack for undo functionality (LIFO)
    private boolean firstMove;     // Track if it's the first move (mines not placed yet)

    // Track last mine clicked for special undo case
    private int lastMineRow = -1;
    private int lastMineCol = -1;

    // Constructor to initialize a new game
    public MinesweeperGame(int rows, int cols, int mines) {
        this.board = new Board(rows, cols, mines);
        this.flagsPlaced = 0;
        this.gameOver = false;
        this.gameWon = false;
        this.undoStack = new Stack<>();
        this.firstMove = true;  // Mines will be placed on first click
    }

    // Handle revealing a cell (left click)
    public synchronized boolean revealCell(int row, int col) {
        // Check if reveal is allowed
        if (gameOver || !board.isValidPosition(row, col) ||
                board.getCell(row, col).isRevealed() ||
                board.getCell(row, col).isFlagged()) {
            return false;
        }

        // If cell is not a mine (safe cell)
        if (!board.getCell(row, col).isMine()) {
            GameState state = createGameState("Reveal cell at (" + row + ", " + col + ")");

            // If this is the first move, place mines after saving initial state
            if (firstMove) {
                saveBoardState(state);     // Save entire board state
                pushToUndoStack(state);    // Save to undo stack

                // Place mines randomly, avoiding the clicked cell and its neighbors
                board.placeMines(row, col, new Random());
                firstMove = false;         // Mines are now placed

                state = createGameState("First move - mines placed");
            }

            // Get all cells that should be revealed (BFS for empty cells)
            List<int[]> cellsToReveal = board.getCellsToReveal(row, col, state);

            // Reveal all cells in the list
            for (int[] pos : cellsToReveal) {
                board.getCell(pos[0], pos[1]).setRevealed(true);
            }

            pushToUndoStack(state);  // Save this game state for undo
            checkWinCondition();     // Check if the player has won
            return true;
        } else {
            // Player clicked on a mine - game over
            this.lastMineRow = row;
            this.lastMineCol = col;
            board.getCell(row, col).setRevealed(true);
            gameOver = true;
            return true;
        }
    }

    // Handle toggling a flag on/off (right click)
    public synchronized boolean toggleFlag(int row, int col) {
        // Check if flag toggle is allowed
        if (gameOver || !board.isValidPosition(row, col) ||
                board.getCell(row, col).isRevealed()) {
            return false;
        }

        // Prevent placing more flags than total mines
        if (!board.getCell(row, col).isFlagged() && flagsPlaced >= board.getTotalMines()) {
            return false;
        }

        // Create game state for undo
        GameState state = createGameState("Toggle flag at (" + row + ", " + col + ")");
        state.addChangedCell(board.getCell(row, col));

        // Toggle flag state
        if (board.getCell(row, col).isFlagged()) {
            board.getCell(row, col).setFlagged(false);
            flagsPlaced--;
        } else {
            board.getCell(row, col).setFlagged(true);
            flagsPlaced++;
        }

        pushToUndoStack(state);  // Save for undo
        checkWinCondition();     // Check if flagging completed the game
        return true;
    }

    // Undo the last move
    public synchronized boolean undo() {
        // Special case: undo clicking on a mine
        if (gameOver && !gameWon && lastMineRow != -1) {
            board.getCell(lastMineRow, lastMineCol).setRevealed(false);
            gameOver = false;
            lastMineRow = -1;
            lastMineCol = -1;
            return true;
        }

        // Regular undo: restore previous game state from stack
        if (undoStack.isEmpty()) {
            return false;
        }

        GameState state = undoStack.pop();
        restoreGameState(state);
        return true;
    }

    // Save the entire board state (used for first move undo)
    private void saveBoardState(GameState state) {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                state.addChangedCell(new Cell(board.getCell(i, j)));
            }
        }
    }

    // Restore game state from a saved state
    private void restoreGameState(GameState state) {
        // Restore game status flags
        this.gameOver = state.isGameOver();
        this.gameWon = state.isGameWon();
        this.flagsPlaced = state.getFlagsPlaced();
        this.firstMove = state.isFirstMove();

        // Restore each cell to its saved state
        for (Cell savedCell : state.getChangedCells()) {
            Cell currentCell = board.getCell(savedCell.getRow(), savedCell.getCol());
            currentCell.setRevealed(savedCell.isRevealed());
            currentCell.setFlagged(savedCell.isFlagged());
            currentCell.setMine(savedCell.isMine());
            currentCell.setAdjacentMines(savedCell.getAdjacentMines());
        }
    }

    // Check if the player has won the game
    private void checkWinCondition() {
        boolean allNonMinesRevealed = true;
        boolean allMinesCorrectlyFlagged = true;
        boolean noIncorrectFlags = true;

        // Check all cells on the board
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell cell = board.getCell(i, j);

                if (cell.isMine()) {
                    // Mine must be flagged for win condition
                    if (!cell.isFlagged()) {
                        allMinesCorrectlyFlagged = false;
                    }
                } else {
                    // Non-mine must be revealed for win condition
                    if (!cell.isRevealed()) {
                        allNonMinesRevealed = false;
                    }
                    // Check for incorrect flags on non-mines
                    if (cell.isFlagged()) {
                        noIncorrectFlags = false;
                    }
                }
            }
        }

        // Win if either:
        // 1. All non-mines are revealed (standard Minesweeper win)
        // 2. All mines are flagged AND no incorrect flags (flagging win)
        gameWon = allNonMinesRevealed || (allMinesCorrectlyFlagged && noIncorrectFlags);
        gameOver = gameWon;  // Game ends when player wins
    }

    // Create a new game state object with current game status
    private GameState createGameState(String description) {
        GameState state = new GameState();
        state.setGameOver(gameOver);
        state.setGameWon(gameWon);
        state.setFlagsPlaced(flagsPlaced);
        state.setFirstMove(firstMove);
        state.setDescription(description);
        return state;
    }

    // Save a game state to the undo stack
    private void pushToUndoStack(GameState state) {
        undoStack.push(state);
    }

    // Getter methods for game information
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public int getRows() { return board.getRows(); }
    public int getCols() { return board.getCols(); }
    public Cell getCell(int row, int col) { return board.getCell(row, col); }
    public int getFlagsPlaced() { return flagsPlaced; }
    public int getTotalMines() { return board.getTotalMines(); }
    public int getUndoCount() { return undoStack.size(); }

    // Reset game with new dimensions
    public void resetGame(int rows, int cols, int mines) {
        this.board = new Board(rows, cols, mines);
        this.flagsPlaced = 0;
        this.gameOver = false;
        this.gameWon = false;
        this.undoStack.clear();
        this.firstMove = true;
        this.lastMineRow = -1;
        this.lastMineCol = -1;
    }

    // Reset game with current dimensions
    public void resetGame() {
        resetGame(board.getRows(), board.getCols(), board.getTotalMines());
    }
}