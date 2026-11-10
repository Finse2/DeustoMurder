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
        try {
            while (running) {
                try {
                    Thread.sleep(2000);
                    if (running) history.exportCSV(destination);
                } catch (InterruptedException interrupted) {
                    break;
                } catch (IOException error) {
                    System.err.println("Error saving history: " + error.getMessage());
                }
            }
        } finally {
            // Guardar también al cerrar; no perder los últimos eventos del turno.
            try {
                history.exportCSV(destination);
            } catch (IOException error) {
                System.err.println("Error saving final history: " + error.getMessage());
            }
        }
    }


    public void stopWriter() {
        running = false;
        interrupt();
    }
}
