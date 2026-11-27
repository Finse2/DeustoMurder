package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public final class TurnTransitionOverlay extends JPanel {

    private static final int FADE_IN = 350;
    private static final int HOLD = 1200;
    private static final int FADE_OUT = 450;

    private static final int DURATION =
            FADE_IN + HOLD + FADE_OUT;

    private final Timer timer;

    private String playerName = "";

    private boolean localTurn;

    private long startTime;

    private float opacity = 0f;

    // CONSTRUCTOR

    public TurnTransitionOverlay() {

        setOpaque(false);
        setFocusable(false);
        setVisible(false);

        timer = new Timer(
                16,
                event -> updateAnimation()
        );
    }

    // START TRANSITION

    public void showTurn(
            String playerName,
            boolean localTurn
    ) {

        if (!SwingUtilities.isEventDispatchThread()) {

            SwingUtilities.invokeLater(
                    () -> showTurn(playerName, localTurn)
            );

            return;
        }

        timer.stop();

        this.playerName = playerName;
        this.localTurn = localTurn;

        opacity = 0f;

        startTime = System.nanoTime();

        setVisible(true);

        timer.start();

        repaint();
    }

    // UPDATE ANIMATION

    private void updateAnimation() {

        long elapsed =
                (System.nanoTime() - startTime) / 1_000_000;

        if (elapsed < FADE_IN) {

            opacity = (float) elapsed / FADE_IN;

        } else if (elapsed < FADE_IN + HOLD) {

            opacity = 1f;

        } else if (elapsed < DURATION) {

            opacity = 1f -
                    (float) (elapsed - FADE_IN - HOLD)
                            / FADE_OUT;

        } else {

            stop();

            return;
        }

        repaint();
    }

    // STOP TRANSITION

    public void stop() {

        timer.stop();

        opacity = 0f;

        setVisible(false);

        repaint();
    }

    // ALLOW MOUSE EVENTS TO REACH THE BOARD

    @Override
    public boolean contains(int x, int y) {

        return false;
    }

    // CUSTOM RENDERING

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        opacity
                )
        );

        int width = getWidth();
        int height = getHeight();

        // DARK BACKGROUND

        g2.setColor(new Color(10, 7, 9, 185));

        g2.fillRect(0, 0, width, height);

        // CENTRAL AREA

        int centerX = width / 2;
        int centerY = height / 2;

        int bannerHeight = 220;

        GradientPaint gradient = new GradientPaint(
                0,
                centerY - bannerHeight / 2,
                new Color(25, 19, 23, 100),
                0,
                centerY,
                new Color(25, 19, 23, 240),
                true
        );

        g2.setPaint(gradient);

        g2.fillRect(
                0,
                centerY - bannerHeight / 2,
                width,
                bannerHeight
        );

        // DECORATIVE LINES

        int lineWidth = (int) (width * 0.75);

        int lineStart = centerX - lineWidth / 2;

        g2.setColor(NoirTheme.GOLD);

        g2.setStroke(new BasicStroke(1.2f));

        g2.drawLine(
                lineStart,
                centerY - 100,
                lineStart + lineWidth,
                centerY - 100
        );

        g2.drawLine(
                lineStart,
                centerY + 100,
                lineStart + lineWidth,
                centerY + 100
        );

        // GAME TITLE

        drawCenteredText(
                g2,
                "D E U S T O M U R D E R",
                new Font("Georgia", Font.PLAIN, 13),
                NoirTheme.GOLD,
                centerX,
                centerY - 65
        );

        // SUBTITLE

        drawCenteredText(
                g2,
                "THE INVESTIGATION CONTINUES",
                new Font("Monospaced", Font.PLAIN, 11),
                NoirTheme.SECONDARY_TEXT,
                centerX,
                centerY - 30
        );

        // TURN TITLE

        String title = localTurn
                ? "YOUR TURN"
                : playerName.toUpperCase(Locale.ROOT) + "'S TURN";

        drawCenteredText(
                g2,
                title,
                new Font("Georgia", Font.BOLD, 36),
                NoirTheme.TEXT,
                centerX,
                centerY + 18
        );

        // PLAYER NAME OR DESCRIPTION

        String description = localTurn
                ? playerName + ", the truth awaits."
                : "Every move brings you closer to the truth.";

        drawCenteredText(
                g2,
                description,
                new Font("Georgia", Font.ITALIC, 14),
                NoirTheme.GOLD,
                centerX,
                centerY + 50
        );

        // FOOTER

        drawCenteredText(
                g2,
                "CONFIDENTIAL CASE FILE",
                new Font("Monospaced", Font.PLAIN, 9),
                NoirTheme.SECONDARY_TEXT,
                centerX,
                centerY + 80
        );

        g2.dispose();
    }

    // CENTERED TEXT

    private void drawCenteredText(
            Graphics2D g2,
            String text,
            Font font,
            Color color,
            int centerX,
            int y
    ) {

        g2.setFont(font);

        int availableWidth = Math.max(1, getWidth() - 24);

        while (g2.getFontMetrics().stringWidth(text)
                > availableWidth
                && font.getSize() > 8) {

            font = font.deriveFont(
                    (float) font.getSize() - 1
            );

            g2.setFont(font);
        }

        g2.setColor(color);

        FontMetrics fm = g2.getFontMetrics();

        int x = centerX - fm.stringWidth(text) / 2;

        g2.drawString(text, x, y);
    }
}