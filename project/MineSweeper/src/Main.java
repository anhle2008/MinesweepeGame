import javax.swing.*;
import game.MinesweeperGame;
import gui.MinesweeperGUI;
import utils.InputValidator;

public class Main {
    // Game model and view components
    private MinesweeperGame game;
    private MinesweeperGUI gui;

    // Main entry point of the application
    public static void main(String[] args) {
        // Use SwingUtilities to ensure GUI is created on Event Dispatch Thread
        SwingUtilities.invokeLater(Main::initializeAndStart);
    }

    // Initialize and start the game application
    private static void initializeAndStart() {
        setLookAndFeel();  // Set the system look and feel
        new Main().start(); // Create Main instance and start the game
    }

    // Set the application's look and feel to match the operating system
    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to cross-platform look and feel if system L&F fails
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Error setting look and feel: " + ex.getMessage());
            }
        }
    }

    // Start the game by showing difficulty dialog and initializing game
    public void start() {
        showDifficultyDialog();  // Let user choose game difficulty
        initializeGame();        // Set up game and GUI
    }

    // Show dialog for user to select game difficulty or custom settings
    private void showDifficultyDialog() {
        String[] options = {"Beginner (8x8, 10 mines)", "Intermediate (12x12, 20 mines)",
                "Expert (16x16, 40 mines)", "Custom"};
        int choice = JOptionPane.showOptionDialog(null, "Choose difficulty level:", "Minesweeper",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        int rows, cols, mines;

        // Set game parameters based on user choice
        switch (choice) {
            case 0: rows = 8; cols = 8; mines = 10; break;   // Beginner
            case 1: rows = 12; cols = 12; mines = 20; break; // Intermediate
            case 2: rows = 16; cols = 16; mines = 40; break; // Expert
            case 3: // Custom settings
                rows = getCustomValue("Enter number of rows (5-30):", 10, 5, 30);
                cols = getCustomValue("Enter number of columns (5-30):", 10, 5, 30);
                int maxMines = InputValidator.getMaxMines(rows, cols);
                mines = getCustomValue("Enter number of mines (1-" + maxMines + "):",
                        Math.min(20, maxMines), 1, maxMines);
                break;
            default: rows = 8; cols = 8; mines = 10; // Default to beginner
        }

        // Create new game with selected parameters
        this.game = new MinesweeperGame(rows, cols, mines);
    }

    // Show dialog to get a custom value from user with validation
    private int getCustomValue(String message, int defaultValue, int min, int max) {
        while (true) {
            String input = JOptionPane.showInputDialog(null, message, defaultValue);
            if (input == null) {
                return defaultValue; // User cancelled, use default
            }
            try {
                int value = Integer.parseInt(input);
                if (InputValidator.isValidCustomValue(value, min, max)) {
                    return value; // Valid value entered
                }
                // Invalid value, show error and retry
                JOptionPane.showMessageDialog(null,
                        "Please enter a value between " + min + " and " + max);
            } catch (NumberFormatException e) {
                // Not a valid number, show error and retry
                JOptionPane.showMessageDialog(null, "Please enter a valid number");
            }
        }
    }

    // Initialize the game GUI and set up event handlers
    private void initializeGame() {
        gui = new MinesweeperGUI(game);

        // Set up event listener for GUI actions
        gui.setGameEventListener(new MinesweeperGUI.GameEventListener() {
            @Override
            public void onCellRevealed(int row, int col) {
                handleCellReveal(row, col);  // Handle left click on cell
            }

            @Override
            public void onCellFlagged(int row, int col) {
                handleCellFlag(row, col);    // Handle right click on cell
            }

            @Override
            public void onUndo() {
                handleUndo();                // Handle undo request
            }

            @Override
            public void onNewGame() {
                handleNewGame();             // Handle new game request
            }
        });

        gui.setVisible(true);  // Make the GUI window visible
    }

    // Handle cell reveal (left click) from GUI
    private void handleCellReveal(int row, int col) {
        if (game.isGameOver()) return;  // Ignore clicks if game is over

        if (game.revealCell(row, col)) {
            gui.updateDisplay();  // Update GUI to show revealed cells

            if (game.isGameOver()) {
                // Show win/lose message
                gui.showGameOver(game.isGameWon(),
                        game.isGameWon() ? -1 : row,  // -1 for win, actual coordinates for loss
                        game.isGameWon() ? -1 : col);
            }
        }
    }

    // Handle cell flag toggle (right click) from GUI
    private void handleCellFlag(int row, int col) {
        if (game.isGameOver()) return;  // Ignore clicks if game is over

        if (game.toggleFlag(row, col)) {
            gui.updateDisplay();  // Update GUI to show flag state
            if (game.isGameOver() && game.isGameWon()) {
                gui.showGameOver(true, -1, -1);  // Show win message if flagging completed the game
            }
        }
    }

    // Handle undo request from GUI
    private void handleUndo() {
        if (game.undo()) {
            gui.updateDisplay();  // Update GUI to previous state
            if (!game.isGameOver()) {
                gui.showMessage("Move undone!");  // Show temporary confirmation
            } else if (game.isGameWon()) {
                gui.showGameOver(true, -1, -1);  // Show win if undo resulted in win
            }
        } else {
            gui.showMessage("Cannot undo!");  // Show error if undo not possible
        }
    }

    // Handle new game request from GUI
    private void handleNewGame() {
        showDifficultyDialog();  // Show difficulty selection dialog
        gui.resetGame(game);     // Reset GUI with new game
    }
}