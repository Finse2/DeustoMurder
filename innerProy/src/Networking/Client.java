
package Networking;

import Networking.client.GameClient;
import Networking.common.Packet;
import Networking.common.ChatChannel;

import Networking.packets.CardShowPacket;
import Networking.packets.ChatMessagePacket;
import Networking.packets.GameOverPacket;
import Networking.packets.GameStartPacket;
import Networking.packets.JoinAcceptedPacket;
import Networking.packets.PlayerJoinedPacket;
import Networking.packets.PlayerMovePacket;

import app.ApplicationContext;

import java.io.IOException;
import java.io.Serializable;

import java.util.UUID;
import java.util.List;
import java.util.Objects;

import java.util.concurrent.CopyOnWriteArrayList;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Client {

    // LISTENER INTERFACE

    public interface Listener {

        default void onConnected(int playerID) {}

        default void onGameOverPacket(
                GameOverPacket packet
        ) {}

        // Legacy player joined callback

        default void onPlayerJoined(
                int playerID,
                String playerName,
                String computerName
        ) {}

        // Player joined callback with persistent UUID

        default void onPlayerJoined(
                int playerID,
                String playerName,
                String computerName,
                UUID profileId
        ) {

            onPlayerJoined(
                    playerID,
                    playerName,
                    computerName
            );
        }

        default void onPlayerMoved(
                int playerID,
                int x,
                int y,
                int distance
        ) {}

        default void onCardShown(
                Serializable card,
                String fromUser,
                String targetUser,
                int targetPlayerID
        ) {}

        default void onGameStarted() {}

        default void onGameOver(String winner) {}

        default void onChatMessage(
                int playerID,
                String senderName,
                String text
        ) {}

        default void onDisconnected() {}

        default void onError(Exception exception) {

            exception.printStackTrace();
        }
    }

    // FIELDS

    private final GameClient client;

    private final Listener listener;

    private final List<BiConsumer<String, String>>
            chatListeners = new CopyOnWriteArrayList<>();

    private final List<Consumer<ChatMessagePacket>>
            chatPacketListeners = new CopyOnWriteArrayList<>();

    // CONSTRUCTORS

    public Client(String host, int port) {

        this(host, port, null);
    }

    public Client(
            String host,
            int port,
            Listener listener
    ) {

        this.listener = listener;

        this.client = new GameClient(
                host,
                port,
                new GameClient.PacketListener() {

                    @Override
                    public void onPacket(Packet packet) {

                        dispatch(packet);
                    }

                    @Override
                    public void onDisconnected() {

                        if (Client.this.listener != null) {

                            Client.this.listener
                                    .onDisconnected();
                        }
                    }

                    @Override
                    public void onError(Exception exception) {

                        if (Client.this.listener != null) {

                            Client.this.listener
                                    .onError(exception);
                        }
                    }
                }
        );
    }

    // CONNECT USING PERSISTENT LOCAL PROFILE

    public void connect(
            String userName,
            String computerName
    ) throws IOException {

        ApplicationContext context =
                ApplicationContext.getInstance();

        if (!context.isInitialized()) {

            throw new IllegalStateException(
                    "Application context is not initialized."
            );
        }

        connect(
                userName,
                computerName,
                context.getLocalPlayerId()
        );
    }

    // CONNECT USING EXPLICIT PROFILE UUID

    public void connect(
            String userName,
            String computerName,
            UUID profileId
    ) throws IOException {

        client.connect(
                userName,
                computerName,
                Objects.requireNonNull(profileId)
        );
    }

    // GAME ACTIONS

    public void startGame() {

        client.sendPacket(
                new GameStartPacket(true)
        );
    }

    public void movePlayer(
            int x,
            int y,
            int distance
    ) {

        client.sendPacket(
                new PlayerMovePacket(
                        x,
                        y,
                        distance,
                        client.getPlayerID()
                )
        );
    }

    public void showCard(
            Serializable card,
            int targetPlayerID
    ) {

        client.sendPacket(
                new CardShowPacket(
                        card,
                        null,
                        null,
                        targetPlayerID
                )
        );
    }

    public void endGame(String winner) {

        client.sendPacket(
                new GameOverPacket(
                        true,
                        winner
                )
        );
    }

    // GENERAL CHAT

    public void sendChatMessage(String message) {

        if (message == null
                || message.isBlank()
                || message.strip().length() > 500) {

            throw new IllegalArgumentException(
                    "Message must contain 1 to 500 characters"
            );
        }

        client.sendPacket(
                new ChatMessagePacket(
                        -1,
                        null,
                        message.strip()
                )
        );
    }

    // PRIVATE CHAT

    public void sendPrivateMessage(
            String recipient,
            String message
    ) {

        if (recipient == null || recipient.isBlank()) {

            throw new IllegalArgumentException(
                    "Recipient cannot be empty"
            );
        }

        if (message == null
                || message.isBlank()
                || message.strip().length() > 500) {

            throw new IllegalArgumentException(
                    "Message must contain 1 to 500 characters"
            );
        }

        client.sendPacket(
                new ChatMessagePacket(
                        -1,
                        null,
                        message.strip(),
                        ChatChannel.PRIVATE,
                        recipient.trim()
                )
        );
    }

    // COMPLETE CHAT PACKET LISTENERS

    public void addChatPacketListener(
            Consumer<ChatMessagePacket> listener
    ) {

        chatPacketListeners.add(
                Objects.requireNonNull(listener)
        );
    }

    public void removeChatPacketListener(
            Consumer<ChatMessagePacket> listener
    ) {

        chatPacketListeners.remove(listener);
    }

    // GENERAL CHAT LISTENERS

    public void addChatListener(
            BiConsumer<String, String> listener
    ) {

        chatListeners.add(
                Objects.requireNonNull(listener)
        );
    }

    public void removeChatListener(
            BiConsumer<String, String> listener
    ) {

        chatListeners.remove(listener);
    }

    // CONNECTION MANAGEMENT

    public void disconnect() {

        client.disconnect();
    }

    public boolean isConnected() {

        return client.isConnected();
    }

    // TEMPORARY SESSION ID

    public int getPlayerID() {

        return client.getPlayerID();
    }

    // PERMANENT PROFILE UUID

    public UUID getProfileId() {

        return client.getProfileId();
    }

    // PACKET DISPATCH

    private void dispatch(Packet packet) {

        // CHAT MESSAGE PACKET

        if (packet instanceof ChatMessagePacket chatPacket) {

            // Complete packet listeners

            for (Consumer<ChatMessagePacket> chatListener :
                    chatPacketListeners) {

                chatListener.accept(chatPacket);
            }

            // Legacy general chat listeners

            if (chatPacket.getChannel()
                    == ChatChannel.GENERAL) {

                if (listener != null) {

                    listener.onChatMessage(
                            chatPacket.getSenderID(),
                            chatPacket.getSenderName(),
                            chatPacket.getMessage()
                    );
                }

                for (BiConsumer<String, String> chatListener :
                        chatListeners) {

                    chatListener.accept(
                            chatPacket.getSenderName(),
                            chatPacket.getMessage()
                    );
                }
            }

            return;
        }

        if (listener == null) {

            return;
        }

        // JOIN ACCEPTED

        if (packet instanceof JoinAcceptedPacket acceptedPacket) {

            listener.onConnected(
                    acceptedPacket.getPlayerID()
            );
        }

        // PLAYER JOINED

        else if (packet instanceof PlayerJoinedPacket joinedPacket) {

            listener.onPlayerJoined(
                    joinedPacket.getPlayerID(),
                    joinedPacket.getPlayerName(),
                    joinedPacket.getComputerName(),
                    joinedPacket.getProfileId()
            );
        }

        // PLAYER MOVEMENT

        else if (packet instanceof PlayerMovePacket movePacket) {

            listener.onPlayerMoved(
                    movePacket.getPlayerID(),
                    movePacket.getPlayerPosX(),
                    movePacket.getPlayerPosY(),
                    movePacket.getMoveDistance()
            );
        }

        // CARD REVELATION

        else if (packet instanceof CardShowPacket cardShowPacket) {

            listener.onCardShown(
                    cardShowPacket.getShowCard(),
                    cardShowPacket.getUser(),
                    cardShowPacket.getTargetUser(),
                    cardShowPacket.getTargetUserID()
            );
        }

        // GAME START

        else if (packet instanceof GameStartPacket startPacket
                && startPacket.getGameStart()) {

            listener.onGameStarted();
        }

        // GAME OVER

        else if (packet instanceof GameOverPacket gameOverPacket
                && gameOverPacket.getGameOver()) {

            listener.onGameOver(
                    gameOverPacket.getWinner()
            );

            listener.onGameOverPacket(
                    gameOverPacket
            );
        }
    }
}
