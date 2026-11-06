
package ui.match;

import model.Card;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/**
 * This class receives a card object and renders its image (BufferedImage) and its name (String). <br>
 * The image is rendered as the background, on top of it a black rectangle (R = 0, G = 0, B = 0). <br>
 * With a very high opacity (A = 150) as the background. On top of it the cards image is written. <br>
 * The card is purposely rendered with a rounded and diluted edge but  that ussesFont.Bold border.<br>
 */
public class CardRenderer extends JPanel {

    private final Card card;
    private final BufferedImage image;
    private final BufferedImage circleImage;
    final int height;
    final int width;
    final int rotationDegrees;

    boolean hover = false;
    boolean isSelected = false;

    private static final double LEFT = 100;
    private static final double TOP = 150;
    private static final double CORNER_RADIUS = 28;

    public CardRenderer(Card card, int height, int width, int rotationDegrees) throws IOException {
        this.card = card;
        this.image = card.getMainBackgroundImage() != null
                ? card.getMainBackgroundImage()
                : readImage(card.getAspectToRender());
        this.circleImage = card.getFrontCircleImage() != null
                ? card.getFrontCircleImage()
                : card.getCircleImageToRender() == null
                  ? null
                  : readImage(card.getCircleImageToRender());

        this.height = height;
        this.width = width;
        this.rotationDegrees = rotationDegrees;
        setOpaque(false);

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateHover(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                updateHover(e);
                if (hover) {
                    isSelected = !isSelected;
                    repaint();
                }
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    private static BufferedImage readImage(Path path) throws IOException {
        if (path == null) {
            throw new IOException("Missing card image path");
        }
        BufferedImage result = ImageIO.read(path.toFile());
        if (result == null) {
            throw new IOException("Unsupported card image: " + path);
        }
        return result;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            double centerX = LEFT + width / 2.0;
            double centerY = TOP + height / 2.0;
            g2.rotate(Math.toRadians(rotationDegrees), centerX, centerY);

            Color base = card.getCardBackGroundColor() == null ? new Color(9, 39, 62) : card.getCardBackGroundColor();
            double borderWidth = Math.max(6, Math.min(10, width * 0.04));

            Shape outerShape = new RoundRectangle2D.Double(LEFT, TOP, width, height, CORNER_RADIUS, CORNER_RADIUS);
            Shape innerShape = new RoundRectangle2D.Double(LEFT + borderWidth, TOP + borderWidth, width - borderWidth * 2, height - borderWidth * 2, CORNER_RADIUS - 5, CORNER_RADIUS - 5);

            // Subtle shadow and a single solid, rounded border.
            g2.setColor(new Color(0, 0, 0, 48));
            g2.fill(new RoundRectangle2D.Double(LEFT + 2, TOP + 4, width, height, CORNER_RADIUS, CORNER_RADIUS));

            g2.setColor(brightness(base, 0.72));
            g2.fill(outerShape);
            g2.setColor(base);
            g2.fill(innerShape);

            double imageX = LEFT + width * 0.075;
            double imageY = TOP + height * 0.125;
            double imageW = width * 0.85;
            double imageH = height * 0.635;
            Shape picture = new RoundRectangle2D.Double(imageX, imageY, imageW, imageH, 15, 15);

            // Rounded image without additional decorative frames.
            Shape oldClip = g2.getClip();
            g2.clip(picture);
            drawCover(g2, image, imageX, imageY, imageW, imageH);
            g2.setClip(oldClip);

            String category = card.getCardType();
            int headingSize = Math.max(11, (int) Math.round(width * 0.073));
            g2.setFont(new Font("Serif", Font.BOLD, headingSize));
            FontMetrics headingMetrics = g2.getFontMetrics();
            while (headingMetrics.stringWidth(category) > width * 0.75 && headingSize > 9) {g2.setFont(new Font("Serif", Font.BOLD, --headingSize));headingMetrics = g2.getFontMetrics();}
            int headingX = (int) Math.round(centerX - headingMetrics.stringWidth(category) / 2.0);
            int headingY = (int) Math.round(TOP + height * 0.085);
            g2.setColor(new Color(0, 0, 0, 75));
            g2.drawString(category, headingX, headingY + 1);
            g2.setColor(new Color(238, 241, 243));
            g2.drawString(category, headingX, headingY);

            // Simple circular image without a double border.
            double badgeY = TOP + height * 0.755;
            double badgeRadius = width * 0.118;
            Shape badge = new Ellipse2D.Double(centerX - badgeRadius, badgeY - badgeRadius, badgeRadius * 2, badgeRadius * 2);
            g2.setColor(brightness(base, 0.82));
            g2.fill(badge);
            if (circleImage != null) {
                g2.clip(badge);
                drawContain(g2, circleImage, centerX - badgeRadius, badgeY - badgeRadius, badgeRadius * 2, badgeRadius * 2);
                g2.setClip(oldClip);
            }

            String name = card.getNameToRender() == null ? "" : card.getNameToRender().toUpperCase();
            int nameSize = Math.max(12, (int) Math.round(width * 0.123));
            g2.setFont(new Font("Serif", Font.PLAIN, nameSize));
            FontMetrics nameMetrics = g2.getFontMetrics();
            while (nameMetrics.stringWidth(name) > width * 0.82 && nameSize > 10) {g2.setFont(new Font("Serif", Font.PLAIN, --nameSize));nameMetrics = g2.getFontMetrics();}
            int nameX = (int) Math.round(centerX - nameMetrics.stringWidth(name) / 2.0);
            int nameY = (int) Math.round(TOP + height * 0.94);
            g2.setColor(new Color(0, 0, 0, 95));
            g2.drawString(name, nameX, nameY + 1);
            g2.setColor(new Color(242, 243, 244));
            g2.drawString(name, nameX, nameY);

            // Only hover and selection add an accent; the normal border has none.
            if (isSelected || hover) {g2.setColor(isSelected ? new Color(232, 92, 82) : new Color(123, 188, 219));g2.setStroke(new BasicStroke(2.5f));g2.draw(outerShape);}
        } finally {
            g2.dispose();
        }
    }

    private static Color brightness(Color color, double scale) {
        return new Color(Math.min(255, (int) Math.round(color.getRed() * scale)), Math.min(255, (int) Math.round(color.getGreen() * scale)), Math.min(255, (int) Math.round(color.getBlue() * scale)));
    }

    private static void drawCover(Graphics2D g2, BufferedImage source,
                                  double x, double y, double w, double h) {
        double imageRatio = (double) source.getWidth() / source.getHeight();
        double rectRatio = w / h;
        int srcX = 0, srcY = 0, srcW = source.getWidth(), srcH = source.getHeight();
        if (imageRatio > rectRatio) {
            srcW = (int) Math.round(srcH * rectRatio);
            srcX = (source.getWidth() - srcW) / 2;
        } else {
            srcH = (int) Math.round(srcW / rectRatio);
            srcY = (source.getHeight() - srcH) / 2;
        }
        g2.drawImage(source, (int) Math.round(x), (int) Math.round(y), (int) Math.round(x + w), (int) Math.round(y + h), srcX, srcY, srcX + srcW, srcY + srcH, null);
    }

    private static void drawContain(Graphics2D g2, BufferedImage source, double x, double y, double w, double h) {
        double scale = Math.min(w / source.getWidth(), h / source.getHeight());
        int destW = (int) Math.round(source.getWidth() * scale);
        int destH = (int) Math.round(source.getHeight() * scale);
        int destX = (int) Math.round(x + (w - destW) / 2);
        int destY = (int) Math.round(y + (h - destH) / 2);
        g2.drawImage(source, destX, destY, destW, destH, null);
    }

    private void updateHover(MouseEvent e) {
        double angle = Math.toRadians(rotationDegrees);
        double cx = LEFT + width / 2.0;
        double cy = TOP + height / 2.0;
        double dx = e.getX() - cx;
        double dy = e.getY() - cy;
        double x = cx + dx * Math.cos(angle) + dy * Math.sin(angle);
        double y = cy - dx * Math.sin(angle) + dy * Math.cos(angle);
        Shape cardShape = new RoundRectangle2D.Double(LEFT, TOP, width, height, CORNER_RADIUS, CORNER_RADIUS);
        boolean inside = cardShape.contains(x, y);
        if (hover != inside) {
            hover = inside;
            repaint();
        }
    }
}
