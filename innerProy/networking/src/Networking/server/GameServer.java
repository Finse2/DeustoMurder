package Networking.server;

import Networking.common.Packet;
import Networking.common.PacketType;
import Networking.packets.CardShowPacket;
import Networking.packets.ChatMessagePacket;
import Networking.packets.GameOverPacket;
import Networking.packets.JoinAcceptedPacket;
import Networking.packets.JoinPacket;
import Networking.packets.PlayerJoinedPacket;
import Networking.packets.PlayerMovePacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * This class represents a game server that listens for incoming client connections and handles communication between clients.
 * It manages the connected clients, assigns player IDs, and processes packets sent by clients.
 */
public final class GameServer {

    private final int port;
    private final Map<Integer, ServersClientHandler> clients = new ConcurrentHashMap<>();
    private final AtomicInteger nextPlayerID = new AtomicInteger(1);
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();

    private volatile boolean running;
    private volatile ServerSocket serverSocket;
/**
     * Constructor for the GameServer class.
     *
     * @param port The port number on which the server will listen for incoming client connections.
     * @throws IllegalArgumentException if the port number is not between 1 and 65535.
     */
    public GameServer(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.port = port;
    }

    /**initializes the backend server
     *
     * @throws IllegalStateException if server is already running
     * */
    public void start() {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }

        try (ServerSocket listeningSocket = new ServerSocket(port)) {
            serverSocket = listeningSocket;
            running = true;
            InetAddress localAddress = InetAddress.getLocalHost();
            int listeningPort = listeningSocket.getLocalPort();
            System.out.println("Server started:");
            System.out.println("  Host name: " + localAddress.getHostName());
            System.out.println("  IP address: " + localAddress.getHostAddress());
            System.out.println("  Port: " + listeningPort);
            System.out.println("Clients can connect using "
                    + localAddress.getHostName() + ":" + listeningPort);

            while (running) {
                Socket clientSocket = listeningSocket.accept();
                int playerID = nextPlayerID.getAndIncrement();
                ServersClientHandler serversClientHandler = new ServersClientHandler(clientSocket, this, playerID);
                clients.put(playerID, serversClientHandler);
                clientExecutor.execute(serversClientHandler);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Server stopped because it could not accept clients: " + e.getMessage());
            }
        } finally {
            running = false;
            serverSocket = null;
            disconnectClients();
        }
    }
/**
     * Stops the server and disconnects all connected clients.
     * This method is thread-safe and can be called from any thread.
     */
    public void stop() {
        running = false;
        ServerSocket socket = serverSocket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        disconnectClients();
    }
/**
     * Handles a packet received from a client.
     *
     * @param packet The packet received from the client.
     * @param serversClientHandler The client handler that received the packet.
     */
    void handlePacket(Packet packet, ServersClientHandler serversClientHandler) {
        if (packet == null || serversClientHandler == null) {
            return;
        }

        PacketType type = packet.getPacketType();
        if (type == null) {
            return;
        }

        switch (type) {
            case JOIN -> handleJoin(packet, serversClientHandler);
            case CARD_SHOW -> handleCardShow(packet, serversClientHandler);
            case GAME_OVER -> broadcast(packet);
            case PLAYER_MOVE -> handlePlayerMove(packet, serversClientHandler);
            case GAME_START -> broadcast(packet);
            case CHAT_MESSAGE -> handleChatMessage(packet, serversClientHandler);
            case JOIN_ACCEPTED, PLAYER_JOINED ->
                    System.err.println("Ignoring server-only packet from player " + serversClientHandler.getPlayerID());
        }
    }
/**
     * Handles a JOIN packet received from a client.
     *
     * @param packet The JOIN packet received from the client.
     * @param serversClientHandler The client handler that received the packet.
     */
    private void handleJoin(Packet packet, ServersClientHandler serversClientHandler) {
        if (!(packet instanceof JoinPacket joinPacket)) {
            return;
        }

        String userName = cleanName(joinPacket.getUserName(), "Player " + serversClientHandler.getPlayerID());
        String computerName = cleanName(joinPacket.getComputerName(), "Unknown computer");
        serversClientHandler.setUserName(userName);
        serversClientHandler.setComputerName(computerName);

        serversClientHandler.sendPacket(new JoinAcceptedPacket(serversClientHandler.getPlayerID()));

        for (ServersClientHandler existingClient : clients.values()) {
            if (existingClient == serversClientHandler || existingClient.getUserName() == null) {
                continue;
            }
            serversClientHandler.sendPacket(new PlayerJoinedPacket(
                    existingClient.getPlayerID(),
                    existingClient.getUserName(),
                    existingClient.getComputerName()
            ));
        }

        broadcastExcept(
                new PlayerJoinedPacket(serversClientHandler.getPlayerID(), userName, computerName),
                serversClientHandler
        );
        System.out.println("Player joined: " + userName + " from " + computerName);
    }
