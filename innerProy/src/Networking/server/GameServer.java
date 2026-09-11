package Networking.server;

import Networking.common.Packet;
import Networking.common.PacketType;
import Networking.packets.CardShowPacket;
import Networking.packets.GameOverPacket;
import Networking.packets.GameStartPacket;
import Networking.packets.JoinAcceptedPacket;
import Networking.packets.JoinPacket;
import Networking.packets.PlayerJoinedPacket;
import Networking.packets.PlayerMovePacket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {

    private final int port;
    private final Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private final AtomicInteger nextPlayerID = new AtomicInteger(1);
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();

    private volatile boolean running;
    private volatile ServerSocket serverSocket;

    public GameServer(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        }
        this.port = port;
    }

    public void start() {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }

        try (ServerSocket listeningSocket = new ServerSocket(port)) {
            serverSocket = listeningSocket;
            running = true;
            System.out.println("Server started on port " + listeningSocket.getLocalPort());

            while (running) {
                Socket clientSocket = listeningSocket.accept();
                int playerID = nextPlayerID.getAndIncrement();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, playerID);
                clients.put(playerID, clientHandler);
                clientExecutor.execute(clientHandler);
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

    public void handlePacket(Packet packet, ClientHandler clientHandler) {
        if (packet == null || clientHandler == null) {
            return;
        }

        PacketType type = packet.getPacketType();
        if (type == null) {
            return;
        }

        switch (type) {
            case JOIN -> handleJoin(packet, clientHandler);
            case CARD_SHOW -> handleCardShow(packet, clientHandler);
            case GAME_OVER -> broadcast(packet);
            case PLAYER_MOVE -> handlePlayerMove(packet, clientHandler);
            case GAME_START -> broadcast(packet);
            case JOIN_ACCEPTED, PLAYER_JOINED ->
                    System.err.println("Ignoring server-only packet from player " + clientHandler.getPlayerID());
        }
    }

    private void handleJoin(Packet packet, ClientHandler clientHandler) {
        if (!(packet instanceof JoinPacket joinPacket)) {
            return;
        }

        String userName = cleanName(joinPacket.getUserName(), "Player " + clientHandler.getPlayerID());
        String computerName = cleanName(joinPacket.getComputerName(), "Unknown computer");
        clientHandler.setUserName(userName);
        clientHandler.setComputerName(computerName);

        clientHandler.sendPacket(new JoinAcceptedPacket(clientHandler.getPlayerID()));

        for (ClientHandler existingClient : clients.values()) {
            if (existingClient == clientHandler || existingClient.getUserName() == null) {
                continue;
            }
            clientHandler.sendPacket(new PlayerJoinedPacket(
                    existingClient.getPlayerID(),
                    existingClient.getUserName(),
                    existingClient.getComputerName()
            ));
        }

        broadcastExcept(
                new PlayerJoinedPacket(clientHandler.getPlayerID(), userName, computerName),
                clientHandler
        );
        System.out.println("Player joined: " + userName + " from " + computerName);
    }

    private void handleCardShow(Packet packet, ClientHandler clientHandler) {
        if (!(packet instanceof CardShowPacket cardShowPacket)) {
            return;
        }

        ClientHandler target = clients.get(cardShowPacket.getTargetUserID());
        if (target == null) {
            System.err.println("Target player not found: " + cardShowPacket.getTargetUserID());
            return;
        }

        target.sendPacket(new CardShowPacket(
                cardShowPacket.getShowCard(),
                clientHandler.getUserName(),
                target.getUserName(),
                target.getPlayerID()
        ));
    }

    private void handlePlayerMove(Packet packet, ClientHandler clientHandler) {
        if (!(packet instanceof PlayerMovePacket movePacket)) {
            return;
        }

        PlayerMovePacket authoritativeMove = new PlayerMovePacket(
                movePacket.getPlayerPosX(),
                movePacket.getPlayerPosY(),
                movePacket.getMoveDistance(),
                clientHandler.getPlayerID()
        );
        broadcastExcept(authoritativeMove, clientHandler);
    }

    private void broadcast(Packet packet) {
        for (ClientHandler client : clients.values()) {
            client.sendPacket(packet);
        }
    }

    private void broadcastExcept(Packet packet, ClientHandler excludedClient) {
        for (ClientHandler client : clients.values()) {
            if (client != excludedClient) {
                client.sendPacket(packet);
            }
        }
    }

    private void disconnectClients() {
        for (ClientHandler client : clients.values()) {
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

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler.getPlayerID(), clientHandler);
    }

    public int getClientCount() {
        return clients.size();
    }

    public boolean isRunning() {
        return running;
    }
}
