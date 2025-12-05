package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class ImageLoader {
    private static final String USER_ABSOLUTE_PATH = "E:/study/sem 1 nam 3/dsa/project/project/MineSweeper/images/";
    private static final String IMAGE_FOLDER = "images/";

    private ImageIcon flagIcon;
    private ImageIcon mineIcon;
    private ImageIcon hitMineIcon;

    public ImageLoader() {
        loadIcons();
    }

    private void loadIcons() {
        flagIcon = createScaledIcon("flag.png", 24);
        mineIcon = createScaledIcon("bomb.png", 24);
        hitMineIcon = createScaledIcon("bomb.png", 24);
    }

    private ImageIcon createScaledIcon(String filename, int size) {
        String[] paths = {
                USER_ABSOLUTE_PATH + filename,
                IMAGE_FOLDER + filename,
                "/" + IMAGE_FOLDER + filename
        };

        for (String path : paths) {
            Image image = loadImage(path);
            if (image != null) {
                Image scaled = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        }

        return createFallbackIcon(size, getColorForFilename(filename));
    }

    private Image loadImage(String path) {
        try {
            if (path.startsWith("/")) {
                URL resource = getClass().getResource(path);
                return resource != null ? javax.imageio.ImageIO.read(resource) : null;
            } else {
                File file = new File(path);
                return file.exists() ? javax.imageio.ImageIO.read(file) : null;
            }
        } catch (Exception e) {
            return null;
        }
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

    // Getters
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