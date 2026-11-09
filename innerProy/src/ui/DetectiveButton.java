package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Botón amarillo de un único borde para abrir la tarjeta de marcaje. */
public class DetectiveButton extends JButton {

    private static final Color YELLOW = new Color(235, 205, 105);
    private static final Color YELLOW_HOVER = new Color(245, 216, 119);
    private static final Color YELLOW_PRESSED = new Color(219, 185, 85);
    private static final Color INK = new Color(40, 34, 25);

    private boolean mouseOver;
    private boolean pressed;

    public DetectiveButton() {
        super("Tarjeta del detective");
        setFont(new Font("Serif", Font.BOLD, 17));
        setForeground(INK);
        setPreferredSize(new Dimension(310, 70));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                mouseOver = true;
                SoundEffect.playHover();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                mouseOver = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    pressed = true;
                    SoundEffect.playClick();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color fill = pressed ? YELLOW_PRESSED : mouseOver ? YELLOW_HOVER : YELLOW;
        int width = getWidth();
        int height = getHeight();

        // Un único relleno amarillo y un único contorno oscuro, sin dorados ni sombras.
        g.setColor(fill);
        g.fillRoundRect(2, 2, width - 5, height - 5, 12, 12);
        g.setColor(INK);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(2, 2, width - 5, height - 5, 12, 12);

        g.setFont(getFont());
        FontMetrics metrics = g.getFontMetrics();
        int baseline = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(getText(), 20, baseline);

        // Lupa a la derecha, sin bordes decorativos adicionales.
        int glassX = width - 53;
        int glassY = (height - 28) / 2 - 5;
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(glassX, glassY, 25, 25);
        g.drawLine(glassX + 21, glassY + 22, glassX + 36, glassY + 37);
        g.dispose();
    }
}
