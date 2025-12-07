package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import game.MinesweeperGame;
import game.Cell;
import utils.GameConstants;

public class MinesweeperGUI extends JFrame {
    // Game model reference
    private MinesweeperGame game;

    // UI components
    private JButton[][] buttons;      // Grid of buttons representing cells
    private JLabel statusLabel;       // Label for game status messages
    private JLabel flagsLabel;        // Label showing flag count
    private JPanel boardPanel;        // Panel containing the game board
    private Timer messageTimer;       // Timer for temporary status messages
    private ImageLoader imageLoader;  // Loads and manages game icons

    // Interface for handling game events
    public interface GameEventListener {
        void onCellRevealed(int row, int col);  // Called when a cell is left-clicked
        void onCellFlagged(int row, int col);   // Called when a cell is right-clicked
        void onUndo();                          // Called when undo is requested
        void onNewGame();                       // Called when new game is requested
    }

    private GameEventListener gameEventListener;  // Listener for game events

    // Constructor - sets up the GUI
    public MinesweeperGUI(MinesweeperGame game) {
        this.game = game;
        this.imageLoader = new ImageLoader();
        this.gameEventListener = null;
        initializeGUI();            // Set up the window and components
        setupKeyboardShortcuts();   // Set up keyboard controls
    }

    // Set the event listener for game actions
    public void setGameEventListener(GameEventListener listener) {
        this.gameEventListener = listener;
    }

    // Initialize all GUI components
    private void initializeGUI() {
        setTitle("Minesweeper with Undo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the north panel with status and controls
        JPanel northPanel = createNorthPanel();
        add(northPanel, BorderLayout.NORTH);

        // Board panel (game grid)
        setupBoardPanel();

        // Timer for temporary messages (clears after 2 seconds)
        messageTimer = new Timer(2000, e -> clearStatusMessage());
        messageTimer.setRepeats(false);

        pack();                    // Size window to fit components
        setLocationRelativeTo(null);  // Center window on screen
        setResizable(false);       // Prevent window resizing
    }

    // Create the top panel with status and control buttons
    private JPanel createNorthPanel() {
        // Main north panel with vertical box layout
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Status Panel (top line - shows game messages)
        JPanel statusLinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Click to start!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLinePanel.add(statusLabel);
        northPanel.add(statusLinePanel);

        // Control/Info Panel (second line - flags counter and buttons)
        JPanel controlInfoLinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        flagsLabel = new JLabel("Flags: 0/" + game.getTotalMines());

        JButton newGameButton = new JButton("New Game");
        JButton undoButton = new JButton("Undo");

        controlInfoLinePanel.add(flagsLabel);
        controlInfoLinePanel.add(Box.createHorizontalStrut(15));  // Spacer
        controlInfoLinePanel.add(newGameButton);
        controlInfoLinePanel.add(undoButton);

        // Button actions
        newGameButton.addActionListener(e -> {
            if (gameEventListener != null) {
                gameEventListener.onNewGame();  // Request new game
            }
        });

        undoButton.addActionListener(e -> {
            if (gameEventListener != null) {
                gameEventListener.onUndo();  // Request undo
            }
        });

        northPanel.add(controlInfoLinePanel);

        return northPanel;
    }

    // Set up the game board panel with grid of buttons
    private void setupBoardPanel() {
        boardPanel = new JPanel();
        int rows = game.getRows();
        int cols = game.getCols();
        boardPanel.setLayout(new GridLayout(rows, cols, 1, 1));  // Grid with 1px gaps
        boardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttons = new JButton[rows][cols];

        // Create a button for each cell
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j] = createCellButton(i, j);
                boardPanel.add(buttons[i][j]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);
    }

