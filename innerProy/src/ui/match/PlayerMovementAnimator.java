package ui.match;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PlayerMovementAnimator {

    private static final int FRAME_DELAY = 16;
    private static final int STEP_DURATION = 140;

    private final Timer timer;

    private List<Point> path = List.of();

    private int currentStep;
    private long stepStartTime;

    private boolean running;

    private Consumer<Point2D.Double> onPositionChanged;
    private Runnable onFinished;

    public PlayerMovementAnimator() {

        timer = new Timer(
                FRAME_DELAY,
                event -> updateAnimation()
        );
    }

    // START ANIMATION

    public void animate(
            List<Point> path,
            Consumer<Point2D.Double> onPositionChanged,
            Runnable onFinished
    ) {

        if (!SwingUtilities.isEventDispatchThread()) {

            SwingUtilities.invokeLater(() ->
                    animate(path, onPositionChanged, onFinished)
            );

            return;
        }

        stop();

        this.path = new ArrayList<>();

        for (Point point : path) {
            this.path.add(new Point(point));
        }

        this.onPositionChanged = onPositionChanged;
        this.onFinished = onFinished;

        currentStep = 0;

        if (this.path.size() < 2) {

            finish();

            return;
        }

        Point origin = this.path.getFirst();

        running = true;

        stepStartTime = System.nanoTime();

        onPositionChanged.accept(
                new Point2D.Double(origin.x, origin.y)
        );

        timer.start();
    }

    // UPDATE ANIMATION

    private void updateAnimation() {

        if (!running) return;

        if (currentStep >= path.size() - 1) {

            finish();

            return;
        }

        Point origin = path.get(currentStep);

        Point destination = path.get(currentStep + 1);

        double elapsed =
                (System.nanoTime() - stepStartTime)
                        / 1_000_000.0;

        double progress = Math.min(
                1.0,
                elapsed / STEP_DURATION
        );

        double smoothProgress = ease(progress);

        double x = origin.x
                + (destination.x - origin.x)
                * smoothProgress;

        double y = origin.y
                + (destination.y - origin.y)
                * smoothProgress;

        if (onPositionChanged != null) {

            onPositionChanged.accept(
                    new Point2D.Double(x, y)
            );
        }

        if (progress >= 1.0) {

            currentStep++;

            stepStartTime = System.nanoTime();

            if (currentStep >= path.size() - 1) {
                finish();
            }
        }
    }

    // SMOOTH STEP

    private double ease(double progress) {

        return progress * progress
                * (3.0 - 2.0 * progress);
    }

    // FINISH ANIMATION

    private void finish() {

        timer.stop();

        running = false;

        Runnable callback = onFinished;

        onFinished = null;
        onPositionChanged = null;

        if (callback != null) {
            callback.run();
        }
    }

    // CANCEL ANIMATION

    public void stop() {

        timer.stop();

        running = false;

        currentStep = 0;

        path = List.of();

        onFinished = null;
        onPositionChanged = null;
    }

    public boolean isRunning() {

        return running;
    }
}