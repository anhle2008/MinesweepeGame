package game;

import java.util.*;

public class Board {
    private Cell[][] grid;
    private final int rows;
    private final int cols;
    private final int totalMines;

    public Board(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = mines;
        initializeGrid();
    }

    private void initializeGrid() {
        grid = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }
    }

    public void placeMines(int safeRow, int safeCol, Random random) {
        List<int[]> safeZones = getAdjacentPositions(safeRow, safeCol);
        safeZones.add(new int[]{safeRow, safeCol});

        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);

            if (!isSafeZone(row, col, safeZones) && !grid[row][col].isMine()) {
                grid[row][col].setMine(true);
                minesPlaced++;
            }
        }
        calculateAdjacentMines();
    }

    private boolean isSafeZone(int row, int col, List<int[]> safeZones) {
        for (int[] safeZone : safeZones) {
            if (safeZone[0] == row && safeZone[1] == col) {
                return true;
            }
        }
        return false;
    }

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

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int newRow = row + i;
                int newCol = col + j;
                if (isValidPosition(newRow, newCol) && grid[newRow][newCol].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<int[]> getCellsToReveal(int row, int col, GameState state) {
        List<int[]> cellsToReveal = new ArrayList<>();
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[rows][cols];

        if (isValidPosition(row, col) && !grid[row][col].isRevealed() && !grid[row][col].isFlagged()) {
            queue.add(new int[]{row, col});
            visited[row][col] = true;
        }

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];

            // Save state before revealing
            state.addChangedCell(grid[r][c]);
            cellsToReveal.add(new int[]{r, c});

            // If this cell has 0 adjacent mines, add its neighbors
            if (grid[r][c].getAdjacentMines() == 0) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        int newRow = r + i;
                        int newCol = c + j;

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

    private List<int[]> getAdjacentPositions(int row, int col) {
        List<int[]> positions = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int newRow = row + i;
                int newCol = col + j;
                if (isValidPosition(newRow, newCol)) {
                    positions.add(new int[]{newRow, newCol});
                }
            }
        }
        return positions;
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // Getters
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