    // Create a button for a specific cell
    private JButton createCellButton(int row, int col) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(GameConstants.CELL_SIZE, GameConstants.CELL_SIZE));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);  // Don't show focus border
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createRaisedBevelBorder());  // 3D raised look

        // Left click - reveal cell
        button.addActionListener(e -> {
            if (gameEventListener != null) {
                clearStatusMessage();
                gameEventListener.onCellRevealed(row, col);
            }
        });

        // Right click - toggle flag
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && gameEventListener != null) {
                    clearStatusMessage();
                    gameEventListener.onCellFlagged(row, col);
                }
            }
        });

        return button;
    }

    // Set up keyboard shortcuts (hotkeys)
    private void setupKeyboardShortcuts() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        // Ctrl+Z for undo
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        // Ctrl+N for new game
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "newGame");

        // Define actions for keyboard shortcuts
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameEventListener != null) gameEventListener.onUndo();
            }
        });

        actionMap.put("newGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameEventListener != null) gameEventListener.onNewGame();
            }
        });
    }

    // Update the entire display to reflect current game state
    public void updateDisplay() {
        // Update each cell button
        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getCols(); j++) {
                updateCellDisplay(i, j);
            }
        }
        // Update flags counter
        flagsLabel.setText("Flags: " + game.getFlagsPlaced() + "/" + game.getTotalMines());
    }

    // Update the display of a single cell
    private void updateCellDisplay(int row, int col) {
        JButton button = buttons[row][col];
        Cell cell = game.getCell(row, col);

        button.setText("");
        button.setIcon(null);
        button.setForeground(Color.BLACK);

        if (cell.isRevealed()) {
            //button.setEnabled(false);  // Disable revealed cells
            button.setBackground(GameConstants.REVEALED_COLOR);
            button.setBorder(BorderFactory.createLoweredBevelBorder());  // 3D sunken look

            if (cell.isMine()) {
                button.setIcon(imageLoader.getMineIcon());  // Show mine icon
                button.setBackground(Color.RED);  // Red background for mine
            } else if (cell.getAdjacentMines() > 0) {
                // Show number of adjacent mines with appropriate color
                button.setText(String.valueOf(cell.getAdjacentMines()));
                button.setForeground(GameConstants.getNumberColor(cell.getAdjacentMines()));
            }
        } else {
            button.setEnabled(true);  // Enable covered cells
            button.setBackground(GameConstants.COVERED_COLOR);
            button.setBorder(BorderFactory.createRaisedBevelBorder());  // 3D raised look

            if (cell.isFlagged()) {
                button.setIcon(imageLoader.getFlagIcon());  // Show flag icon
            }
        }
    }

    // Show game over message and reveal the board
    public void showGameOver(boolean won, int hitRow, int hitCol) {
        if (messageTimer.isRunning()) {
            messageTimer.stop();  // Stop any temporary messages
        }

        if (won) {
            statusLabel.setText(" Congratulations! You won!");
            statusLabel.setForeground(new Color(0, 128, 0));  // Green for win
        } else {
            statusLabel.setText(" Game Over! You hit a mine!");
            statusLabel.setForeground(Color.RED);  // Red for loss
            revealAllMines(hitRow, hitCol);  // Show all mines on loss
        }
    }

    // Reveal all mines when game is lost
    private void revealAllMines(int hitRow, int hitCol) {
        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getCols(); j++) {
                Cell cell = game.getCell(i, j);
                JButton button = buttons[i][j];
                button.setEnabled(false);  // Disable all buttons

                // Highlight the mine that was clicked
                if (i == hitRow && j == hitCol) {
                    button.setIcon(imageLoader.getHitMineIcon());
                    button.setBackground(Color.RED);
                    button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                } else if (cell.isMine() && !cell.isFlagged()) {
                    // Show unflagged mines
                    button.setIcon(imageLoader.getMineIcon());
                    button.setBackground(new Color(255, 200, 200));  // Light red
                } else if (!cell.isMine() && cell.isFlagged()) {
                    // Show incorrect flags (flags on non-mines)
                    button.setIcon(imageLoader.getFlagIcon());
                    button.setBackground(Color.PINK);  // Pink for incorrect flags
                }
            }
        }
    }

    // Reset the game with a new game instance
    public void resetGame(MinesweeperGame newGame) {
        if (messageTimer.isRunning()) {
            messageTimer.stop();
        }

        this.game = newGame;  // Replace game model
        remove(boardPanel);   //
        remove(boardPanel);   // Remove old board panel from the frame

        setupBoardPanel();    // Create new board panel with new dimensions
        revalidate();         // Re-layout the components
        repaint();            // Redraw the window

        // Reset status display
        statusLabel.setText("Click to start!");
        statusLabel.setForeground(Color.BLACK);
        flagsLabel.setText("Flags: 0/" + game.getTotalMines());

        pack();                 // Resize window to fit new board
        setLocationRelativeTo(null);  // Center window on screen
    }

    // Show a temporary message in the status bar
    public void showMessage(String message) {
        if (messageTimer.isRunning()) {
            messageTimer.stop();  // Stop existing timer
        }

        statusLabel.setText(message);
        statusLabel.setForeground(Color.BLACK);
        messageTimer.start();  // Start timer to clear message after 2 seconds
    }

    // Clear temporary status messages (called by timer)
    private void clearStatusMessage() {
        if (messageTimer.isRunning()) {
            messageTimer.stop();
        }

        String currentText = statusLabel.getText();
        // Only clear if not showing win/lose messages
        if (!currentText.contains("Congratulations") && !currentText.contains("Game Over")) {
            statusLabel.setText("");
        }
    }
}