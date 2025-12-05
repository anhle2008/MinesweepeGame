package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class ImageLoader {
    // Paths to look for images (in order of priority)
    private static final String USER_ABSOLUTE_PATH = "E:/study/sem 1 nam 3/dsa/project/project/MineSweeper/images/";
    private static final String IMAGE_FOLDER = "images/";

    // Image icons for game elements
    private ImageIcon flagIcon;     // Icon for flagged cells
    private ImageIcon mineIcon;     // Icon for mines
    private ImageIcon hitMineIcon;  // Icon for the mine that was clicked (game over)

    // Constructor - loads all icons when ImageLoader is created
    public ImageLoader() {
        loadIcons();
    }

    // Load all required icons
    private void loadIcons() {
        flagIcon = createScaledIcon("flag.png", 24);
        mineIcon = createScaledIcon("bomb.png", 24);
        hitMineIcon = createScaledIcon("bomb.png", 24);
    }

    // Create a scaled icon from an image file
    private ImageIcon createScaledIcon(String filename, int size) {
        // Try different paths in order
        String[] paths = {
                USER_ABSOLUTE_PATH + filename,   // Absolute path first
                IMAGE_FOLDER + filename,         // Relative path in project folder
                "/" + IMAGE_FOLDER + filename    // Resource path (in JAR file)
        };

        // Try each path until we find the image
        for (String path : paths) {
            Image image = loadImage(path);
            if (image != null) {
                // Scale image to desired size
                Image scaled = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        }

        // If image not found, create a fallback colored circle
        return createFallbackIcon(size, getColorForFilename(filename));
    }

    // Load an image from a path (file system or resource)
    private Image loadImage(String path) {
        try {
            if (path.startsWith("/")) {
                // Load from resources (inside JAR file)
                URL resource = getClass().getResource(path);
                return resource != null ? javax.imageio.ImageIO.read(resource) : null;
            } else {
                // Load from file system
                File file = new File(path);
                return file.exists() ? javax.imageio.ImageIO.read(file) : null;
            }
        } catch (Exception e) {
            return null;  // Return null if image loading fails
        }
    }

    // Determine what color to use for fallback icons based on filename
    private Color getColorForFilename(String filename) {
        if (filename.contains("flag")) return Color.BLUE;    // Flags are blue
        if (filename.contains("bomb") || filename.contains("mine")) return Color.BLACK;  // Mines are black
        return Color.GRAY;  // Default color
    }

    // Create a simple colored circle as a fallback when images aren't found
    private ImageIcon createFallbackIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(2, 2, size-4, size-4);  // Draw filled circle
        g2d.setColor(Color.BLACK);
        g2d.drawOval(2, 2, size-4, size-4);  // Draw circle outline
        g2d.dispose();
        return new ImageIcon(image);
    }

    // Getter methods for icons (with fallback creation if needed)
    public ImageIcon getFlagIcon() {
        return flagIcon != null ? flagIcon : createFallbackIcon(24, Color.BLUE);
    }

    public ImageIcon getMineIcon() {
        return mineIcon != null ? mineIcon : createFallbackIcon(24, Color.BLACK);
    }

    public ImageIcon getHitMineIcon() {
        return hitMineIcon != null ? hitMineIcon : createFallbackIcon(24, Color.RED);
    }
}