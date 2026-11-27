package ui.chat;

import Networking.Client;
import Networking.common.ChatChannel;
import Networking.packets.ChatMessagePacket;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class Chat {

    private final Set<String> participants =
            new LinkedHashSet<>();

    private final String localPlayer;

    private final ChatHistory history =
            new ChatHistory();

    private Client client;

    private Consumer<ChatMessagePacket> networkListener;

    public Chat(
            List<String> playerNames,
            String localPlayer
    ) {

        Objects.requireNonNull(playerNames);

        if (localPlayer == null || localPlayer.isBlank()) {

            throw new IllegalArgumentException(
                    "El jugador local no puede estar vacío"
            );
        }

        this.localPlayer = localPlayer.trim();

        for (String player : playerNames) {
            addParticipant(player);
        }

        addParticipant(this.localPlayer);
    }

    // PARTICIPANTS

    public synchronized void addParticipant(String player) {

        if (player == null || player.isBlank()) {

            throw new IllegalArgumentException(
                    "El participante no puede estar vacío"
            );
        }

        participants.add(player.trim());
    }

    public synchronized List<String> getParticipants() {
        return List.copyOf(participants);
    }

    public String getLocalPlayer() {
        return localPlayer;
    }

    public ChatHistory getHistory() {
        return history;
    }

    // GENERAL MESSAGE

    public void send(String text) {

        String message = ChatHistory.normalize(text);

        Client current;

        synchronized (this) {
            current = client;
        }

        if (current != null) {

            current.sendChatMessage(message);

            if (!current.isConnected()) {

                throw new IllegalStateException(
                        "Se ha perdido la conexión con el servidor"
                );
            }
        }

        history.add(
                localPlayer,
                message,
                ChatChannel.GENERAL,
                null
        );
    }

    // PRIVATE MESSAGE

    public void sendPrivate(
            String recipient,
            String text
    ) {

        String message = ChatHistory.normalize(text);

        if (recipient == null || recipient.isBlank()) {

            throw new IllegalArgumentException(
                    "Selecciona un destinatario"
            );
        }

        recipient = recipient.trim();

        if (recipient.equals(localPlayer)) {

            throw new IllegalArgumentException(
                    "No puedes enviarte mensajes a ti mismo"
            );
        }

        Client current;

        synchronized (this) {

            if (!participants.contains(recipient)) {

                throw new IllegalArgumentException(
                        "El destinatario no pertenece a la partida"
                );
            }

            current = client;
        }

        if (current == null || !current.isConnected()) {

            throw new IllegalStateException(
                    "No hay conexión con el servidor"
            );
        }

        current.sendPrivateMessage(recipient, message);

        if (!current.isConnected()) {

            throw new IllegalStateException(
                    "Se ha perdido la conexión con el servidor"
            );
        }
    }

    // SYSTEM MESSAGE

    public void addSystemMessage(String message) {

        history.add(
                "System",
                message,
                ChatChannel.SYSTEM,
                null
        );
    }

    // NETWORK CONNECTION

    public synchronized void bind(Client newClient) {

        if (client != null && networkListener != null) {

            client.removeChatPacketListener(
                    networkListener
            );
        }

        client = newClient;
        networkListener = null;

        if (newClient == null) {
            return;
        }

        networkListener = this::receiveMessage;

        newClient.addChatPacketListener(
                networkListener
        );
    }

    // RECEIVE NETWORK MESSAGE

    private void receiveMessage(ChatMessagePacket packet) {

        try {

            ChatChannel channel = packet.getChannel();

            if (channel == null) {
                return;
            }

            switch (channel) {

                case GENERAL -> {

                    addParticipant(packet.getSenderName());

                    history.add(
                            packet.getSenderName(),
                            packet.getMessage()
                    );
                }

                case PRIVATE -> {

                    String sender = packet.getSenderName();
                    String recipient = packet.getRecipient();

                    boolean sent =
                            Objects.equals(sender, localPlayer);

                    boolean received =
                            Objects.equals(recipient, localPlayer);

                    if (!sent && !received) {
                        return;
                    }

                    addParticipant(sender);
                    addParticipant(recipient);

                    history.add(
                            sender,
                            packet.getMessage(),
                            ChatChannel.PRIVATE,
                            recipient
                    );
                }

                case SYSTEM -> {

                    addSystemMessage(
                            packet.getMessage()
                    );
                }
            }

        } catch (IllegalArgumentException exception) {

            System.err.println(
                    "Mensaje descartado: "
                            + exception.getMessage()
            );
        }
    }
}