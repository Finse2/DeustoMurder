package game.history;

import java.util.LinkedList;
import java.util.Queue;

public class GameEventProcessorThread extends Thread {
    private final Queue<GameEvent> events;
    private boolean running = true;

    public GameEventProcessorThread() {
        events = new LinkedList<>();
        setName("GameEventProcessorThread");
    }

    // AÑADIR EVENTO

    public void addEvent (GameEvent event) {
        synchronized (events) {
            events.add(event);
            events.notifyAll();
        }
    }

    private void processEvent(GameEvent event) {

        System.out.println(
                "[EVENT PROCESSOR] " + event
        );
    }

    // DETENER HILO

    public void stopProcessor() {

        synchronized (events) {

            running = false;

            events.notifyAll();
        }
    }

    // PROCESAR EVENTOS

    @Override
    public void run() {
        while (true) {
            GameEvent event;
            synchronized (events) {
                while(events.isEmpty() && running) {
                    try {
                        events.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (!running && events.isEmpty()) {
                    return;
                }

                event = events.poll();
            }

            // Procesamos fuera del bloque synchronized

            processEvent(event);
        }
    }
}
