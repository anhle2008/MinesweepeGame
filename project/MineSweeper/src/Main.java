import javax.swing.*;

public class Main {
    private MinesweeperGame game;
    private MinesweeperGUI gui;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::initializeAndStart);
    }

    private static void initializeAndStart() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Error setting look and feel: " + ex.getMessage());
            }
        }
        new Main().start();
    }

    public void start() {
        showDifficultyDialog();
        initializeGame();
    }

    private void showDifficultyDialog() {
        String[] options = {"Beginner (8x8, 10 mines)", "Intermediate (12x12, 20 mines)", "Expert (16x16, 40 mines)", "Custom"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose difficulty level:", "Minesweeper",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        int rows, cols, mines;

        switch (choice) {
            case 0:
                rows = 8; cols = 8; mines = 10;
                break;
            case 1:
                rows = 12; cols = 12; mines = 20;
                break;
            case 2:
                rows = 16; cols = 16; mines = 40;
                break;
            case 3:
                rows = getCustomValue("Enter number of rows (5-30):", 10, 5, 30);
                cols = getCustomValue("Enter number of columns (5-30):", 10, 5, 30);
                int maxMines = (rows * cols) / 3;
                mines = getCustomValue("Enter number of mines (1-" + maxMines + "):", Math.min(20, maxMines), 1, maxMines);
                break;
            default:
                rows = 8; cols = 8; mines = 10;
        }

        this.game = new MinesweeperGame(rows, cols, mines);
    }

    private int getCustomValue(String message, int defaultValue, int min, int max) {
        while (true) {
            String input = JOptionPane.showInputDialog(null, message, defaultValue);
            if (input == null) {
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                JOptionPane.showMessageDialog(null,
                        "Please enter a value between " + min + " and " + max);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number");
            }
        }
    }

    private void initializeGame() {
        gui = new MinesweeperGUI(game);

        gui.setGameEventListener(new MinesweeperGUI.GameEventListener() {
            @Override
            public void onCellRevealed(int row, int col) {
                handleCellReveal(row, col);
            }

            @Override
            public void onCellFlagged(int row, int col) {
                handleCellFlag(row, col);
            }

            @Override
            public void onUndo() {
                handleUndo();
            }

            @Override
            public void onNewGame() {
                handleNewGame();
            }
        });

        gui.setVisible(true);
    }

    private void handleCellReveal(int row, int col) {
        if (game.isGameOver()) return;

        // Store current state for comparison
        boolean wasGameOverBefore = game.isGameOver();

        // Perform the reveal operation
        boolean wasRevealed = game.revealCell(row, col);

        if (wasRevealed) {
            gui.updateDisplay();

            // Check if game over occurred (mine click)
            if (game.isGameOver() && !game.isGameWon()) {
                // Mine was clicked - show game over
                gui.showGameOver(false, row, col);
            } else if (game.isGameOver() && game.isGameWon()) {
                // Game was won - show win message
                gui.showGameOver(true, -1, -1);
            }
        }
    }

    private void handleCellFlag(int row, int col) {
        if (game.isGameOver()) return;

        if (game.toggleFlag(row, col)) {
            gui.updateDisplay();
            // Check for win after flagging
            if (game.isGameOver() && game.isGameWon()) {
                gui.showGameOver(true, -1, -1);
            }
        }
    }

    private void handleUndo() {
        if (game.undo()) {
            gui.updateDisplay();
            // Only show "Move undone!" if the game is not over (not won)
            if (!game.isGameOver()) {
                gui.showMessage("Move undone!");
            } else if (game.isGameOver() && game.isGameWon()) {
                // If we undid to a win state, show win message
                gui.showGameOver(true, -1, -1);
            }
        } else {
            gui.showMessage("Cannot undo!");
        }
    }

    private void handleNewGame() {
        showDifficultyDialog();
        gui.resetGame(game);
    }
}