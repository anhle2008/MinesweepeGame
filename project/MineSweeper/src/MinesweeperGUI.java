import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.image.BufferedImage;
import java.net.URL;

public class MinesweeperGUI extends JFrame {
    private MinesweeperGame game;
    private JButton[][] buttons;
    private JLabel statusLabel;
    private JLabel flagsLabel;
    private JPanel boardPanel;
    private Timer messageTimer;

    // Image paths
    private static final String[] IMAGE_PATHS = {
            "E:\\study\\sem 1 nam 3\\dsa\\project\\MineSweeper\\imanges\\", // Fixed path
            "E:\\study\\sem1_3\\dsa\\project\\Minesweeper\\images\\",
            "images/",
            "imanges/", // Add the misspelled version too
            "./images/",
            "./imanges/", // Add the misspelled version
            "../images/",
            "../imanges/" // Add the misspelled version
    };

    // Cache the icons
    private ImageIcon flagIcon;
    private ImageIcon mineIcon;
    private ImageIcon hitMineIcon;


    public interface GameEventListener {
        void onCellRevealed(int row, int col);
        void onCellFlagged(int row, int col);
        void onUndo();
        void onNewGame();
    }

    private GameEventListener gameEventListener;

    public MinesweeperGUI(MinesweeperGame game) {
        this.game = game;
        this.gameEventListener = null;
        loadIcons();
        initializeGUI();
        setupKeyboardShortcuts();
    }

    public void setGameEventListener(GameEventListener listener) {
        this.gameEventListener = listener;
    }

    private void loadIcons() {
        System.out.println("Loading icons...");

        flagIcon = createScaledIcon("flag.png", 24);
        mineIcon = createScaledIcon("bomb.png", 24);
        hitMineIcon = createScaledIcon("bomb.png", 24);


        System.out.println("Flag icon loaded: " + (flagIcon != null));
        System.out.println("Mine icon loaded: " + (mineIcon != null));
        System.out.println("Hit mine icon loaded: " + (hitMineIcon != null));

    }

    private ImageIcon createScaledIcon(String filename, int size) {
        // Try file paths first
        for (String basePath : IMAGE_PATHS) {
            String fullPath = basePath + filename;
            try {
                File imgFile = new File(fullPath);
                if (imgFile.exists() && imgFile.canRead()) {
                    System.out.println("Found image at: " + fullPath);
                    Image image = javax.imageio.ImageIO.read(imgFile);
                    if (image != null) {
                        Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                }
            } catch (Exception e) {
                // Continue to next path
            }
        }

        // Try resource loading
        try {
            URL resource = getClass().getResource("/images/" + filename);
            if (resource != null) {
                Image image = javax.imageio.ImageIO.read(resource);
                Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.err.println("Resource loading failed for: " + filename);
        }

        // Fallback to colored icons
        System.out.println("Using fallback icon for: " + filename);
        return createFallbackIcon(size, getColorForFilename(filename));
    }

    private Color getColorForFilename(String filename) {
        if (filename.contains("flag")) return Color.BLUE;
        if (filename.contains("bomb") || filename.contains("mine")) return Color.BLACK;
        return Color.GRAY;
    }

    private ImageIcon createFallbackIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(2, 2, size-4, size-4);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(2, 2, size-4, size-4);

        g2d.dispose();
        return new ImageIcon(image);
    }

    private ImageIcon getFlagIcon() {
        return flagIcon != null ? flagIcon : createFallbackIcon(24, Color.BLUE);
    }

    private ImageIcon getMineIcon() {
        return mineIcon != null ? mineIcon : createFallbackIcon(24, Color.BLACK);
    }

    private ImageIcon getHitMineIcon() {
        return hitMineIcon != null ? hitMineIcon : createFallbackIcon(24, Color.RED);
    }



    private void initializeGUI() {
        setTitle("Minesweeper with Undo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Status Panel
        JPanel statusLinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Click to start!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLinePanel.add(statusLabel);

        // Control/Info Panel
        JPanel controlInfoLinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        flagsLabel = new JLabel("Flags: 0/" + game.getTotalMines());

        JButton newGameButton = new JButton("New Game");
        JButton undoButton = new JButton("Undo");

        controlInfoLinePanel.add(flagsLabel);
        controlInfoLinePanel.add(Box.createHorizontalStrut(15));
        controlInfoLinePanel.add(newGameButton);
        controlInfoLinePanel.add(undoButton);

        // Combine panels
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        northPanel.add(statusLinePanel);
        northPanel.add(controlInfoLinePanel);
        add(northPanel, BorderLayout.NORTH);

        // Board panel
        setupBoardPanel();

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

        // Initialize message timer
        messageTimer = new Timer(2000, e -> {
            clearStatusMessage();
        });
        messageTimer.setRepeats(false);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
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
                JButton button = createCellButton(i, j);
                buttons[i][j] = button;
                boardPanel.add(button);
            }
        }

        add(boardPanel, BorderLayout.CENTER);
    }

