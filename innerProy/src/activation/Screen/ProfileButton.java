
package activation.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
/**
 * The ProfileButton class is a custom JButton that displays a circular profile picture.
 * It extends JButton and overrides the paintComponent method to draw the profile picture within a circular shape.
 * The button also includes hover effects and focus indicators for better user interaction.
 */
public class ProfileButton extends JButton {

    private BufferedImage picture;
/**
     * Constructor for the ProfileButton class.
     *
     * @param picture The BufferedImage representing the profile picture to be displayed on the button.
     */
    public ProfileButton(BufferedImage picture) {
        this.picture = picture;

        setPreferredSize(new Dimension(54, 54));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("My Profile");
    }
/**
     * Sets the profile picture to be displayed on the button.
     *
     * @param picture The BufferedImage representing the new profile picture.
     */
    public void setPicture(BufferedImage picture) {
        this.picture = picture;
        repaint();
    }
/**
     * Gets the current profile picture displayed on the button.
     *
     * @return The BufferedImage representing the current profile picture.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int size = Math.min(getWidth(), getHeight()) - 8;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        Ellipse2D circle = new Ellipse2D.Double(
                x, y, size, size
        );

        // Dark background behind the picture
        g2.setColor(new Color(25, 20, 20));
        g2.fill(circle);

        if (picture != null) {

            Shape originalClip = g2.getClip();
            g2.setClip(circle);

            // Scale image while preserving aspect ratio
            double scale = Math.max(
                    (double) size / picture.getWidth(),
                    (double) size / picture.getHeight()
            );

            int width = (int) Math.ceil(picture.getWidth() * scale);
            int height = (int) Math.ceil(picture.getHeight() * scale);

            g2.drawImage(
                    picture,
                    x + (size - width) / 2,
                    y + (size - height) / 2,
                    width,
                    height,
                    null
            );

            g2.setClip(originalClip);
        }

        // Antique gold border
        boolean hovered = getModel().isRollover();

        g2.setColor(hovered
                ? new Color(255, 220, 150)
                : new Color(185, 150, 95));

        g2.setStroke(new BasicStroke(hovered ? 3f : 2f));
        g2.draw(circle);

        // Outer focus indicator
        if (hasFocus()) {
            g2.setColor(new Color(255, 230, 175));
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
        }

        g2.dispose();
    }
}
