package ui.match.inGameScreen;

import game.dice.Dice;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Panel de animación para dos dados antiguos.<br>
 *<br>
 * Interacción:<br>
 * - mantener pulsado el botón izquierdo -> los dados se agitan y cambian de cara<br>
 * - soltar el botón -> los dados caen y terminan mostrando la tirada real<br>
 * <br>
 * las funciones que utilizo son:<br>
 * <br>
 * x += Math.sin(t * 1.85) * size * 0.10; // oscilacion sinusoidal horizontal<br>
 *<br>
 * y -= size * 0.92 * (1.0 - fall * fall * fall); // interpolacion cubica de caida<br>
 *<br>
 * y -= Math.abs(Math.sin(bounce * Math.PI * 2.7)) * size * 0.24 * (1.0 - bounce); // rebote sinusoidal amortiguado<br>
 *<br>
 * double px = leftX * (1.0 - u) + rightX * u; // interpolacion lineal<br>
 */
public final class DiceRollPanel extends JPanel {

    private enum Phase { READY, HOLDING, FALLING, SETTLED }

    private static final Color GOLD = new Color(205, 165, 102);
    private static final Color CREAM = new Color(239, 218, 177);
    private static final int FRAME_DELAY_MS = 16;
    private static final double HOLD_FACE_SWITCH_NS = 78_000_000.0;
    private static final double FALL_FACE_SWITCH_NS = 105_000_000.0;

    private final Dice dice;
    private final Random visualRandom;
    private final Timer timer;

    // Caras visibles en cada dado.
    private final int[] visibleTopFaces = {1, 5};
    private final int[] visibleFrontFaces = {2, 3};
    private final int[] visibleRightFaces = {4, 6};

    // Objetivos finales al asentarse los dados.
    private final int[] targetTopFaces = {0, 0};
    private final int[] targetFrontFaces = {0, 0};
    private final int[] targetRightFaces = {0, 0};

    private final int[] stopTimes = {0, 0};

    private Phase phase = Phase.READY;
    private long phaseStartedNanos = System.nanoTime();
    private long lastFaceChangeNanos = phaseStartedNanos;

    private int firstResult;
    private int secondResult;

    private BiConsumer<Integer, Integer> onRollComplete;

    public DiceRollPanel() {
        this(new Dice(), new Random());
    }