    private JButton createCellButton(int row, int col) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(35, 35));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        // Left click - reveal
        button.addActionListener(e -> {
            if (gameEventListener != null) {
                // Clear any existing message when making a move
                clearStatusMessage();
                gameEventListener.onCellRevealed(row, col);
            }
        });

        // Right click - flag
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (gameEventListener != null) {
                        // Clear any existing message when making a move
                        clearStatusMessage();
                        gameEventListener.onCellFlagged(row, col);
                    }
                }
            }
        });

        return button;
    }

    private void setupKeyboardShortcuts() {
        InputMap inputMap = boardPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = boardPanel.getActionMap();

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

    private Color getNumberColor(int adjacentMines) {
        return switch (adjacentMines) {
            case 1 -> new Color(0, 0, 255);     // Blue
            case 2 -> new Color(0, 128, 0);     // Green
            case 3 -> new Color(255, 0, 0);     // Red
            case 4 -> new Color(0, 0, 128);     // Navy
            case 5 -> new Color(128, 0, 0);     // Maroon
            case 6 -> new Color(0, 128, 128);   // Teal
            case 7 -> new Color(0, 0, 0);       // Black
            case 8 -> new Color(128, 128, 128); // Gray
            default -> Color.BLACK;
        };
    }
    public void updateDisplay() {
        int rows = game.getRows();
        int cols = game.getCols();
        Color COVERED_COLOR = new Color(192, 192, 192);
        Color REVEALED_COLOR = new Color(220, 220, 220);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton button = buttons[i][j];
                Cell cell = game.getCell(i, j);

                // Reset appearance
                button.setText("");
                button.setIcon(null);
                button.setForeground(Color.BLACK);

                if (cell.isRevealed()) {
                    button.setEnabled(false);
                    button.setBackground(REVEALED_COLOR);
                    button.setBorder(BorderFactory.createLoweredBevelBorder());

                    if (cell.isMine()) {
                        button.setIcon(getMineIcon());
                        button.setBackground(Color.RED);
                    } else if (cell.getAdjacentMines() > 0) {
                        button.setText(String.valueOf(cell.getAdjacentMines()));
                        button.setForeground(getNumberColor(cell.getAdjacentMines()));
                    }
                } else {
                    button.setEnabled(true);
                    button.setBackground(COVERED_COLOR);
                    button.setBorder(BorderFactory.createRaisedBevelBorder());

                    if (cell.isFlagged()) {
                        button.setIcon(getFlagIcon());
                    }
                }
            }
        }

        flagsLabel.setText("Flags: " + game.getFlagsPlaced() + "/" + game.getTotalMines());
    }



    public void showGameOver(boolean won, int hitRow, int hitCol) {
        // Stop any pending message timer
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

        // Game over messages stay permanently, no auto-clear
    }

    private void revealAllMines(int hitRow, int hitCol) {
        int rows = game.getRows();
        int cols = game.getCols();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = game.getCell(i, j);
                JButton button = buttons[i][j];

                button.setEnabled(false);
                button.setIcon(null);

                if (i == hitRow && j == hitCol) {
                    button.setIcon(getHitMineIcon());
                    button.setBackground(Color.RED);
                    button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                }
                else if (cell.isMine() && !cell.isFlagged()) {
                    button.setIcon(getMineIcon());
                    button.setBackground(new Color(255, 200, 200));
                }

                else if (!cell.isMine() && cell.isFlagged()) {
                    // Just show the flag icon as normal, no special treatment
                    button.setIcon(getFlagIcon());
                    button.setBackground(Color.PINK); // Optional: keep background color to indicate error
                }
            }
        }
    }

    public void resetGame(MinesweeperGame newGame) {
        // Stop any pending message timer
        if (messageTimer.isRunning()) {
            messageTimer.stop();
        }

        this.game = newGame;

        // Remove old board
        remove(boardPanel);

        // Create new board
        setupBoardPanel();

        // Refresh the display
        revalidate();
        repaint();

        statusLabel.setText("Click to start!");
        statusLabel.setForeground(Color.BLACK);
        flagsLabel.setText("Flags: 0/" + game.getTotalMines());

        pack();
        setLocationRelativeTo(null);
    }

    public void showMessage(String message) {
        // Stop any pending message timer
        if (messageTimer.isRunning()) {
            messageTimer.stop();
        }

        statusLabel.setText(message);
        statusLabel.setForeground(Color.BLACK);

        // Start timer to clear message after 2 seconds
        messageTimer.start();
    }

    private void clearStatusMessage() {
        // Stop any pending message timer
        if (messageTimer.isRunning()) {
            messageTimer.stop();
        }

        // Only clear if it's not a game over or win message
        String currentText = statusLabel.getText();
        if (!currentText.contains("Congratulations") && !currentText.contains("Game Over")) {
            statusLabel.setText("");
        }
    }
}