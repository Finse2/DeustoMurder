package ui.match.inGameScreen;

import game.dice.Dice;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Dos dados rigidos animados encima del tablero.
 * Cada dado conserva sus seis caras durante toda la animacion:
 * solo cambia la orientacion de un cubo tridimensional.
 *
 * clic en "Tirar dados" -> aviso -> mantener raton (maximo 5 s)
 * -> soltar / limite de tiempo -> rebotes -> resultado y retirar la capa.
 * Si no se pulsa el raton en 5 s, la tirada se resuelve al instante.
 */
public final class DiceRollPanel extends JPanel {

    private enum Phase { WAITING, HOLDING, FALLING, SETTLED }

    private static final int FRAME_MS = 15;
    private static final int MAX_HOLD_MS = 5000;
    private static final int MAX_WAIT_MS = 5000;
    private static final Color CREAM = new Color(240, 220, 180);
    private static final Color GOLD = new Color(205, 165, 105);
    private static final Vec3 WORLD_UP = new Vec3(0, 0, 1);
    private static final Vec3 CAMERA = new Vec3(6, -8, 11).unit();
    private static final Vec3 SCREEN_RIGHT = new Vec3(8, 6, 0).unit();
    private static final Vec3 SCREEN_UP = CAMERA.cross(SCREEN_RIGHT).unit();
    private static final Vec3 LIGHT = new Vec3(-4, -5, 10).unit();

    // normas fijas para un dado real: las caras opuestas suman siete.
    private static final Face[] FACES = {
            new Face(1, new Vec3(0, 0, 1), new Vec3(1, 0, 0), new Vec3(0, 1, 0)),
            new Face(6, new Vec3(0, 0, -1), new Vec3(1, 0, 0), new Vec3(0, -1, 0)),
            new Face(3, new Vec3(1, 0, 0), new Vec3(0, 1, 0), new Vec3(0, 0, 1)),
            new Face(4, new Vec3(-1, 0, 0), new Vec3(0, -1, 0), new Vec3(0, 0, 1)),
            new Face(2, new Vec3(0, 1, 0), new Vec3(-1, 0, 0), new Vec3(0, 0, 1)),
            new Face(5, new Vec3(0, -1, 0), new Vec3(1, 0, 0), new Vec3(0, 0, 1))
    };

    private final Dice dice;
    private final Random random;
    private final Timer timer;
    private final AnimatedDie[] animated = {new AnimatedDie(), new AnimatedDie()};

    private Phase phase = Phase.WAITING;
    private long phaseStart = System.nanoTime();
    private BiConsumer<Integer, Integer> onRollComplete;
    private int firstResult;
    private int secondResult;

    public DiceRollPanel() {
        this(new Dice(), new Random());
    }

