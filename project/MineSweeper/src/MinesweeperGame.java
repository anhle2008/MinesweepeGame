import java.util.*;

public class MinesweeperGame {
    private Cell[][] board;
    private int rows;
    private int cols;
    private int totalMines;
    private int flagsPlaced;
    private boolean gameOver;
    private boolean gameWon;
    private Stack<GameState> undoStack;
    private boolean firstMove;
    private int lastMineRow = -1;
    private int lastMineCol = -1;

    public MinesweeperGame(int rows, int cols, int mines) {
        validateDimensions(rows, cols, mines);
        this.rows = rows;
        this.cols = cols;
        this.totalMines = mines;
        this.flagsPlaced = 0;
        this.gameOver = false;
        this.gameWon = false;
        this.undoStack = new Stack<>();
        this.firstMove = true;
        this.lastMineRow = -1;
        this.lastMineCol = -1;
        initializeBoard();
    }

    private void validateDimensions(int rows, int cols, int mines) {
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

    private void initializeBoard() {
        board = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new Cell(i, j);
            }
        }
    }

    public synchronized void placeMines(int firstClickRow, int firstClickCol) {
        validateCoordinates(firstClickRow, firstClickCol);

        Random random = new Random();
        int minesPlaced = 0;

        // Ensure first click is safe
        List<int[]> safeZones = getAdjacentPositions(firstClickRow, firstClickCol);
        safeZones.add(new int[]{firstClickRow, firstClickCol});

        while (minesPlaced < totalMines) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);

            // Check if this position is a safe zone
            boolean isSafeZone = false;
            for (int[] safeZone : safeZones) {
                if (safeZone[0] == row && safeZone[1] == col) {
                    isSafeZone = true;
                    break;
                }
            }

            if (!isSafeZone && !board[row][col].isMine()) {
                board[row][col].setMine(true);
                minesPlaced++;
            }
        }

        // Calculate adjacent mines for all cells
        calculateAdjacentMines();
    }

    private void calculateAdjacentMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isMine()) {
                    int count = countAdjacentMines(i, j);
                    board[i][j].setAdjacentMines(count);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int[] pos : getAdjacentPositions(row, col)) {
            if (board[pos[0]][pos[1]].isMine()) {
                count++;
            }
        }
        return count;
    }

    private List<int[]> getAdjacentPositions(int row, int col) {
        List<int[]> positions = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    positions.add(new int[]{newRow, newCol});
                }
            }
        }
        return positions;
    }

    public synchronized boolean revealCell(int row, int col) {
        if (gameOver || !validateCoordinates(row, col) ||
                board[row][col].isRevealed() || board[row][col].isFlagged()) {
            return false;
        }

        // Save state BEFORE any changes for safe moves
        if (!board[row][col].isMine()) {
            GameState state = new GameState();
            state.setGameOver(false);
            state.setGameWon(false);
            state.setFlagsPlaced(flagsPlaced);
            state.setFirstMove(firstMove);
            state.setDescription("Reveal cell at (" + row + ", " + col + ")");

            if (firstMove) {
                // For first move, save the initial empty board state
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        state.addChangedCell(new Cell(board[i][j]));
                    }
                }
                pushToUndoStack(state);

                placeMines(row, col);
                firstMove = false;

                // Create new state after mine placement
                state = new GameState();
                state.setGameOver(false);
                state.setGameWon(false);
                state.setFlagsPlaced(flagsPlaced);
                state.setFirstMove(firstMove);
                state.setDescription("First move - mines placed");
            }

            // Save current state of all cells that will be changed
            revealCellsRecursive(row, col, state);
            pushToUndoStack(state);
            checkWinCondition();
            return true;
        } else {
            // Mine clicked - track the mine location but don't save state
            this.lastMineRow = row;
            this.lastMineCol = col;
            board[row][col].setRevealed(true);
            gameOver = true;
            return true;
        }
    }

    private void revealCellsRecursive(int row, int col, GameState state) {
        if (!validateCoordinates(row, col) ||
                board[row][col].isRevealed() || board[row][col].isFlagged()) {
            return;
        }

        // Save the cell state BEFORE revealing it
        state.addChangedCell(new Cell(board[row][col]));

        board[row][col].setRevealed(true);

        if (board[row][col].getAdjacentMines() == 0) {
            for (int[] pos : getAdjacentPositions(row, col)) {
                revealCellsRecursive(pos[0], pos[1], state);
            }
        }
    }

    public synchronized boolean toggleFlag(int row, int col) {
        if (gameOver || !validateCoordinates(row, col) || board[row][col].isRevealed()) {
            return false;
        }

        // Save state before making changes
        GameState state = new GameState();
        state.setGameOver(gameOver);
        state.setGameWon(gameWon);
        state.setFlagsPlaced(flagsPlaced);
        state.setFirstMove(firstMove);
        state.setDescription("Toggle flag at (" + row + ", " + col + ")");

        // Save the cell that will be changed
        state.addChangedCell(new Cell(board[row][col]));

        if (board[row][col].isFlagged()) {
            board[row][col].setFlagged(false);
            flagsPlaced--;
        } else {
            board[row][col].setFlagged(true);
            flagsPlaced++;
        }

        pushToUndoStack(state);
        checkWinCondition();
        return true;
    }

    public synchronized boolean undo() {
        // Special case: if game over was caused by a mine click, undo it
        if (gameOver && !gameWon && lastMineRow != -1 && lastMineCol != -1) {
            // Hide the mine and reset game state
            board[lastMineRow][lastMineCol].setRevealed(false);
            gameOver = false;
            lastMineRow = -1;
            lastMineCol = -1;
            return true;
        }

        // Normal case: pop from undo stack
        if (undoStack.isEmpty()) {
            return false;
        }

        GameState state = undoStack.pop();

        // Restore game state
        this.gameOver = state.isGameOver();
        this.gameWon = state.isGameWon();
        this.flagsPlaced = state.getFlagsPlaced();
        this.firstMove = state.isFirstMove();

        // Restore all changed cells to their previous state
        for (Cell savedCell : state.getChangedCells()) {
            Cell currentCell = board[savedCell.getRow()][savedCell.getCol()];
            currentCell.setRevealed(savedCell.isRevealed());
            currentCell.setFlagged(savedCell.isFlagged());
            currentCell.setMine(savedCell.isMine());
            currentCell.setAdjacentMines(savedCell.getAdjacentMines());
        }

        return true;
    }

    private void pushToUndoStack(GameState state) {
        undoStack.push(state);
    }

    private void checkWinCondition() {
        boolean allNonMinesRevealed = true;
        boolean allMinesFlagged = true;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = board[i][j];
                if (!cell.isMine() && !cell.isRevealed()) {
                    allNonMinesRevealed = false;
                }
                if (cell.isMine() && !cell.isFlagged()) {
                    allMinesFlagged = false;
                }
            }
        }

        gameWon = allNonMinesRevealed || allMinesFlagged;
        gameOver = gameWon;
    }

    private boolean validateCoordinates(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // Getters
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public Cell getCell(int row, int col) {
        if (!validateCoordinates(row, col)) {
            throw new IllegalArgumentException("Invalid cell coordinates: (" + row + ", " + col + ")");
        }
        return board[row][col];
    }
    public int getFlagsPlaced() { return flagsPlaced; }
    public int getTotalMines() { return totalMines; }
    public int getUndoCount() { return undoStack.size(); }

    // Reset game with new dimensions
    public void resetGame(int rows, int cols, int mines) {
        validateDimensions(rows, cols, mines);
        this.rows = rows;
        this.cols = cols;
        this.totalMines = mines;
        this.flagsPlaced = 0;
        this.gameOver = false;
        this.gameWon = false;
        this.undoStack.clear();
        this.firstMove = true;
        this.lastMineRow = -1;
        this.lastMineCol = -1;
        initializeBoard();
    }

    // Reset game with current dimensions
    public void resetGame() {
        resetGame(this.rows, this.cols, this.totalMines);
    }
}