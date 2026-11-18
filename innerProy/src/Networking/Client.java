package Networking;

import Networking.client.GameClient;
import Networking.common.Packet;
import Networking.packets.CardShowPacket;
import Networking.packets.ChatMessagePacket;
import Networking.packets.GameOverPacket;
import Networking.packets.GameStartPacket;
import Networking.packets.JoinAcceptedPacket;
import Networking.packets.PlayerJoinedPacket;
import Networking.packets.PlayerMovePacket;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public final class Client {

    //bunch of empty default functions, meant to not force an over-write from implementors from the get-go but instead force
    //the user to only over-write functions that it actually has to use

    /**
     * used by Client to notify Application when something arrives from Server
     */
    public interface Listener {
        default void onConnected(int playerID) {}

        default void onGameOverPacket(GameOverPacket packet) {}

        /**used when a player joins the game*/
        default void onPlayerJoined(int playerID, String playerName, String computerName) {}

        /**used when a player makes a move*/
        default void onPlayerMoved(int playerID, int x, int y, int distance) {}

        /**used when a player shows a card to another player*/
        default void onCardShown(Serializable card, String fromUser, String targetUser, int targetPlayerID) {}

        /**default game initializer*/
        default void onGameStarted() {}

        default void onGameOver(String winner) {}

        default void onChatMessage(int playerID, String senderName, String text) {}

        default void onDisconnected() {}

        default void onError(Exception exception) {
            exception.printStackTrace();
        }
    }

    /**the real Client backend engine, instead of inheritance I thought composition would be a better choice for this case
     * its package private so it cant be accessed externally*/
    private final GameClient client;
    private final Listener listener;
    private final List<BiConsumer<String, String>> chatListeners = new CopyOnWriteArrayList<>();

    public Client(String host, int port) {
        this(host, port, null);
    }

    /**we pass a host whitch is the Server, a port witch is the */
    public Client(String host, int port, Listener listener) {
        this.listener = listener;
        this.client = new GameClient(host, port, new GameClient.PacketListener() {
            @Override
            public void onPacket(Packet packet) {
                dispatch(packet);
            }

            @Override
            public void onDisconnected() {
                if (Client.this.listener != null) {
                    Client.this.listener.onDisconnected();
                }
            }

            @Override
            public void onError(Exception exception) {
                if (Client.this.listener != null) {
                    Client.this.listener.onError(exception);
                }
            }
        });
    }

    public void connect(String userName, String computerName) throws IOException {
        client.connect(userName, computerName);
    }

    public void startGame() {
        client.sendPacket(new GameStartPacket(true));
    }

    public void movePlayer(int x, int y, int distance) {
        client.sendPacket(new PlayerMovePacket(x, y, distance, client.getPlayerID()));
    }

    public void showCard(Serializable card, int targetPlayerID) {
        client.sendPacket(new CardShowPacket(card, null, null, targetPlayerID));
    }

    public void endGame(String winner) {
        client.sendPacket(new GameOverPacket(true, winner));
    }

    public void sendChatMessage(String message) {
        if (message == null || message.isBlank() || message.strip().length() > 500) {
            throw new IllegalArgumentException("Message must contain 1 to 500 characters");
        }
        client.sendPacket(new ChatMessagePacket(-1, null, message.strip()));
    }

    public void addChatListener(BiConsumer<String, String> listener) {
        chatListeners.add(java.util.Objects.requireNonNull(listener));
    }

    public void removeChatListener(BiConsumer<String, String> listener) {
        chatListeners.remove(listener);
    }

    public void disconnect() {
        client.disconnect();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public int getPlayerID() {
        return client.getPlayerID();
    }

    private void dispatch(Packet packet) {
        if (packet instanceof ChatMessagePacket chatPacket) {
            if (listener != null) {
                listener.onChatMessage(chatPacket.getSenderID(), chatPacket.getSenderName(), chatPacket.getMessage());
            }
            for (BiConsumer<String, String> chatListener : chatListeners) {
                chatListener.accept(chatPacket.getSenderName(), chatPacket.getMessage());
            }
            return;
        }
        if (listener == null) {
            return;
        }

        if (packet instanceof JoinAcceptedPacket acceptedPacket) {
            listener.onConnected(acceptedPacket.getPlayerID());
        } else if (packet instanceof PlayerJoinedPacket joinedPacket) {
            listener.onPlayerJoined(
                    joinedPacket.getPlayerID(),
                    joinedPacket.getPlayerName(),
                    joinedPacket.getComputerName()
            );
        } else if (packet instanceof PlayerMovePacket movePacket) {
            listener.onPlayerMoved(
                    movePacket.getPlayerID(),
                    movePacket.getPlayerPosX(),
                    movePacket.getPlayerPosY(),
                    movePacket.getMoveDistance()
            );
        } else if (packet instanceof CardShowPacket cardShowPacket) {
            listener.onCardShown(
                    cardShowPacket.getShowCard(),
                    cardShowPacket.getUser(),
                    cardShowPacket.getTargetUser(),
                    cardShowPacket.getTargetUserID()
            );
        } else if (packet instanceof GameStartPacket startPacket && startPacket.getGameStart()) {
            listener.onGameStarted();
        } else if (packet instanceof GameOverPacket gameOverPacket
                && gameOverPacket.getGameOver()) {

            listener.onGameOver(gameOverPacket.getWinner());
            listener.onGameOverPacket(gameOverPacket);
        }
    }
}
