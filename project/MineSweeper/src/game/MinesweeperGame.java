package game;

import java.util.Random;
import java.util.Stack;
import java.util.List;

public class MinesweeperGame {
    private Board board;
    private int flagsPlaced;
    private boolean gameOver;
    private boolean gameWon;
    private Stack<GameState> undoStack;
    private boolean firstMove;
    private int lastMineRow = -1;
    private int lastMineCol = -1;

    public MinesweeperGame(int rows, int cols, int mines) {
        this.board = new Board(rows, cols, mines);
        this.flagsPlaced = 0;
        this.gameOver = false;
        this.gameWon = false;
        this.undoStack = new Stack<>();
        this.firstMove = true;
    }

    public synchronized boolean revealCell(int row, int col) {
        if (gameOver || !board.isValidPosition(row, col) ||
                board.getCell(row, col).isRevealed() ||
                board.getCell(row, col).isFlagged()) {
            return false;
        }

        if (!board.getCell(row, col).isMine()) {
            GameState state = createGameState("Reveal cell at (" + row + ", " + col + ")");

            if (firstMove) {
                saveBoardState(state);
                pushToUndoStack(state);

                board.placeMines(row, col, new Random());
                firstMove = false;

                state = createGameState("First move - mines placed");
            }

            // Reveal cells
            List<int[]> cellsToReveal = board.getCellsToReveal(row, col, state);

            for (int[] pos : cellsToReveal) {
                board.getCell(pos[0], pos[1]).setRevealed(true);
            }

            pushToUndoStack(state);
            checkWinCondition();
            return true;
        } else {
            // Mine clicked
            this.lastMineRow = row;
            this.lastMineCol = col;
            board.getCell(row, col).setRevealed(true);
            gameOver = true;
            return true;
        }
    }

    public synchronized boolean toggleFlag(int row, int col) {
        if (gameOver || !board.isValidPosition(row, col) ||
                board.getCell(row, col).isRevealed()) {
            return false;
        }

        // Prevent placing more flags than total mines
        if (!board.getCell(row, col).isFlagged() && flagsPlaced >= board.getTotalMines()) {
            return false;
        }

        GameState state = createGameState("Toggle flag at (" + row + ", " + col + ")");
        state.addChangedCell(board.getCell(row, col));

        if (board.getCell(row, col).isFlagged()) {
            board.getCell(row, col).setFlagged(false);
            flagsPlaced--;
        } else {
            board.getCell(row, col).setFlagged(true);
            flagsPlaced++;
        }

        pushToUndoStack(state);
        checkWinCondition();
        return true;
    }

    public synchronized boolean undo() {
        if (gameOver && !gameWon && lastMineRow != -1) {
            // Special undo for mine click
            board.getCell(lastMineRow, lastMineCol).setRevealed(false);
            gameOver = false;
            lastMineRow = -1;
            lastMineCol = -1;
            return true;
        }

        if (undoStack.isEmpty()) {
            return false;
        }

        GameState state = undoStack.pop();
        restoreGameState(state);
        return true;
    }

    private void saveBoardState(GameState state) {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                state.addChangedCell(new Cell(board.getCell(i, j)));
            }
        }
    }

    private void restoreGameState(GameState state) {
        this.gameOver = state.isGameOver();
        this.gameWon = state.isGameWon();
        this.flagsPlaced = state.getFlagsPlaced();
        this.firstMove = state.isFirstMove();

        for (Cell savedCell : state.getChangedCells()) {
            Cell currentCell = board.getCell(savedCell.getRow(), savedCell.getCol());
            currentCell.setRevealed(savedCell.isRevealed());
            currentCell.setFlagged(savedCell.isFlagged());
            currentCell.setMine(savedCell.isMine());
            currentCell.setAdjacentMines(savedCell.getAdjacentMines());
        }
    }

    private void checkWinCondition() {
        boolean allNonMinesRevealed = true;
        boolean allMinesCorrectlyFlagged = true;
        boolean noIncorrectFlags = true;

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell cell = board.getCell(i, j);

                if (cell.isMine()) {
                    // Mine must be flagged
                    if (!cell.isFlagged()) {
                        allMinesCorrectlyFlagged = false;
                    }
                } else {
                    // Non-mine must be revealed
                    if (!cell.isRevealed()) {
                        allNonMinesRevealed = false;
                    }
                    // Check for incorrect flag
                    if (cell.isFlagged()) {
                        noIncorrectFlags = false;
                    }
                }
            }
        }

        // Win if:
        // 1. All non-mines are revealed (standard win by revealing)
        // OR
        // 2. All mines are flagged AND no incorrect flags (win by flagging)
        gameWon = allNonMinesRevealed || (allMinesCorrectlyFlagged && noIncorrectFlags);
        gameOver = gameWon;
    }

    private GameState createGameState(String description) {
        GameState state = new GameState();
        state.setGameOver(gameOver);
        state.setGameWon(gameWon);
        state.setFlagsPlaced(flagsPlaced);
        state.setFirstMove(firstMove);
        state.setDescription(description);
        return state;
    }

    private void pushToUndoStack(GameState state) {
        undoStack.push(state);
    }

    // Getters
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public int getRows() { return board.getRows(); }
    public int getCols() { return board.getCols(); }
    public Cell getCell(int row, int col) { return board.getCell(row, col); }
    public int getFlagsPlaced() { return flagsPlaced; }
    public int getTotalMines() { return board.getTotalMines(); }
    public int getUndoCount() { return undoStack.size(); }

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

    public void resetGame() {
        resetGame(board.getRows(), board.getCols(), board.getTotalMines());
    }
}