/**
     * Handles a CARD_SHOW packet received from a client.
     *
     * @param packet The CARD_SHOW packet received from the client.
     * @param serversClientHandler The client handler that received the packet.
     */
    /** Retransmite mensajes a todos los clientes excepto al emisor (que ya los muestra localmente). */
    private void handleChatMessage(Packet packet, ServersClientHandler sender) {
        if (!(packet instanceof ChatMessagePacket chatPacket) || sender.getUserName() == null) {
            return;
        }
        String message = chatPacket.getMessage();
        if (message == null || message.isBlank() || message.strip().length() > 500) {
            return;
        }
        // El nombre y el ID se obtienen de la conexión, nunca del paquete del cliente.
        ChatMessagePacket authoritative = new ChatMessagePacket(
                sender.getPlayerID(), sender.getUserName(), message.strip()
        );
        broadcastExcept(authoritative, sender);
    }

    private void handleCardShow(Packet packet, ServersClientHandler serversClientHandler) {
        if (!(packet instanceof CardShowPacket cardShowPacket)) {
            return;
        }

        ServersClientHandler target = clients.get(cardShowPacket.getTargetUserID());
        if (target == null) {
            System.err.println("Target player not found: " + cardShowPacket.getTargetUserID());
            return;
        }

        target.sendPacket(new CardShowPacket(
                cardShowPacket.getShowCard(),
                serversClientHandler.getUserName(),
                target.getUserName(),
                target.getPlayerID()
        ));
    }
    /**
     * Handles a PLAYER_MOVE packet received from a client.
     *
     * @param packet The PLAYER_MOVE packet received from the client.
     * @param serversClientHandler The client handler that received the packet.
     */
    private void handlePlayerMove(Packet packet, ServersClientHandler serversClientHandler) {
        if (!(packet instanceof PlayerMovePacket movePacket)) {
            return;
        }

        PlayerMovePacket authoritativeMove = new PlayerMovePacket(
                movePacket.getPlayerPosX(),
                movePacket.getPlayerPosY(),
                movePacket.getMoveDistance(),
                serversClientHandler.getPlayerID()
        );
        broadcastExcept(authoritativeMove, serversClientHandler);
    }
/**
     * Broadcasts a packet to all connected clients.
     *
     * @param packet The packet to broadcast.
     */
    private void broadcast(Packet packet) {
        for (ServersClientHandler client : clients.values()) {
            client.sendPacket(packet);
        }
    }
/**
     * Broadcasts a packet to all connected clients except for the specified client.
     *
     * @param packet The packet to broadcast.
     * @param excludedClient The client to exclude from the broadcast.
     */
    private void broadcastExcept(Packet packet, ServersClientHandler excludedClient) {
        for (ServersClientHandler client : clients.values()) {
            if (client != excludedClient) {
                client.sendPacket(packet);
            }
        }
    }
/**
     * Disconnects all connected clients and shuts down the client executor service.
     */
    private void disconnectClients() {
        for (ServersClientHandler client : clients.values()) {
            client.disconnect();
        }
        clientExecutor.shutdownNow();
    }
/**
     * Cleans a string value by trimming whitespace and providing a fallback if the value is null or blank.
     *
     * @param value The string value to clean.
     * @param fallback The fallback value to use if the input value is null or blank.
     * @return The cleaned string value or the fallback if the input was null or blank.
     */
    private String cleanName(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
/**
     * Removes a client from the server's client list.
     *
     * @param serversClientHandler The client handler to remove.
     */
    void removeClient(ServersClientHandler serversClientHandler) {
        clients.remove(serversClientHandler.getPlayerID(), serversClientHandler);
    }
/**
     * Gets the current number of connected clients.
     *
     * @return The number of connected clients.
     */
    public int getClientCount() {
        return clients.size();
    }
/**
     * Checks if the server is currently running.
     *
     * @return true if the server is running, false otherwise.
     */
    public boolean isRunning() {
        return running;
    }
}
