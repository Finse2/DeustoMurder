package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class NoirActionButton extends JButton {

    public enum Style {
        PRIMARY,
        SECONDARY,
        DANGER
    }

    private static final Color GOLD = NoirTheme.GOLD;
    private static final Color BACKGROUND = NoirTheme.BACKGROUND;
    private static final Color TEXT = NoirTheme.TEXT;

    private final String subtitle;
    private final Style style;

    public NoirActionButton(
            String title,
            String subtitle,
            Style style
    ) {

        super(title);

        this.subtitle = subtitle;
        this.style = style;

        setOpaque(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setRolloverEnabled(true);

        setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        setPreferredSize(new Dimension(280, 44));
        setMinimumSize(new Dimension(160, 44));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        setAlignmentX(Component.CENTER_ALIGNMENT);

        setToolTipText(subtitle);

        // SOUND EFFECTS
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent event) {
                if (isEnabled()) {
                    SoundEffect.playHover();
                }
            }
        });

        addActionListener(event -> SoundEffect.playClick());
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        Graphics2D g2 = (Graphics2D) graphics.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        int width = getWidth();
        int height = getHeight();

        // BUTTON COLORS

        Color background = switch (style) {
            case PRIMARY -> NoirTheme.DARK_CRIMSON;
            case SECONDARY -> NoirTheme.SURFACE;
            case DANGER -> NoirTheme.BACKGROUND;
        };

        Color foreground = switch (style) {
            case PRIMARY -> NoirTheme.TEXT;
            case SECONDARY -> NoirTheme.TEXT;
            case DANGER -> new Color(219, 135, 145);
        };

        Color border = switch (style) {
            case PRIMARY -> NoirTheme.GOLD;
            case SECONDARY -> NoirTheme.BORDER;
            case DANGER -> NoirTheme.CRIMSON;
        };

        // INTERACTION STATES

        if (!isEnabled()) {

            background = NoirTheme.PANEL;
            foreground = new Color(105, 92, 94);
            border = NoirTheme.INNER_BORDER;

        } else if (getModel().isPressed()) {

            background = background.darker();

        } else if (getModel().isRollover()
                || isSelected()) {

            background = background.brighter();
        }

        // BACKGROUND

        g2.setColor(background);

        g2.fillRoundRect(
                1, 1,
                width - 3,
                height - 3,
                12, 12
        );

        // BORDER

        g2.setColor(border);

        g2.setStroke(
                new BasicStroke(hasFocus() ? 2f : 1f)
        );

        g2.drawRoundRect(
                1, 1,
                width - 3,
                height - 3,
                12, 12
        );

        // ICON

        g2.setColor(foreground);

        drawIcon(
                g2,
                14,
                height / 2 - 10
        );

        // TITLE

        g2.setFont(new Font(
                "Georgia", Font.BOLD, 13
        ));

        g2.drawString(getText(), 45, 18);

        // SUBTITLE

        g2.setFont(new Font(
                "Arial", Font.PLAIN, 10
        ));

        g2.setColor(
                isEnabled() && style != Style.PRIMARY
                        ? TEXT
                        : foreground
        );

        g2.drawString(subtitle, 45, 32);

        // RIGHT ARROW

        g2.setColor(foreground);

        g2.setStroke(new BasicStroke(
                1.5f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        int arrowX = width - 16;
        int arrowY = height / 2;

        g2.drawLine(
                arrowX - 4, arrowY - 5,
                arrowX + 1, arrowY
        );

        g2.drawLine(
                arrowX + 1, arrowY,
                arrowX - 4, arrowY + 5
        );

        g2.dispose();
    }

    private void drawIcon(
            Graphics2D graphics,
            int x,
            int y
    ) {

        Graphics2D g2 = (Graphics2D) graphics.create();

        g2.translate(x, y);

        g2.setStroke(new BasicStroke(
                1.6f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        switch (style) {

            case PRIMARY -> {

                // DICE

                g2.drawRoundRect(
                        1, 1, 18, 18, 5, 5
                );

                g2.fillOval(4, 4, 3, 3);
                g2.fillOval(13, 4, 3, 3);
                g2.fillOval(8, 8, 3, 3);
                g2.fillOval(4, 13, 3, 3);
                g2.fillOval(13, 13, 3, 3);
            }

            case SECONDARY -> {

                // NOTEBOOK

                g2.drawRoundRect(
                        4, 1, 15, 19, 3, 3
                );

                for (int i = 4; i <= 16; i += 4) {
                    g2.drawLine(1, i, 6, i);
                }

                g2.drawLine(9, 6, 15, 6);
                g2.drawLine(9, 10, 15, 10);
                g2.drawLine(9, 14, 13, 14);
            }

            case DANGER -> {

                // ACCUSATION

                g2.drawOval(1, 1, 18, 18);

                g2.drawLine(10, 5, 10, 12);

                g2.fillOval(9, 15, 2, 2);
            }
        }

        g2.dispose();
    }
}