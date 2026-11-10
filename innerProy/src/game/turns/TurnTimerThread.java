package game.turns;

import game.core.Game;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Cuenta atrás de un único turno. Cada turno debe crear su propio temporizador.
 * Espera sin consumir CPU, admite pausa/reanudación y solo avisa una vez al expirar.
 */
public class TurnTimerThread extends Thread {

    private static final long SECOND_NANOS = TimeUnit.SECONDS.toNanos(1);

    private final Consumer<Integer> onTick;
    private final Runnable onExpired;
    private int remainingSeconds;
    private boolean paused;
    private boolean stopped;
    private long nextTickNanos;

    /** Compatibilidad con las llamadas anteriores. */
    public TurnTimerThread(Game game, int seconds) {
        this(seconds, remaining -> {}, Objects.requireNonNull(game)::nextTurn);
    }

    public TurnTimerThread(int seconds, Consumer<Integer> onTick, Runnable onExpired) {
        if (seconds <= 0) throw new IllegalArgumentException("Seconds must be positive");
        this.remainingSeconds = seconds;
        this.onTick = Objects.requireNonNull(onTick);
        this.onExpired = Objects.requireNonNull(onExpired);
        setName("TurnTimerThread");
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                if (stopped) return;
                nextTickNanos = System.nanoTime() + SECOND_NANOS;
            }
            notifyTick(getRemainingSeconds());

            while (true) {
                int remaining;
                boolean expired;

                synchronized (this) {
                    while (paused && !stopped) wait();
                    if (stopped) return;

                    long timeLeft = nextTickNanos - System.nanoTime();
                    if (timeLeft > 0) {
                        TimeUnit.NANOSECONDS.timedWait(this, timeLeft);
                        continue;
                    }

                    long passedTicks = 1 + (-timeLeft / SECOND_NANOS);
                    remainingSeconds -= (int) Math.min(passedTicks, remainingSeconds);
                    nextTickNanos += passedTicks * SECOND_NANOS;
                    remaining = remainingSeconds;
                    expired = remaining == 0;
                    if (expired) stopped = true;
                }

                notifyTick(remaining);
                if (expired) {
                    onExpired.run();
                    return;
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void notifyTick(int seconds) {
        onTick.accept(seconds);

    }

    public synchronized void pauseTimer() {
        if (stopped || paused) return;
        paused = true;
        notifyAll();
    }

    public synchronized void resumeTimer() {
        if (stopped || !paused) return;
        paused = false;
        nextTickNanos = System.nanoTime() + SECOND_NANOS;
        notifyAll();
    }

    public synchronized void stopTimer() {
        if (stopped) return;
        stopped = true;
        notifyAll();
        interrupt();
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public synchronized int getRemainingSeconds() {
        return remainingSeconds;
    }
}
