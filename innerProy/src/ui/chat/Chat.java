package ui.chat;

import Networking.Client;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/** Chat de una partida: jugadores, historial y conexión opcional con el servidor. */
public final class Chat {

    private final Set<String> participants = new LinkedHashSet<>();
    private final String localPlayer;
    private final ChatHistory history = new ChatHistory();

    private Client client;
    private BiConsumer<String, String> networkListener;

    public Chat(List<String> playerNames, String localPlayer) {
        Objects.requireNonNull(playerNames, "playerNames");
        if (localPlayer == null || localPlayer.isBlank()) {
            throw new IllegalArgumentException("El jugador local no puede estar vacío");
        }
        this.localPlayer = localPlayer.trim();
        for (String player : playerNames) {
            addParticipant(player);
        }
        addParticipant(this.localPlayer);
    }

    public synchronized void addParticipant(String player) {
        if (player == null || player.isBlank()) {
            throw new IllegalArgumentException("El participante no puede estar vacío");
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

    /** Envía por TCP si hay cliente asociado; en el mock funciona localmente. */
    public void send(String text) {
        String message = ChatHistory.normalize(text);
        Client current;
        synchronized (this) {
            current = client;
        }
        if (current != null) {
            current.sendChatMessage(message);
            if (!current.isConnected()) {
                throw new IllegalStateException("Se ha perdido la conexión con el servidor");
            }
        }
        history.add(localPlayer, message);
    }

    /** Vincula el chat a la conexión TCP existente de DeustoMurder. */
    public synchronized void bind(Client newClient) {
        if (client != null && networkListener != null) {
            client.removeChatListener(networkListener);
        }
        client = newClient;
        networkListener = null;
        if (newClient == null) return;

        networkListener = (sender, text) -> {
            try {
                addParticipant(sender);
                history.add(sender, text);
            } catch (IllegalArgumentException exception) {
                System.err.println("Mensaje de chat descartado: " + exception.getMessage());
            }
        };
        newClient.addChatListener(networkListener);
    }
}
