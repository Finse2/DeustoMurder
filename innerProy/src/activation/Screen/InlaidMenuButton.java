package activation.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InlaidMenuButton extends JPanel {

    private final String text;
    private final Runnable action;
    private boolean hovered = false;
    private boolean pressed = false;

    public InlaidMenuButton(String text, Runnable action) {
        this.text = text;
        this.action = action;

        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setPreferredSize(new Dimension(230,60));

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressed = true;
                    repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                boolean activate = pressed && contains(e.getPoint()) && SwingUtilities.isLeftMouseButton(e);
                pressed = false;
                repaint();
                if (activate) {
                    action.run();
                }
            }

        };

        addMouseListener(mouse);

        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "activate");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "activate");

        getActionMap().put("activate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });

        getAccessibleContext().setAccessibleName(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int w = getWidth();
        int h = getHeight();

        // Dark translucent interior
        int alpha = pressed ? 145 : hovered ? 90 : 55;

        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRoundRect(2, 2, w - 4, h - 4, 15, 15);

        // Dark inner upper edge
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawLine(12, 3, w - 12, 3);
        g2.drawLine(3, 12, 3, h - 12);

        // Light lower edge
        g2.setColor(new Color(220, 190, 150, 85));
        g2.drawLine(12, h - 3, w - 12, h - 3);
        g2.drawLine(w - 3, 12, w - 3, h - 12);

        // Text
        g2.setFont(new Font("Georgia", Font.BOLD, 19));

        g2.setColor(hovered
                ? new Color(255, 230, 185)
                : new Color(215, 190, 150));

        FontMetrics fm = g2.getFontMetrics();

        int x = (w - fm.stringWidth(text)) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();

        if (pressed) {
            y += 2;
        }

        g2.drawString(text, x, y);

        g2.dispose();
    }
}

