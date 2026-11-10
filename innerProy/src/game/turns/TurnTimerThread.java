package game.turns;

import game.core.Game;
import org.w3c.dom.ls.LSOutput;

public class TurnTimerThread extends Thread {

    private final Game game;
    private int remainingSeconds;
    private boolean paused;
    private boolean stopped;

    public TurnTimerThread(Game game, int seconds) {
        this.game = game;
        this.remainingSeconds = seconds;

        setName("TurnTimerThread");
    }

    @Override
    public void run () {
        while (remainingSeconds > 0) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (stopped) {
                return;
            }
        }
        System.out.println("Time remaining" + remainingSeconds);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        synchronized (this) {
            if (!paused && !stopped) {
                remainingSeconds--;
            }
        }

        synchronized (this) {

            if (stopped) {
                return;
            }

            stopped = true;
        }

        System.out.println("Time expired!");

        game.nextTurn();
    }

    public synchronized void resumeTimer() {

        paused = false;

        notifyAll();
    }

    public synchronized void stopTimer() {

        stopped = true;

        notifyAll();

        interrupt();
    }

    public synchronized int getRemainingSeconds() {

        return remainingSeconds;

    }
}
