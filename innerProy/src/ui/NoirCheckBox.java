package ui;

import javax.swing.*;
import java.awt.*;

public final class NoirCheckBox extends JCheckBox {

    public NoirCheckBox(String text) {

        super(text);

        setFont(NoirTheme.BODY_FONT);
        setForeground(NoirTheme.TEXT);

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);

        setPreferredSize(new Dimension(180, 34));
        setMinimumSize(new Dimension(100, 34));
        setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 34)
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
                ? NoirTheme.PANEL
                : NoirTheme.SURFACE;

        if (getModel().isPressed()) {

            background = NoirTheme.DARK_CRIMSON;

        } else if (getModel().isRollover()) {

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
                getModel().isRollover() || hasFocus()
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
                !isEnabled() || isSelected()
                        ? NoirTheme.SECONDARY_TEXT
                        : NoirTheme.TEXT
        );

        FontMetrics fm = g2.getFontMetrics();

        int textX = 12;

        int textY = (getHeight() - fm.getHeight()) / 2
                + fm.getAscent();

        int boxSize = 15;

        int boxX = getWidth() - boxSize - 12;
        int boxY = (getHeight() - boxSize) / 2;

        Graphics2D textGraphics = (Graphics2D) g2.create();

        textGraphics.clipRect(
                textX, 0,
                Math.max(0, boxX - textX - 8),
                getHeight()
        );

        textGraphics.drawString(
                getText(), textX, textY
        );

        // STRIKETHROUGH

        if (isSelected()) {

            int textWidth = fm.stringWidth(getText());

            textGraphics.drawLine(
                    textX,
                    textY - fm.getAscent() / 3,
                    textX + textWidth,
                    textY - fm.getAscent() / 3
            );
        }

        textGraphics.dispose();

        // CHECKBOX

        g2.setColor(
                isSelected()
                        ? NoirTheme.SUCCESS
                        : NoirTheme.GOLD
        );

        g2.setStroke(new BasicStroke(1.3f));

        g2.drawRoundRect(
                boxX, boxY,
                boxSize, boxSize,
                4, 4
        );

        // CHECK MARK

        if (isSelected()) {

            g2.setStroke(
                    new BasicStroke(
                            2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g2.drawLine(
                    boxX + 3, boxY + 8,
                    boxX + 6, boxY + 11
            );

            g2.drawLine(
                    boxX + 6, boxY + 11,
                    boxX + 12, boxY + 4
            );
        }

        g2.dispose();
    }
}