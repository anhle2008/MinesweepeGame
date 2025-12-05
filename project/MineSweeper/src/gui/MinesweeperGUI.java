package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import game.MinesweeperGame;
import game.Cell;
import utils.GameConstants;

public class MinesweeperGUI extends JFrame {
    private MinesweeperGame game;
    private JButton[][] buttons;
    private JLabel statusLabel;
    private JLabel flagsLabel;
    private JPanel boardPanel;
    private Timer messageTimer;
    private ImageLoader imageLoader;

    public interface GameEventListener {
        void onCellRevealed(int row, int col);
        void onCellFlagged(int row, int col);
        void onUndo();
        void onNewGame();
    }

    private GameEventListener gameEventListener;

    public MinesweeperGUI(MinesweeperGame game) {
        this.game = game;
        this.imageLoader = new ImageLoader();
        this.gameEventListener = null;
        initializeGUI();
        setupKeyboardShortcuts();
    }

    public void setGameEventListener(GameEventListener listener) {
        this.gameEventListener = listener;
    }

    private void initializeGUI() {
        setTitle("Minesweeper with Undo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the north panel with status and controls (YOUR ORIGINAL LAYOUT)
        JPanel northPanel = createNorthPanel();
        add(northPanel, BorderLayout.NORTH);

        // Board panel
        setupBoardPanel();

        // Timer for temporary messages
        messageTimer = new Timer(2000, e -> clearStatusMessage());
        messageTimer.setRepeats(false);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private JPanel createNorthPanel() {
        // Main north panel with vertical box layout
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Status Panel (top line)
        JPanel statusLinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Click to start!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLinePanel.add(statusLabel);
        northPanel.add(statusLinePanel);

        // Control/Info Panel (second line) - YOUR ORIGINAL LAYOUT
        JPanel controlInfoLinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        flagsLabel = new JLabel("Flags: 0/" + game.getTotalMines());

        JButton newGameButton = new JButton("New Game");
        JButton undoButton = new JButton("Undo");

        controlInfoLinePanel.add(flagsLabel);
        controlInfoLinePanel.add(Box.createHorizontalStrut(15));
        controlInfoLinePanel.add(newGameButton);
        controlInfoLinePanel.add(undoButton);

        // Button actions
        newGameButton.addActionListener(e -> {
            if (gameEventListener != null) {
                gameEventListener.onNewGame();
            }
        });

        undoButton.addActionListener(e -> {
            if (gameEventListener != null) {
                gameEventListener.onUndo();
            }
        });

        northPanel.add(controlInfoLinePanel);

        return northPanel;
    }

    private void setupBoardPanel() {
        boardPanel = new JPanel();
        int rows = game.getRows();
        int cols = game.getCols();
        boardPanel.setLayout(new GridLayout(rows, cols, 1, 1));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttons = new JButton[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j] = createCellButton(i, j);
                boardPanel.add(buttons[i][j]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);
    }

    private JButton createCellButton(int row, int col) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(GameConstants.CELL_SIZE, GameConstants.CELL_SIZE));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        // Left click - reveal
        button.addActionListener(e -> {
            if (gameEventListener != null) {
                clearStatusMessage();
                gameEventListener.onCellRevealed(row, col);
            }
        });

        // Right click - flag
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

    private void setupKeyboardShortcuts() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "newGame");

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

    public void updateDisplay() {
        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getCols(); j++) {
                updateCellDisplay(i, j);
            }
        }
        flagsLabel.setText("Flags: " + game.getFlagsPlaced() + "/" + game.getTotalMines());
    }

    private void updateCellDisplay(int row, int col) {
        JButton button = buttons[row][col];
        Cell cell = game.getCell(row, col);

        button.setText("");
        button.setIcon(null);
        button.setForeground(Color.BLACK);

        if (cell.isRevealed()) {
            button.setEnabled(false);
            button.setBackground(GameConstants.REVEALED_COLOR);
            button.setBorder(BorderFactory.createLoweredBevelBorder());

            if (cell.isMine()) {
                button.setIcon(imageLoader.getMineIcon());
                button.setBackground(Color.RED);
            } else if (cell.getAdjacentMines() > 0) {
                button.setText(String.valueOf(cell.getAdjacentMines()));
                button.setForeground(GameConstants.getNumberColor(cell.getAdjacentMines()));
            }
        } else {
            button.setEnabled(true);
            button.setBackground(GameConstants.COVERED_COLOR);
            button.setBorder(BorderFactory.createRaisedBevelBorder());

            if (cell.isFlagged()) {
                button.setIcon(imageLoader.getFlagIcon());
            }
        }
    }

    public void showGameOver(boolean won, int hitRow, int hitCol) {
        if (messageTimer.isRunning()) {
            messageTimer.stop();
        }

        if (won) {
            statusLabel.setText(" Congratulations! You won!");
            statusLabel.setForeground(new Color(0, 128, 0));
        } else {
            statusLabel.setText(" Game Over! You hit a mine!");
            statusLabel.setForeground(Color.RED);
            revealAllMines(hitRow, hitCol);
        }
    }

    private void revealAllMines(int hitRow, int hitCol) {
        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getCols(); j++) {
                Cell cell = game.getCell(i, j);
                JButton button = buttons[i][j];
                button.setEnabled(false);

                if (i == hitRow && j == hitCol) {
                    button.setIcon(imageLoader.getHitMineIcon());
                    button.setBackground(Color.RED);
                    button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                } else if (cell.isMine() && !cell.isFlagged()) {
                    button.setIcon(imageLoader.getMineIcon());
                    button.setBackground(new Color(255, 200, 200));
                } else if (!cell.isMine() && cell.isFlagged()) {
                    button.setIcon(imageLoader.getFlagIcon());
                    button.setBackground(Color.PINK);
                }
            }
        }
    }

    public void resetGame(MinesweeperGame newGame) {
        if (messageTimer.isRunning()) {
            messageTimer.stop();
        }

        this.game = newGame;
        remove(boardPanel);
        setupBoardPanel();
        revalidate();
        repaint();

        statusLabel.setText("Click to start!");
        statusLabel.setForeground(Color.BLACK);
        flagsLabel.setText("Flags: 0/" + game.getTotalMines());

        pack();
        setLocationRelativeTo(null);
    }

    public void showMessage(String message) {
        if (messageTimer.isRunning()) {
            messageTimer.stop();
        }

        statusLabel.setText(message);
        statusLabel.setForeground(Color.BLACK);
        messageTimer.start();
    }

    private void clearStatusMessage() {
        if (messageTimer.isRunning()) {
            messageTimer.stop();
        }

        String currentText = statusLabel.getText();
        if (!currentText.contains("Congratulations") && !currentText.contains("Game Over")) {
            statusLabel.setText("");
        }
    }
}