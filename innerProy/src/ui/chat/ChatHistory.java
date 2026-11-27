package ui.chat;

import Networking.common.ChatChannel;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ChatHistory {

    public static final int MAX_MESSAGE_LENGTH = 500;

    public record Message(
            String sender,
            String text,
            LocalTime time,
            ChatChannel channel,
            String recipient
    ) {

        public Message(
                String sender,
                String text,
                LocalTime time
        ) {
            this(sender, text, time, ChatChannel.GENERAL, null);
        }
    }

    private final List<Message> messages = new ArrayList<>();

    private final List<Consumer<Message>> listeners =
            new CopyOnWriteArrayList<>();

    public synchronized List<Message> getMessages() {
        return List.copyOf(messages);
    }

    // GET MESSAGES BY CHANNEL

    public synchronized List<Message> getMessages(
            ChatChannel channel
    ) {

        List<Message> result = new ArrayList<>();

        for (Message message : messages) {

            if (message.channel() == channel) {
                result.add(message);
            }
        }

        return List.copyOf(result);
    }

    // GET PRIVATE CONVERSATION

    public synchronized List<Message> getPrivateMessages(
            String localPlayer,
            String otherPlayer
    ) {

        List<Message> result = new ArrayList<>();

        for (Message message : messages) {

            if (message.channel() != ChatChannel.PRIVATE) {
                continue;
            }

            boolean sent =
                    Objects.equals(message.sender(), localPlayer)
                            && Objects.equals(message.recipient(), otherPlayer);

            boolean received =
                    Objects.equals(message.sender(), otherPlayer)
                            && Objects.equals(message.recipient(), localPlayer);

            if (sent || received) {
                result.add(message);
            }
        }

        return List.copyOf(result);
    }

    // GENERAL MESSAGE

    public Message add(String sender, String text) {

        return add(
                sender,
                text,
                ChatChannel.GENERAL,
                null
        );
    }

    // CHANNEL MESSAGE

    public Message add(
            String sender,
            String text,
            ChatChannel channel,
            String recipient
    ) {

        String clean = normalize(text);

        if (sender == null || sender.isBlank()) {

            throw new IllegalArgumentException(
                    "El remitente no puede estar vacío"
            );
        }

        Objects.requireNonNull(channel);

        if (channel == ChatChannel.PRIVATE) {

            if (recipient == null || recipient.isBlank()) {

                throw new IllegalArgumentException(
                        "El destinatario no puede estar vacío"
                );
            }

            recipient = recipient.trim();

        } else {
            recipient = null;
        }

        Message message = new Message(
                sender.trim(),
                clean,
                LocalTime.now(),
                channel,
                recipient
        );

        synchronized (this) {
            messages.add(message);
        }

        for (Consumer<Message> listener : listeners) {
            listener.accept(message);
        }

        return message;
    }

    // LISTENERS

    public void addListener(Consumer<Message> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeListener(Consumer<Message> listener) {
        listeners.remove(listener);
    }

    // MESSAGE VALIDATION

    public static String normalize(String text) {

        if (text == null || text.isBlank()) {

            throw new IllegalArgumentException(
                    "El mensaje no puede estar vacío"
            );
        }

        String clean = text.strip();

        if (clean.length() > MAX_MESSAGE_LENGTH) {

            throw new IllegalArgumentException(
                    "Máximo " + MAX_MESSAGE_LENGTH
                            + " caracteres por mensaje"
            );
        }

        return clean;
    }
}