package ui.chat;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Guarda los mensajes y avisa a los renderers cuando llega uno nuevo. */
public final class ChatHistory {

    public static final int MAX_MESSAGE_LENGTH = 500;

    public record Message(String sender, String text, LocalTime time) {}

    private final List<Message> messages = new ArrayList<>();
    private final List<Consumer<Message>> listeners = new CopyOnWriteArrayList<>();

    public synchronized List<Message> getMessages() {
        return List.copyOf(messages);
    }

    public Message add(String sender, String text) {
        String clean = normalize(text);
        if (sender == null || sender.isBlank()) {
            throw new IllegalArgumentException("El remitente no puede estar vacío");
        }

        Message message = new Message(sender.trim(), clean, LocalTime.now());
        synchronized (this) {
            messages.add(message);
        }
        for (Consumer<Message> listener : listeners) {
            listener.accept(message);
        }
        return message;
    }

    public void addListener(Consumer<Message> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeListener(Consumer<Message> listener) {
        listeners.remove(listener);
    }

    public static String normalize(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        String clean = text.strip();
        if (clean.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Máximo " + MAX_MESSAGE_LENGTH + " caracteres por mensaje");
        }
        return clean;
    }
}