    public DiceRollPanel(Dice dice, Random random) {
        this.dice = Objects.requireNonNull(dice, "dice");
        this.random = Objects.requireNonNull(random, "random");
        this.timer = new Timer(FRAME_MS, event -> tick());

        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        for (int i = 0; i < animated.length; i++) resetDie(i);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!SwingUtilities.isLeftMouseButton(event)) return;
                if (phase == Phase.WAITING) beginHold();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) releaseAndRoll();
            }
        });

    }

    /** Prepara dos dados en el centro; aun no decide su resultado. */
    public void prepareRoll() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::prepareRoll);
            return;
        }
        if (phase == Phase.FALLING || phase == Phase.HOLDING) return;

        timer.stop();
        phase = Phase.WAITING;
        firstResult = 0;
        secondResult = 0;
        phaseStart = System.nanoTime();
        for (int i = 0; i < animated.length; i++) resetDie(i);
        setVisible(true);
        timer.start(); // el plazo de espera comienza incluso sin interactuar.
        requestFocusInWindow();
        repaint();
    }

    /** Inicia la agitacion al presionar con el boton izquierdo. */
    public void beginHold() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::beginHold);
            return;
        }
        if (phase != Phase.WAITING) return;
        phase = Phase.HOLDING;
        phaseStart = System.nanoTime();
        timer.start();
        repaint();
    }

    /** Soltar el raton o agotar el plazo produce exactamente una tirada. */
    public void releaseAndRoll() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::releaseAndRoll);
            return;
        }
        if (phase != Phase.HOLDING) return;

        double heldSeconds = Math.min(MAX_HOLD_MS / 1000.0, elapsedMs() / 1000.0);
        for (int i = 0; i < animated.length; i++) updateHoldingDie(animated[i], heldSeconds);

        dice.roll();
        int[] outcomes = {dice.getFirstDie(), dice.getSecondDie()};
        for (int i = 0; i < animated.length; i++) {
            AnimatedDie d = animated[i];
            d.result = outcomes[i];
            d.releaseRotation = d.orientation;
            d.axis = new Vec3(randomRange(-1, 1), randomRange(-1, 1), randomRange(0.3, 1)).unit();
            d.turns = 2.0 + random.nextInt(3);
            d.durationMs = 1950 + i * 250;
            d.endX = (i == 0 ? -2.1 : 2.1) + randomRange(-0.30, 0.30);
            d.endY = randomRange(-0.25, 0.35);
            Face topFace = faceOf(outcomes[i]);
            Quaternion align = Quaternion.fromTo(topFace.normal, WORLD_UP);
            Quaternion yaw = Quaternion.fromAxisAngle(WORLD_UP, randomRange(-Math.PI, Math.PI));
            d.targetRotation = yaw.multiply(align).unit();
        }
        phase = Phase.FALLING;
        phaseStart = System.nanoTime();
        timer.start();
        repaint();
    }

    public void setOnRollComplete(BiConsumer<Integer, Integer> listener) {
        onRollComplete = listener;
    }

    public boolean isWaiting() { return phase == Phase.WAITING; }
    public boolean isHolding() { return phase == Phase.HOLDING; }
    public boolean isRolling() { return phase == Phase.FALLING; }
    public boolean isSettled() { return phase == Phase.SETTLED; }
    public int getFirstDie() { return isSettled() ? firstResult : 0; }
    public int getSecondDie() { return isSettled() ? secondResult : 0; }
    public int getTotal() { return getFirstDie() + getSecondDie(); }

    /** Cara que apunta hacia arriba fisicamente; util para verificar la orientacion. */
    public int getTopFace(int index) {
        if (index < 0 || index >= animated.length) throw new IndexOutOfBoundsException(index);
        AnimatedDie d = animated[index];
        int best = 0;
        double highest = -Double.MAX_VALUE;
        for (Face f : FACES) {
            double height = d.orientation.rotate(f.normal).dot(WORLD_UP);
            if (height > highest) { highest = height; best = f.value; }
        }
        return best;
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    private void resetDie(int index) {
        AnimatedDie d = animated[index];
        d.startX = index == 0 ? -1.0 : 1.0;
        d.endX = d.startX;
        d.endY = 0;
        d.result = 0;
        d.seed = randomRange(-Math.PI, Math.PI);
        d.initialRotation = Quaternion.fromAxisAngle(new Vec3(1, 1, 1).unit(), d.seed + 1.0);
        d.orientation = d.initialRotation;
        d.height = 1.15;
        d.x = d.startX;
        d.y = 0;
    }

    private void tick() {
        if (phase == Phase.WAITING) {
            if (elapsedMs() >= MAX_WAIT_MS) {
                beginHold();
                releaseAndRoll();
                return;
            }
        } else if (phase == Phase.HOLDING) {
            double t = elapsedMs() / 1000.0;
            for (AnimatedDie d : animated) updateHoldingDie(d, t);
            if (elapsedMs() >= MAX_HOLD_MS) releaseAndRoll(); // soltado automatico a los 5 s.
        } else if (phase == Phase.FALLING) {
            double ms = elapsedMs();
            boolean bothStopped = true;
            for (AnimatedDie d : animated) {
                updateFallingDie(d, ms);
                if (ms < d.durationMs) bothStopped = false;
            }
            if (bothStopped) {
                completeRoll(dice.getFirstDie(), dice.getSecondDie());
                return;
            }
        } else {
            timer.stop();
            return;
        }
        repaint();
    }

    /** Transcurridos 5 s sin pulsar, sortea las caras y termina inmediatamente. */
    private void forceInstantRoll() {
        if (phase != Phase.WAITING) return;
        dice.roll();
        int[] results = {dice.getFirstDie(), dice.getSecondDie()};
        for (int i = 0; i < animated.length; i++) {
            AnimatedDie d = animated[i];
            d.result = results[i];
            Quaternion upright = Quaternion.fromTo(faceOf(results[i]).normal, WORLD_UP);
            Quaternion yaw = Quaternion.fromAxisAngle(WORLD_UP, randomRange(-Math.PI, Math.PI));
            d.orientation = yaw.multiply(upright).unit();
            d.height = 0;
        }
        completeRoll(results[0], results[1]);
    }

    /** Publica el resultado una sola vez y libera el tablero de forma inmediata. */
    private void completeRoll(int first, int second) {
        if (phase == Phase.SETTLED) return;
        firstResult = first;
        secondResult = second;
        phase = Phase.SETTLED;
        timer.stop();
        setVisible(false); // quita dados, mensaje, oscurecimiento y bloqueo del raton.
        if (onRollComplete != null) onRollComplete.accept(firstResult, secondResult);
    }

    private void updateHoldingDie(AnimatedDie d, double seconds) {
        // giro rigido: la matriz de orientacion rota todas las caras a la vez.
        Quaternion pitch = Quaternion.fromAxisAngle(new Vec3(1, 0, 0), seconds * 12 + d.seed);
        Quaternion roll = Quaternion.fromAxisAngle(new Vec3(0, 1, 0), seconds * 15 - d.seed);
        Quaternion yaw = Quaternion.fromAxisAngle(WORLD_UP, seconds * 9);
        d.orientation = yaw.multiply(pitch).multiply(roll).multiply(d.initialRotation).unit();
        d.x = d.startX + Math.sin(seconds * 26 + d.seed) * 0.22;
        d.y = Math.cos(seconds * 20 - d.seed) * 0.25;
        d.height = 1.05 + Math.abs(Math.sin(seconds * 19 + d.seed)) * 0.35;
    }

    private void updateFallingDie(AnimatedDie d, double elapsed) {
        double p = clamp(elapsed / d.durationMs);
        double slide = 1.0 - Math.pow(1.0 - p, 2.0); // desplazamiento amortiguado.
        d.x = d.startX + (d.endX - d.startX) * slide;
        d.y = d.endY * slide;

        // caida inicial seguida de tres rebotes con altura decreciente.
        if (p < 0.35) {
            double u = p / 0.35;
            d.height = 1.35 * (1.0 - u) * (1.0 - u);
        } else if (p < 0.60) {
            d.height = bounce(p, 0.35, 0.60, 0.70);
        } else if (p < 0.80) {
            d.height = bounce(p, 0.60, 0.80, 0.25);
        } else if (p < 0.95) {
            d.height = bounce(p, 0.80, 0.95, 0.07);
        } else {
            d.height = 0;
        }

        double spinProgress = 1.0 - Math.pow(1.0 - Math.min(1, p * 1.35), 3.0);
        Quaternion spin = Quaternion.fromAxisAngle(d.axis, 2.0 * Math.PI * d.turns * spinProgress);
        Quaternion spinning = spin.multiply(d.releaseRotation).unit();
        double alignProgress = smoothstep(clamp((p - 0.55) / 0.40));
        // interpolacion esferica: el cubo termina apoyado en la cara del resultado.
        d.orientation = Quaternion.slerp(spinning, d.targetRotation, alignProgress);
    }

    private double elapsedMs() {
        return (System.nanoTime() - phaseStart) / 1_000_000.0;
    }

    private double randomRange(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static double bounce(double t, double from, double to, double height) {
        return height * Math.sin(Math.PI * clamp((t - from) / (to - from)));
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static double smoothstep(double p) {
        return p * p * (3 - 2 * p);
    }

    private static Face faceOf(int value) {
        for (Face f : FACES) if (f.value == value) return f;
        throw new IllegalArgumentException("Die value out of range: " + value);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(new Color(10, 7, 5, 105));
            g.fillRect(0, 0, getWidth(), getHeight());
            paintDice(g);
            paintMessage(g);
        } finally {
            g.dispose();
        }
    }

    private void paintDice(Graphics2D g) {
        double dieSize = Math.max(30, Math.min(65, Math.min(getWidth() / 11.0, getHeight() / 8.0)));
        for (AnimatedDie d : animated) {
            double x = getWidth() * 0.5 + dieSize * d.x;
            double groundY = getHeight() * 0.62 + d.y * dieSize;
            paintShadow(g, x, groundY, dieSize, d.height);
            double centerY = groundY - dieSize * (0.30 + d.height);
            paintCube(g, d.orientation, x, centerY, dieSize);
        }
    }

    private void paintShadow(Graphics2D g, double x, double y, double size, double height) {
        int opacity = (int) (110 / (1.0 + height * 0.8));
        g.setColor(new Color(0, 0, 0, opacity));
        double w = size * (1.5 + height * 0.2);
        g.fill(new Ellipse2D.Double(x - w / 2, y - size * 0.16, w, size * 0.32));
    }

    private void paintCube(Graphics2D g, Quaternion rotation, double x, double y, double size) {
        for (Face face : FACES) {
            Vec3 normal = rotation.rotate(face.normal);
            if (normal.dot(CAMERA) <= 0.015) continue; // solo las caras visibles.

            Path2D polygon = new Path2D.Double();
            double[][] corners = {{-0.5, -0.5}, {0.5, -0.5}, {0.5, 0.5}, {-0.5, 0.5}};
            for (int i = 0; i < corners.length; i++) {
                Vec3 point = facePoint(face, corners[i][0], corners[i][1], 0.5);
                double[] xy = project(rotation.rotate(point), x, y, size);
                if (i == 0) polygon.moveTo(xy[0], xy[1]);
                else polygon.lineTo(xy[0], xy[1]);
            }
            polygon.closePath();

            double brightness = Math.max(0.50, Math.min(1.0, 0.69 + normal.dot(LIGHT) * 0.28));
            g.setColor(new Color((int) (236 * brightness), (int) (218 * brightness), (int) (188 * brightness)));
            g.fill(polygon);
            g.setColor(new Color(75, 45, 27));
            g.setStroke(new BasicStroke(Math.max(1.5f, (float) (size * 0.025))));
            g.draw(polygon);

            drawPips(g, face, rotation, x, y, size);
        }
    }

    private void drawPips(Graphics2D g, Face face, Quaternion rotation, double x, double y, double size) {
        for (double[] uv : pipCoordinates(face.value)) {
            Path2D pip = new Path2D.Double();
            int steps = 16;
            for (int i = 0; i <= steps; i++) {
                double angle = Math.PI * 2 * i / steps;
                double u = uv[0] + Math.cos(angle) * 0.072;
                double v = uv[1] + Math.sin(angle) * 0.072;
                Vec3 point = facePoint(face, u, v, 0.504);
                double[] xy = project(rotation.rotate(point), x, y, size);
                if (i == 0) pip.moveTo(xy[0], xy[1]);
                else pip.lineTo(xy[0], xy[1]);
            }
            pip.closePath();
            g.setColor(new Color(56, 35, 28));
            g.fill(pip);
        }
    }

    private static double[][] pipCoordinates(int n) {
        double a = 0.27;
        return switch (n) {
            case 1 -> new double[][]{{0, 0}};
            case 2 -> new double[][]{{-a, -a}, {a, a}};
            case 3 -> new double[][]{{-a, -a}, {0, 0}, {a, a}};
            case 4 -> new double[][]{{-a, -a}, {-a, a}, {a, -a}, {a, a}};
            case 5 -> new double[][]{{-a, -a}, {-a, a}, {0, 0}, {a, -a}, {a, a}};
            case 6 -> new double[][]{{-a, -a}, {-a, 0}, {-a, a}, {a, -a}, {a, 0}, {a, a}};
            default -> throw new IllegalArgumentException("Invalid face: " + n);
        };
    }

    private static Vec3 facePoint(Face face, double u, double v, double offset) {
        return face.normal.times(offset).plus(face.u.times(u)).plus(face.v.times(v));
    }

    private static double[] project(Vec3 point, double centerX, double centerY, double size) {
        return new double[]{centerX + size * point.dot(SCREEN_RIGHT), centerY - size * point.dot(SCREEN_UP)};
    }

    private void paintMessage(Graphics2D g) {
        // El resultado final se muestra en GameActionsPanel debajo de "Tirar dados".
        if (phase == Phase.SETTLED) return;

        int width = Math.min(380, Math.max(180, getWidth() - 30));
        int height = 80;
        int x = (getWidth() - width) / 2;
        int y = 20;
        g.setColor(new Color(29, 19, 13, 235));
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setStroke(new BasicStroke(2));
        g.setColor(GOLD);
        g.drawRoundRect(x, y, width, height, 10, 10);

        String heading;
        String subheading;
        if (phase == Phase.WAITING) {
            heading = "ROLL THE DICE";
            subheading = String.format(Locale.ROOT, "Pulsa antes de %.1f s o se lanzaran solos",
                    Math.max(0, (MAX_WAIT_MS - elapsedMs()) / 1000.0));
        } else if (phase == Phase.HOLDING) {
            double remaining = Math.max(0, (MAX_HOLD_MS - elapsedMs()) / 1000.0);
            heading = String.format(Locale.ROOT, "%.1f s", remaining);
            subheading = "Suelta para lanzarlos";
        } else if (phase == Phase.FALLING) {
            heading = "LANZANDO...";
            subheading = "Los dados estan rebotando";
        } else {
            heading = "LANZANDO...";
            subheading = "Los dados estan rebotando";
        }

        int titleSize = Math.max(16, Math.min(24, width / 15));
        g.setFont(new Font("Georgia", Font.BOLD, titleSize));
        g.setColor(CREAM);
        centerText(g, heading, getWidth() / 2, y + 36);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.setColor(new Color(205, 180, 143));
        centerText(g, subheading, getWidth() / 2, y + 62);
    }

    private void centerText(Graphics2D g, String s, int centerX, int baseline) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(s, centerX - metrics.stringWidth(s) / 2, baseline);
    }

    private static final class AnimatedDie {
        Quaternion initialRotation = Quaternion.IDENTITY;
        Quaternion orientation = Quaternion.IDENTITY;
        Quaternion releaseRotation = Quaternion.IDENTITY;
        Quaternion targetRotation = Quaternion.IDENTITY;
        Vec3 axis = WORLD_UP;
        double x;
        double y;
        double height;
        double startX;
        double endX;
        double endY;
        double seed;
        double turns;
        int result;
        int durationMs;
    }

    private record Face(int value, Vec3 normal, Vec3 u, Vec3 v) { }

    private record Vec3(double x, double y, double z) {
        Vec3 plus(Vec3 other) { return new Vec3(x + other.x, y + other.y, z + other.z); }
        Vec3 times(double n) { return new Vec3(x * n, y * n, z * n); }
        double dot(Vec3 other) { return x * other.x + y * other.y + z * other.z; }
        Vec3 cross(Vec3 o) { return new Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x); }
        Vec3 unit() {
            double length = Math.sqrt(dot(this));
            return length < 0.000001 ? WORLD_UP : times(1.0 / length);
        }
    }

    /** Cuaternion unitario para mantener inmutables las caras del cubo. */
    private record Quaternion(double w, double x, double y, double z) {
        static final Quaternion IDENTITY = new Quaternion(1, 0, 0, 0);

        static Quaternion fromAxisAngle(Vec3 axis, double angle) {
            Vec3 n = axis.unit();
            double sin = Math.sin(angle * 0.5);
            return new Quaternion(Math.cos(angle * 0.5), n.x * sin, n.y * sin, n.z * sin);
        }

        static Quaternion fromTo(Vec3 from, Vec3 to) {
            Vec3 a = from.unit();
            Vec3 b = to.unit();
            double cosine = a.dot(b);
            if (cosine > 0.999999) return IDENTITY;
            if (cosine < -0.999999) {
                Vec3 candidate = Math.abs(a.x) < 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
                return fromAxisAngle(a.cross(candidate), Math.PI);
            }
            Vec3 cross = a.cross(b);
            return new Quaternion(1 + cosine, cross.x, cross.y, cross.z).unit();
        }

        Quaternion multiply(Quaternion b) {
            return new Quaternion(
                    w * b.w - x * b.x - y * b.y - z * b.z,
                    w * b.x + x * b.w + y * b.z - z * b.y,
                    w * b.y - x * b.z + y * b.w + z * b.x,
                    w * b.z + x * b.y - y * b.x + z * b.w
            );
        }

        Quaternion unit() {
            double length = Math.sqrt(w * w + x * x + y * y + z * z);
            return new Quaternion(w / length, x / length, y / length, z / length);
        }

        Vec3 rotate(Vec3 v) {
            Vec3 axis = new Vec3(x, y, z);
            Vec3 twice = axis.cross(v).times(2);
            return v.plus(twice.times(w)).plus(axis.cross(twice));
        }

        static Quaternion slerp(Quaternion from, Quaternion to, double amount) {
            double t = clamp(amount);
            double dot = from.w * to.w + from.x * to.x + from.y * to.y + from.z * to.z;
            if (dot < 0) {
                dot = -dot;
                to = new Quaternion(-to.w, -to.x, -to.y, -to.z);
            }
            if (dot > 0.9995) {
                return new Quaternion(
                        from.w + (to.w - from.w) * t,
                        from.x + (to.x - from.x) * t,
                        from.y + (to.y - from.y) * t,
                        from.z + (to.z - from.z) * t
                ).unit();
            }
            double angle = Math.acos(Math.min(1, dot));
            double sin = Math.sin(angle);
            double a = Math.sin((1 - t) * angle) / sin;
            double b = Math.sin(t * angle) / sin;
            return new Quaternion(from.w * a + to.w * b, from.x * a + to.x * b,
                    from.y * a + to.y * b, from.z * a + to.z * b).unit();
        }
    }
}
