package game;

import java.util.*;

public class Board {
    // 2D array to store all cells in the game board
    private Cell[][] grid;
    private final int rows;          // Number of rows in the board
    private final int cols;          // Number of columns in the board
    private final int totalMines;    // Total number of mines to place

    // Constructor to initialize the board with given dimensions and mine count
    public Board(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = mines;
        initializeGrid();  // Set up the grid with empty cells
    }

    // Initialize the grid with Cell objects at each position
    private void initializeGrid() {
        grid = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(i, j);  // Create a new cell at position (i,j)
            }
        }
    }

    // Place mines randomly on the board, avoiding a safe area around the first click
    public void placeMines(int safeRow, int safeCol, Random random) {
        // Get all positions around the safe cell plus the safe cell itself
        List<int[]> safeZones = getAdjacentPositions(safeRow, safeCol);
        safeZones.add(new int[]{safeRow, safeCol});

        int minesPlaced = 0;
        // Keep placing mines until we reach the total number of mines
        while (minesPlaced < totalMines) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);

            // Only place mine if it's not in a safe zone and not already a mine
            if (!isSafeZone(row, col, safeZones) && !grid[row][col].isMine()) {
                grid[row][col].setMine(true);
                minesPlaced++;
            }
        }
        calculateAdjacentMines();  // Update adjacent mine counts for all cells
    }

    // Check if a position is in the list of safe zones
    private boolean isSafeZone(int row, int col, List<int[]> safeZones) {
        for (int[] safeZone : safeZones) {
            if (safeZone[0] == row && safeZone[1] == col) {
                return true;
            }
        }
        return false;
    }

    // Calculate and set the number of adjacent mines for each non-mine cell
    private void calculateAdjacentMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!grid[i][j].isMine()) {
                    int count = countAdjacentMines(i, j);
                    grid[i][j].setAdjacentMines(count);
                }
            }
        }
    }

    // Count how many mines are adjacent to a given cell (8-directional check)
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        // Check all 8 neighboring positions
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;  // Skip the cell itself
                int newRow = row + i;
                int newCol = col + j;
                // If neighbor is valid and has a mine, increment count
                if (isValidPosition(newRow, newCol) && grid[newRow][newCol].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    // Get all cells that should be revealed when a cell is clicked (BFS algorithm)
    public List<int[]> getCellsToReveal(int row, int col, GameState state) {
        List<int[]> cellsToReveal = new ArrayList<>();
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[rows][cols];

        // Start BFS from the clicked cell if it's valid and not already revealed/flagged
        if (isValidPosition(row, col) && !grid[row][col].isRevealed() && !grid[row][col].isFlagged()) {
            queue.add(new int[]{row, col});
            visited[row][col] = true;
        }

        // Process all cells in the queue
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];

            // Save cell state for undo functionality
            state.addChangedCell(grid[r][c]);
            cellsToReveal.add(new int[]{r, c});

            // If cell has 0 adjacent mines, add all its unrevealed neighbors to queue
            if (grid[r][c].getAdjacentMines() == 0) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        int newRow = r + i;
                        int newCol = c + j;

                        // Add neighbor if valid, not visited, not revealed, and not flagged
                        if (isValidPosition(newRow, newCol) &&
                                !visited[newRow][newCol] &&
                                !grid[newRow][newCol].isRevealed() &&
                                !grid[newRow][newCol].isFlagged()) {

                            queue.add(new int[]{newRow, newCol});
                            visited[newRow][newCol] = true;
                        }
                    }
                }
            }
        }

        return cellsToReveal;
    }

    // Get all adjacent positions (8 directions) around a given cell
    private List<int[]> getAdjacentPositions(int row, int col) {
        List<int[]> positions = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;  // Skip the cell itself
                int newRow = row + i;
                int newCol = col + j;
                if (isValidPosition(newRow, newCol)) {
                    positions.add(new int[]{newRow, newCol});
                }
            }
        }
        return positions;
    }

    // Check if a position is within the board boundaries
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // Getters for board properties
    public Cell getCell(int row, int col) {
        if (!isValidPosition(row, col)) {
            throw new IllegalArgumentException("Invalid cell coordinates");
        }
        return grid[row][col];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getTotalMines() { return totalMines; }
}