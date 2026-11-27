package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public final class NoirToastManager {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 82;

    private static final int MARGIN = 18;
    private static final int GAP = 10;

    private static final int MAX_TOASTS = 3;

    private static final int DURATION = 5000;
    private static final int ANIMATION_DURATION = 220;

    private final JLayeredPane layeredPane;

    private final List<ToastPanel> notifications =
            new ArrayList<>();

    // NOTIFICATION TYPES

    public enum Type {

        INFO(NoirTheme.GOLD, "\u24D8"),

        SUCCESS(NoirTheme.SUCCESS, "\u2713"),

        WARNING(new Color(220, 170, 90), "!"),

        ERROR(NoirTheme.DANGER, "\u00D7"),

        TURN(NoirTheme.GOLD, "\u25B6"),

        EVIDENCE(new Color(101, 187, 135), "+"),

        ACHIEVEMENT(new Color(230, 189, 99), "\u2605");

        private final Color color;
        private final String symbol;

        Type(Color color, String symbol) {

            this.color = color;
            this.symbol = symbol;
        }
    }

    // CONSTRUCTOR

    public NoirToastManager(JLayeredPane layeredPane) {

        this.layeredPane = layeredPane;

        layeredPane.addComponentListener(
                new ComponentAdapter() {

                    @Override
                    public void componentResized(ComponentEvent e) {

                        updatePositions();
                    }
                }
        );
    }

    // SHOW NOTIFICATION

    public void show(
            Type type,
            String title,
            String message
    ) {

        if (!SwingUtilities.isEventDispatchThread()) {

            SwingUtilities.invokeLater(
                    () -> show(type, title, message)
            );

            return;
        }

        // LIMIT SIMULTANEOUS NOTIFICATIONS

        while (notifications.size() >= MAX_TOASTS) {

            ToastPanel oldest = notifications.get(0);

            removeToast(oldest);
        }

        ToastPanel toast = new ToastPanel(
                type,
                title,
                message
        );

        notifications.add(toast);

        layeredPane.add(
                toast,
                JLayeredPane.POPUP_LAYER,
                0
        );

        updatePositions();

        toast.start();

        layeredPane.repaint();
    }

    // UPDATE POSITIONS

    private void updatePositions() {

        int width = Math.max(
                1,
                Math.min(
                        WIDTH,
                        layeredPane.getWidth() - MARGIN * 2
                )
        );

        int x = Math.max(
                0,
                layeredPane.getWidth() - width - MARGIN
        );

        int y = MARGIN;

        for (int i = notifications.size() - 1; i >= 0; i--) {

            ToastPanel toast = notifications.get(i);

            toast.setBounds(
                    x,
                    y,
                    width,
                    HEIGHT
            );

            y += HEIGHT + GAP;
        }

        layeredPane.repaint();
    }

    // REMOVE NOTIFICATION

    private void removeToast(ToastPanel toast) {

        toast.stop();

        notifications.remove(toast);

        layeredPane.remove(toast);

        updatePositions();

        layeredPane.revalidate();
        layeredPane.repaint();
    }

    // CLEAR ALL

    public void clear() {

        if (!SwingUtilities.isEventDispatchThread()) {

            SwingUtilities.invokeLater(this::clear);

            return;
        }

        for (ToastPanel toast : new ArrayList<>(notifications)) {

            removeToast(toast);
        }
    }

    // TOAST PANEL

    private final class ToastPanel extends JPanel {

        private final Type type;

        private final Timer timer;

        private long createdAt;

        private long closingAt = -1;

        private float opacity = 0f;

        private float progress = 1f;

        private ToastPanel(
                Type type,
                String title,
                String message
        ) {

            this.type = type;

            setLayout(new BorderLayout(10, 0));

            setOpaque(false);

            setBorder(
                    BorderFactory.createEmptyBorder(
                            10, 12, 14, 10
                    )
            );

            // ICON

            JLabel icon = new JLabel(
                    type.symbol,
                    SwingConstants.CENTER
            );

            icon.setFont(
                    new Font("Dialog", Font.BOLD, 24)
            );

            icon.setForeground(type.color);

            icon.setPreferredSize(
                    new Dimension(38, 38)
            );

            add(icon, BorderLayout.WEST);

            // CONTENT

            JPanel content = new JPanel(
                    new BorderLayout(0, 4)
            );

            content.setOpaque(false);

            JLabel titleLabel = new JLabel(title);

            titleLabel.setFont(
                    new Font("Arial", Font.BOLD, 12)
            );

            titleLabel.setForeground(type.color);

            JTextArea messageLabel = new JTextArea(message);

            messageLabel.setFont(NoirTheme.BODY_FONT);
            messageLabel.setForeground(NoirTheme.TEXT);

            messageLabel.setOpaque(false);
            messageLabel.setEditable(false);
            messageLabel.setFocusable(false);

            messageLabel.setLineWrap(true);
            messageLabel.setWrapStyleWord(true);

            messageLabel.setBorder(null);

            content.add(titleLabel, BorderLayout.NORTH);

            content.add(messageLabel, BorderLayout.CENTER);

            add(content, BorderLayout.CENTER);

            // CLOSE BUTTON

            JButton closeButton = new JButton("\u00D7");

            closeButton.setFont(
                    new Font("Arial", Font.PLAIN, 18)
            );

            closeButton.setForeground(
                    NoirTheme.SECONDARY_TEXT
            );

            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);

            closeButton.setPreferredSize(
                    new Dimension(22, 24)
            );

            closeButton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            closeButton.setToolTipText(
                    "Dismiss notification"
            );

            closeButton.addActionListener(
                    event -> close()
            );

            JPanel closePanel = new JPanel(
                    new BorderLayout()
            );

            closePanel.setOpaque(false);

            closePanel.add(
                    closeButton,
                    BorderLayout.NORTH
            );

            add(closePanel, BorderLayout.EAST);

            // ANIMATION TIMER

            timer = new Timer(
                    16,
                    event -> updateAnimation()
            );
        }

        private void start() {

            createdAt = System.currentTimeMillis();

            timer.start();
        }

        private void stop() {

            timer.stop();
        }

        private void close() {

            if (closingAt == -1) {

                closingAt = System.currentTimeMillis();
            }
        }

        private void updateAnimation() {

            long now = System.currentTimeMillis();

            long elapsed = now - createdAt;

            progress = Math.max(
                    0f,
                    1f - (float) elapsed / DURATION
            );

            // FADE IN

            if (closingAt == -1) {

                opacity = Math.min(
                        1f,
                        (float) elapsed / ANIMATION_DURATION
                );

                if (elapsed >= DURATION) {

                    close();
                }
            }

            // FADE OUT

            if (closingAt != -1) {

                long closingElapsed = now - closingAt;

                opacity = Math.max(
                        0f,
                        1f - (float) closingElapsed
                                / ANIMATION_DURATION
                );

                if (opacity <= 0f) {

                    removeToast(this);

                    return;
                }
            }

            repaint();
        }

        // APPLY OPACITY TO THE ENTIRE PANEL

        @Override
        public void paint(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            opacity
                    )
            );

            super.paint(g2);

            g2.dispose();
        }

        // CUSTOM BACKGROUND

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            // SHADOW

            g2.setColor(new Color(0, 0, 0, 80));

            g2.fillRoundRect(
                    3, 3,
                    width - 3,
                    height - 3,
                    12, 12
            );

            // BACKGROUND

            g2.setColor(NoirTheme.PANEL);

            g2.fillRoundRect(
                    0, 0,
                    width - 4,
                    height - 4,
                    10, 10
            );

            // BORDER

            g2.setColor(type.color);

            g2.drawRoundRect(
                    0, 0,
                    width - 5,
                    height - 5,
                    10, 10
            );

            // LEFT ACCENT

            g2.fillRoundRect(
                    3, 12,
                    3, height - 28,
                    3, 3
            );

            // PROGRESS BACKGROUND

            g2.setColor(NoirTheme.SURFACE);

            g2.fillRoundRect(
                    12,
                    height - 11,
                    width - 28,
                    3,
                    3, 3
            );

            // PROGRESS

            g2.setColor(type.color);

            g2.fillRoundRect(
                    12,
                    height - 11,
                    (int) ((width - 28) * progress),
                    3,
                    3, 3
            );

            g2.dispose();
        }
    }
}