package ui.match;

import model.Card;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * This class receives a card object and renders its image (BufferedImage) and its name (String). <br>
 * The image is rendered as the background, on top of it a black rectangle (R = 0, G = 0, B = 0). <br>
 * With a very high opacity (A = 150) as the background. On top of it the cards image is written. <br>
 * The card is purposely rendered with a rounded and diluted edge but  that ussesFont.Bold border.<br>
 */
public class CardRenderer extends JPanel {

    private final Card card;
    private final BufferedImage image;
    final int height;
    final int width;
    final int rotationDegrees;

    boolean hover = false;
    boolean isSelected = false;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        g2.rotate(
                Math.toRadians(rotationDegrees),
                100 + width / 2.0,
                150 + height / 2.0
        );

        // rounded card shape
        Shape cardShape = new RoundRectangle2D.Float(100, 150, width, height, 25, 25);

        // clip image to rounded corners
        g2.setClip(cardShape);

        // draw cached image without reading the file again during repaint
        g2.drawImage(image, 100, 150, width, height, null);

        // semi-transparent text rectangle - background (we basically set a high opacity and dont give while giving the parameters of black)
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(100, 395, width, 45);

        // text styling (we set a text on top of the image with a very opaque black rectangle as a base)
        g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 20));
        g2.setColor(Color.WHITE);

        // center the text horizontally
        FontMetrics fm = g2.getFontMetrics();
        int textX = 100 + (width - fm.stringWidth(card.getNameToRender())) / 2;

        g2.drawString(card.getNameToRender(), textX, 424);

        // restore clipping
        g2.setClip(null);

        // strong outer border
        g2.setColor(isSelected ? Color.RED : hover ? Color.BLUE : new Color(70, 70, 70, 190));
        g2.setStroke(new BasicStroke(5f));
        g2.draw(cardShape);

        g2.dispose();
    }

    public CardRenderer(Card card, int height, int width, int rotationDegrees) throws IOException {
        this.card = card;
        this.image = ImageIO.read(card.getAspectToRender().toFile());
        if (image == null) {
            throw new IOException("Unsupported card image: " + card.getAspectToRender());
        }
        this.height = height;
        this.width = width;
        this.rotationDegrees = rotationDegrees;

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

            //-----
            @Override
            public void mousePressed(MouseEvent e) {
                updateHover(e);

                if (hover) {
                    isSelected = !isSelected;
                    repaint();
                }
            }
        };

        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
    }

    private void updateHover(MouseEvent e) {
        double angle = Math.toRadians(rotationDegrees);

        double cx = 100 + width / 2.0;
        double cy = 150 + height / 2.0;

        double dx = e.getX() - cx;
        double dy = e.getY() - cy;

        double x = cx + dx * Math.cos(angle) + dy * Math.sin(angle);
        double y = cy - dx * Math.sin(angle) + dy * Math.cos(angle);

        Shape cardShape = new RoundRectangle2D.Float(
                100, 150, width, height, 25, 25
        );

        boolean inside = cardShape.contains(x, y);

        if (hover != inside) {
            hover = inside;
            repaint();
        }
    }
}