    public DiceRollPanel(Dice dice, Random visualRandom) {
        if (dice == null || visualRandom == null) {
            throw new IllegalArgumentException("Dice and visualRandom cannot be null");
        }
        this.dice = dice;
        this.visualRandom = visualRandom;
        this.timer = new Timer(FRAME_DELAY_MS, event -> animate());

        setOpaque(false);
        setPreferredSize(new Dimension(700, 450));
        setMinimumSize(new Dimension(320, 260));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) beginHold();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) releaseAndRoll();
            }
        });
    }

    // ESTADO PÚBLICO

    /** Empieza la fase de agitación si no hay una caída en curso. */
    public void beginHold() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::beginHold);
            return;
        }
        if (phase == Phase.FALLING || phase == Phase.HOLDING) return;

        firstResult = 0;
        secondResult = 0;

        phase = Phase.HOLDING;
        phaseStartedNanos = System.nanoTime();
        lastFaceChangeNanos = phaseStartedNanos;

        for (int i = 0; i < 2; i++) {
            randomizeVisibleFaces(i);
        }

        timer.start();
        repaint();
    }

    /** Suelta los dados y comienza la caída. */
    public void releaseAndRoll() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::releaseAndRoll);
            return;
        }
        if (phase != Phase.HOLDING) return;

        dice.roll();
        targetTopFaces[0] = dice.getFirstDie();
        targetTopFaces[1] = dice.getSecondDie();

        for (int i = 0; i < 2; i++) {
            targetFrontFaces[i] = randomDistinctFace(targetTopFaces[i]);
            targetRightFaces[i] = randomDistinctFace(targetTopFaces[i], targetFrontFaces[i]);
        }

        phase = Phase.FALLING;
        phaseStartedNanos = System.nanoTime();
        lastFaceChangeNanos = phaseStartedNanos;

        stopTimes[0] = 1200 + visualRandom.nextInt(900);
        stopTimes[1] = 1350 + visualRandom.nextInt(950);

        timer.start();
        repaint();
    }

    public void setOnRollComplete(BiConsumer<Integer, Integer> listener) {
        this.onRollComplete = listener;
    }

    public boolean isRolling() {
        return phase == Phase.FALLING;
    }

    public boolean isHolding() {
        return phase == Phase.HOLDING;
    }

    public boolean isSettled() {
        return phase == Phase.SETTLED;
    }

    public int getFirstDie() {
        return isSettled() ? firstResult : 0;
    }

    public int getSecondDie() {
        return isSettled() ? secondResult : 0;
    }

    public int getTotal() {
        return isSettled() ? firstResult + secondResult : 0;
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    // ANIMACIÓN

    private void animate() {
        long now = System.nanoTime();

        if (phase == Phase.HOLDING) {
            if (now - lastFaceChangeNanos >= HOLD_FACE_SWITCH_NS) {
                for (int i = 0; i < 2; i++) {
                    randomizeVisibleFaces(i);
                }
                lastFaceChangeNanos = now;
            }
            repaint();
            return;
        }

        if (phase == Phase.FALLING) {
            if (now - lastFaceChangeNanos >= FALL_FACE_SWITCH_NS) {
                for (int i = 0; i < 2; i++) {
                    if (getElapsedMs(now) < stopTimes[i]) {
                        randomizeVisibleFaces(i);
                    }
                }
                lastFaceChangeNanos = now;
            }

            for (int i = 0; i < 2; i++) {
                if (getElapsedMs(now) >= stopTimes[i]) {
                    visibleTopFaces[i] = targetTopFaces[i];
                    visibleFrontFaces[i] = targetFrontFaces[i];
                    visibleRightFaces[i] = targetRightFaces[i];
                }
            }

            if (getElapsedMs(now) >= Math.max(stopTimes[0], stopTimes[1])) {
                visibleTopFaces[0] = targetTopFaces[0];
                visibleTopFaces[1] = targetTopFaces[1];
                visibleFrontFaces[0] = targetFrontFaces[0];
                visibleFrontFaces[1] = targetFrontFaces[1];
                visibleRightFaces[0] = targetRightFaces[0];
                visibleRightFaces[1] = targetRightFaces[1];

                firstResult = targetTopFaces[0];
                secondResult = targetTopFaces[1];

                phase = Phase.SETTLED;
                timer.stop();
                repaint();

                if (onRollComplete != null) {
                    onRollComplete.accept(firstResult, secondResult);
                }
                return;
            }

            repaint();
            return;
        }

        timer.stop();
    }

    private void randomizeVisibleFaces(int dieIndex) {
        int top = 1 + visualRandom.nextInt(6);
        int front = randomDistinctFace(top);
        int right = randomDistinctFace(top, front);

        visibleTopFaces[dieIndex] = top;
        visibleFrontFaces[dieIndex] = front;
        visibleRightFaces[dieIndex] = right;
    }

    private int randomDistinctFace(int... excluded) {
        while (true) {
            int candidate = 1 + visualRandom.nextInt(6);
            boolean repeated = false;
            for (int value : excluded) {
                if (candidate == value) {
                    repeated = true;
                    break;
                }
            }
            if (!repeated) return candidate;
        }
    }

    private double getElapsedMs(long now) {
        return (now - phaseStartedNanos) / 1_000_000.0;
    }

    // DIBUJO GENERAL

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0) return;

            drawBackground(g, width, height);
            drawTitle(g, width, height);

            double nowMs = getElapsedMs(System.nanoTime());
            double size = Math.max(55, Math.min(width / 4.25, height / 2.7));
            double groundY = height * 0.60;

            for (int i = 0; i < 2; i++) {
                double x = width * (i == 0 ? 0.34 : 0.66);
                double y = groundY;
                double angle = 0;
                double scale = 1;
                double phaseShift = i * 1.9;

                if (phase == Phase.HOLDING) {
                    double t = nowMs / 120.0 + phaseShift;
                    x += Math.sin(t * 1.85) * size * 0.10;
                    y -= size * 0.44 + Math.abs(Math.sin(t * 1.25)) * size * 0.12;
                    angle = Math.sin(t * 2.55) * 0.16;
                    scale = 0.96 + Math.sin(t * 1.55) * 0.035;
                } else if (phase == Phase.FALLING) {
                    double delay = i * 110.0;
                    double duration = Math.max(1.0, stopTimes[i] - delay);
                    double progress = Math.max(0.0, Math.min(1.0, (nowMs - delay) / duration));

                    if (progress < 0.58) {
                        double fall = progress / 0.58;
                        y -= size * 0.92 * (1.0 - fall * fall * fall);
                    } else {
                        double bounce = (progress - 0.58) / 0.42;
                        y -= Math.abs(Math.sin(bounce * Math.PI * 2.7)) * size * 0.24 * (1.0 - bounce);
                    }

                    angle = Math.sin(progress * 18.0 + phaseShift) * 0.55 * (1.0 - progress);
                    x += Math.sin(progress * 11.0 + phaseShift) * size * 0.06 * (1.0 - progress);
                }

                drawShadow(g, x, groundY + size * 0.47, size, y, groundY);
                drawDie(g, x, y, size * scale, angle,
                        visibleTopFaces[i], visibleFrontFaces[i], visibleRightFaces[i]);
            }

            drawStatus(g, width, height);
        } finally {
            g.dispose();
        }
    }

    private static void drawBackground(Graphics2D g, int width, int height) {
        g.setPaint(new GradientPaint(0, 0, new Color(12, 9, 10, 120),
                0, height, new Color(5, 3, 5, 190)));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(180, 136, 82, 90));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(24, 28, Math.max(24, width - 24), 28);
        g.drawLine(24, Math.max(28, height - 32), Math.max(24, width - 24), Math.max(28, height - 32));
        for (int x : new int[]{30, Math.max(30, width - 30)}) {
            g.drawOval(x - 4, 24, 8, 8);
        }
    }

    private static void drawTitle(Graphics2D g, int width, int height) {
        int fontSize = Math.max(16, Math.min(25, height / 18));
        g.setFont(new Font("Georgia", Font.BOLD, fontSize));
        g.setColor(GOLD);
        drawCentered(g, "LOS DADOS DEL DESTINO", width / 2, Math.max(55, height / 7));
    }

    private void drawStatus(Graphics2D g, int width, int height) {
        String text = switch (phase) {
            case READY -> "Mantén pulsado para agitar los dados";
            case HOLDING -> "Suelta el ratón para lanzarlos";
            case FALLING -> "El destino está decidiendo...";
            case SETTLED -> firstResult + "  +  " + secondResult + "  =  " + (firstResult + secondResult);
        };

        g.setColor(phase == Phase.SETTLED ? CREAM : new Color(196, 182, 166));
        g.setFont(new Font("Georgia", phase == Phase.SETTLED ? Font.BOLD : Font.ITALIC,
                Math.max(15, Math.min(23, height / 19))));
        drawCentered(g, text, width / 2, height - 57);

        g.setFont(new Font("Georgia", Font.PLAIN, 13));
        g.setColor(new Color(170, 146, 121));
        if (phase == Phase.SETTLED) {
            drawCentered(g, "Vuelve a mantener pulsado para otra tirada", width / 2, height - 37);
        } else if (phase == Phase.READY) {
            drawCentered(g, "Los dados caerán al soltar", width / 2, height - 37);
        }
    }

    private static void drawCentered(Graphics2D g, String text, int centerX, int baseline) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baseline);
    }

    private static void drawShadow(Graphics2D g, double x, double y, double size, double dieY, double groundY) {
        double opacity = Math.max(0.25, 1.0 - (groundY - dieY) / (size * 2.1));
        for (int i = 5; i >= 0; i--) {
            int alpha = (int) ((21 - i * 2) * opacity);
            g.setColor(new Color(0, 0, 0, Math.max(0, alpha)));
            double spread = i * 6.0;
            g.fill(new Ellipse2D.Double(
                    x - size * 0.53 - spread / 2.0,
                    y - size * 0.05 - spread / 3.0,
                    size * 1.06 + spread,
                    size * 0.22 + spread / 2.0
            ));
        }
    }

    //DIBUJO DEL DADO

    private static void drawDie(Graphics2D parent, double x, double y, double size, double angle,
                                int topFace, int frontFace, int rightFace) {
        Graphics2D g = (Graphics2D) parent.create();
        try {
            g.translate(x, y);
            g.rotate(angle);
            g.scale(size, size);

            // Cubo proporcionado.
            double left = -0.38;
            double right = 0.22;
            double topY = -0.24;
            double bottomY = 0.36;
            double depth = 0.16;

            Path2D front = polygon(new double[][]{
                    {left, topY}, {right, topY}, {right, bottomY}, {left, bottomY}
            });
            Path2D top = polygon(new double[][]{
                    {left, topY}, {right, topY}, {right + depth, topY - depth}, {left + depth, topY - depth}
            });
            Path2D rightSide = polygon(new double[][]{
                    {right, topY}, {right + depth, topY - depth}, {right + depth, bottomY - depth}, {right, bottomY}
            });

            g.setPaint(new GradientPaint(
                    0f, (float) topY, new Color(250, 238, 213),
                    (float) (right + depth), (float) (topY - depth), new Color(205, 184, 150)
            ));
            g.fill(top);

            g.setPaint(new GradientPaint(
                    0f, (float) topY, new Color(170, 140, 111),
                    0f, (float) bottomY, new Color(106, 78, 58)
            ));
            g.fill(front);

            g.setPaint(new GradientPaint(
                    (float) right, (float) topY, new Color(132, 101, 79),
                    (float) (right + depth), (float) bottomY, new Color(78, 56, 43)
            ));
            g.fill(rightSide);

            g.setColor(new Color(45, 28, 22));
            g.setStroke(new BasicStroke(0.022f));
            g.draw(top);
            g.draw(front);
            g.draw(rightSide);

            g.setColor(new Color(255, 247, 223, 105));
            g.setStroke(new BasicStroke(0.010f));
            g.draw(new Line2D.Double(left + 0.03, topY - 0.015, right - 0.03, topY - 0.015));
            g.draw(new Line2D.Double(
                    left + depth + 0.03, topY - depth + 0.02,
                    right + depth - 0.03, topY - depth + 0.02
            ));

            drawTopFacePips(g, topFace, left, right, topY, depth);
            drawFrontFacePips(g, frontFace, left, right, topY, bottomY);
            drawRightFacePips(g, rightFace, right, topY, bottomY, depth);

            Path2D crack = new Path2D.Double();
            crack.moveTo(left + 0.15, topY + 0.17);
            crack.lineTo(left + 0.19, topY + 0.25);
            crack.lineTo(left + 0.16, topY + 0.33);
            crack.lineTo(left + 0.21, topY + 0.40);
            g.setColor(new Color(58, 38, 30, 95));
            g.setStroke(new BasicStroke(0.012f));
            g.draw(crack);
        } finally {
            g.dispose();
        }
    }

    private static Path2D polygon(double[][] vertices) {
        Path2D path = new Path2D.Double();
        path.moveTo(vertices[0][0], vertices[0][1]);
        for (int i = 1; i < vertices.length; i++) {
            path.lineTo(vertices[i][0], vertices[i][1]);
        }
        path.closePath();
        return path;
    }

    //PUNTOS DE CADA CARA

    private static void drawTopFacePips(Graphics2D g, int face,
                                        double left, double right, double topY, double depth) {
        drawFaceByPattern(g, face, (u, v) -> drawFlatTopPip(g, u, v, left, right, topY, depth));
    }

    private static void drawFrontFacePips(Graphics2D g, int face,
                                          double left, double right, double topY, double bottomY) {
        drawFaceByPattern(g, face, (u, v) -> drawFlatFrontPip(g, u, v, left, right, topY, bottomY));
    }

    private static void drawRightFacePips(Graphics2D g, int face,
                                          double right, double topY, double bottomY, double depth) {
        drawFaceByPattern(g, face, (u, v) -> drawFlatRightPip(g, u, v, right, topY, bottomY, depth));
    }

    private interface PipDrawer {
        void draw(double u, double v);
    }

    private static void drawFaceByPattern(Graphics2D g, int face, PipDrawer drawer) {
        double[][] corners = {{0.21, 0.22}, {0.79, 0.22}, {0.21, 0.78}, {0.79, 0.78}};
        switch (face) {
            case 1 -> drawer.draw(0.50, 0.50);
            case 2 -> {
                drawer.draw(0.21, 0.22);
                drawer.draw(0.79, 0.78);
            }
            case 3 -> {
                drawer.draw(0.21, 0.22);
                drawer.draw(0.50, 0.50);
                drawer.draw(0.79, 0.78);
            }
            case 4 -> {
                for (double[] spot : corners) drawer.draw(spot[0], spot[1]);
            }
            case 5 -> {
                for (double[] spot : corners) drawer.draw(spot[0], spot[1]);
                drawer.draw(0.50, 0.50);
            }
            case 6 -> {
                for (double u : new double[]{0.21, 0.79}) {
                    for (double v : new double[]{0.20, 0.50, 0.80}) {
                        drawer.draw(u, v);
                    }
                }
            }
            default -> throw new IllegalArgumentException("Die face must be between 1 and 6");
        }
    }

    // PIPS PLANOS / INTEGRADOS

    private static void drawFlatTopPip(Graphics2D g, double u, double v,
                                       double left, double right, double topY, double depth) {
        double leftX = left + depth * v;
        double rightX = right + depth * v;
        double px = leftX * (1.0 - u) + rightX * u;
        double py = topY - depth * v;

        double rx = 0.042;
        double ry = 0.026;

        g.setColor(new Color(42, 27, 21, 215));
        g.fill(new Ellipse2D.Double(px - rx, py - ry, rx * 2.0, ry * 2.0));

        g.setColor(new Color(255, 240, 220, 25));
        g.setStroke(new BasicStroke(0.006f));
        g.draw(new Ellipse2D.Double(px - rx, py - ry, rx * 2.0, ry * 2.0));
    }

    private static void drawFlatFrontPip(Graphics2D g, double u, double v,
                                         double left, double right, double topY, double bottomY) {
        double px = left + (right - left) * u;
        double py = topY + (bottomY - topY) * v;

        double r = 0.028;

        g.setColor(new Color(54, 34, 26, 180));
        g.fill(new Ellipse2D.Double(px - r, py - r, r * 2.0, r * 2.0));

        g.setColor(new Color(255, 235, 210, 18));
        g.setStroke(new BasicStroke(0.005f));
        g.draw(new Ellipse2D.Double(px - r, py - r, r * 2.0, r * 2.0));
    }

    private static void drawFlatRightPip(Graphics2D g, double u, double v,
                                         double right, double topY, double bottomY, double depth) {
        double px = right + depth * u;
        double py = topY + (bottomY - topY) * v - depth * u;

        double rx = 0.020;
        double ry = 0.030;

        g.setColor(new Color(36, 22, 17, 170));
        g.fill(new Ellipse2D.Double(px - rx, py - ry, rx * 2.0, ry * 2.0));

        g.setColor(new Color(255, 235, 210, 16));
        g.setStroke(new BasicStroke(0.005f));
        g.draw(new Ellipse2D.Double(px - rx, py - ry, rx * 2.0, ry * 2.0));
    }
}