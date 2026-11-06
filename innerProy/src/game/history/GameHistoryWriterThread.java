package game.history;

import java.io.IOException;
import java.nio.file.Path;

public class GameHistoryWriterThread extends Thread {
    private final GameHistory history;
    private final Path destination;
    private volatile boolean running = true;

    public GameHistoryWriterThread(GameHistory history, Path destination) {
        this.history = history;
        this.destination = destination;

        setName("GameHistoryWriterThread");
    }

    @Override

    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(2000);

                if (!running) {
                    break;
                }

                synchronized (history) {
                    history.exportCSV(destination);
                }

                System.out.println("Game history saved succesfully");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                System.err.println("Error saving history: " + e.getMessage());
            }
        }
    }


    public void stopWriter() {
        running = false;
        interrupt();
    }
}
