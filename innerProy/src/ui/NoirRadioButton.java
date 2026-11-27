package ui;

import javax.swing.*;
import java.awt.*;

public final class NoirRadioButton extends JRadioButton {

    public NoirRadioButton(String text) {

        super(text);

        setFont(NoirTheme.BODY_FONT);
        setForeground(NoirTheme.TEXT);

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);

        setPreferredSize(new Dimension(180, 36));
        setMinimumSize(new Dimension(100, 36));
        setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 36)
        );

        setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        // BACKGROUND

        Color background = isSelected()
                ? NoirTheme.DARK_CRIMSON
                : NoirTheme.SURFACE;

        if (getModel().isPressed()) {

            background = NoirTheme.DARK_CRIMSON;

        } else if (getModel().isRollover()
                && !isSelected()) {

            background = NoirTheme.HOVER;
        }

        g2.setColor(background);

        g2.fillRoundRect(
                1, 1,
                getWidth() - 3,
                getHeight() - 3,
                8, 8
        );

        // BORDER

        g2.setColor(
                isSelected()
                        || getModel().isRollover()
                        || hasFocus()
                        ? NoirTheme.GOLD
                        : NoirTheme.INNER_BORDER
        );

        g2.drawRoundRect(
                1, 1,
                getWidth() - 3,
                getHeight() - 3,
                8, 8
        );

        // TEXT

        g2.setFont(getFont());

        g2.setColor(
                isEnabled()
                        ? NoirTheme.TEXT
                        : NoirTheme.SECONDARY_TEXT
        );

        FontMetrics fm = g2.getFontMetrics();

        int textY = (getHeight() - fm.getHeight()) / 2
                + fm.getAscent();

        int size = 14;

        int x = getWidth() - size - 12;
        int y = (getHeight() - size) / 2;

        Graphics2D textGraphics = (Graphics2D) g2.create();

        textGraphics.clipRect(
                12, 0,
                Math.max(0, x - 20),
                getHeight()
        );

        textGraphics.drawString(
                getText(), 12, textY
        );

        textGraphics.dispose();

        // RADIO CIRCLE

        g2.setColor(NoirTheme.GOLD);

        g2.setStroke(new BasicStroke(1.4f));

        g2.drawOval(x, y, size, size);

        // SELECTED INDICATOR

        if (isSelected()) {

            g2.fillOval(
                    x + 4,
                    y + 4,
                    size - 8,
                    size - 8
            );
        }

        g2.dispose();
    }
}