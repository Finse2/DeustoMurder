package Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

final class GameServer {

    private final int port;
    private final Map<Integer, ServersClientHandler> clients = new ConcurrentHashMap<>();
    private final AtomicInteger nextPlayerID = new AtomicInteger(1);
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();

    private volatile boolean running;
    private volatile ServerSocket serverSocket;

    GameServer(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.port = port;
    }

    /**initializes the backend server
     *
     * @throws IllegalStateException if server is already running*/
    void start() {
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

    void stop() {
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
            case JOIN_ACCEPTED, PLAYER_JOINED ->
                    System.err.println("Ignoring server-only packet from player " + serversClientHandler.getPlayerID());
        }
    }

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

    private void broadcast(Packet packet) {
        for (ServersClientHandler client : clients.values()) {
            client.sendPacket(packet);
        }
    }

    private void broadcastExcept(Packet packet, ServersClientHandler excludedClient) {
        for (ServersClientHandler client : clients.values()) {
            if (client != excludedClient) {
                client.sendPacket(packet);
            }
        }
    }

    private void disconnectClients() {
        for (ServersClientHandler client : clients.values()) {
            client.disconnect();
        }
        clientExecutor.shutdownNow();
    }

    private String cleanName(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    void removeClient(ServersClientHandler serversClientHandler) {
        clients.remove(serversClientHandler.getPlayerID(), serversClientHandler);
    }

    int getClientCount() {
        return clients.size();
    }

    boolean isRunning() {
        return running;
    }
}
