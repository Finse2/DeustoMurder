package Networking;

import Networking.client.GameClient;
import Networking.common.Packet;
import Networking.packets.CardShowPacket;
import Networking.packets.GameOverPacket;
import Networking.packets.GameStartPacket;
import Networking.packets.JoinAcceptedPacket;
import Networking.packets.PlayerJoinedPacket;
import Networking.packets.PlayerMovePacket;
import Networking.server.GameServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Console tool for testing the networking layer on one or more computers.
 *
 * Server:
 *   java Networking.NetworkTestMain server 23456
 *
 * Client:
 *   java Networking.NetworkTestMain client 192.168.1.20 23456 Luka Luka-PC
 */
public final class NetworkTestMain {

    private NetworkTestMain() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            runInteractiveSetup();
            return;
        }

        if ("help".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "server" -> runServer(args);
            case "client" -> runClient(args);
            default -> {
                System.err.println("Unknown mode: " + args[0]);
                printUsage();
            }
        }
    }

    private static void runInteractiveSetup() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose mode:");
        System.out.println("1 - Server");
        System.out.println("2 - Client");
        System.out.print("> ");
        String mode = reader.readLine();

        if ("1".equals(mode)) {
            int port = readPort(reader);
            runServer(new String[]{"server", String.valueOf(port)});
            return;
        }

        if ("2".equals(mode)) {
            System.out.print("Server IP or hostname: ");
            String host = reader.readLine();
            int port = readPort(reader);
            System.out.print("Username: ");
            String userName = reader.readLine();
            System.out.print("Computer name: ");
            String computerName = reader.readLine();

            runClient(new String[]{
                    "client",
                    host,
                    String.valueOf(port),
                    userName,
                    computerName
            });
            return;
        }

        System.out.println("Invalid choice.");
        printUsage();
    }

    private static int readPort(BufferedReader reader) throws IOException {
        while (true) {
            System.out.print("Port [23456]: ");
            String portText = reader.readLine();
            if (portText == null || portText.isBlank()) {
                return 23456;
            }

            try {
                return parsePort(portText);
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter a numeric port between 1 and 65535.");
            }
        }
    }

    private static void runServer(String[] args) {
        int port = args.length >= 2 ? parsePort(args[1]) : 23456;
        GameServer server = new GameServer(port);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("Starting test server. Press Ctrl+C to stop.");
        server.start();
    }

    private static void runClient(String[] args) throws IOException, InterruptedException {
        if (args.length < 5) {
            printUsage();
            return;
        }

        String host = args[1];
        int port = parsePort(args[2]);
        String userName = args[3];
        String computerName = args[4];

        GameClient client = new GameClient(host, port, new ConsolePacketListener());
        try {
            client.connect(userName, computerName);
            waitForPlayerID(client);
            System.out.println("Connected as player " + client.getPlayerID() + ".");
            System.out.println("Type 'help' to see test commands.");
            runClientCommands(client);
        } finally {
            client.disconnect();
        }
    }

    private static void runClientCommands(GameClient client) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (client.isConnected()) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                return;
            }

            String[] command = line.trim().split("\\s+");
            if (command.length == 0 || command[0].isBlank()) {
                continue;
            }

            try {
                switch (command[0].toLowerCase()) {
                    case "help" -> printClientCommands();
                    case "start" -> client.sendPacket(new GameStartPacket(true));
                    case "move" -> sendMove(client, command);
                    case "show" -> sendCardShow(client, command);
                    case "demo" -> runDemo(client, command);
                    case "over" -> {
                        String winner = command.length >= 2 ? command[1] : "unknown";
                        client.sendPacket(new GameOverPacket(true, winner));
                    }
                    case "id" -> System.out.println("Assigned player ID: " + client.getPlayerID());
                    case "quit", "exit" -> {
                        return;
                    }
                    default -> System.out.println("Unknown command. Type 'help'.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid command: " + e.getMessage());
            }
        }
    }

    private static void sendMove(GameClient client, String[] command) {
        if (command.length != 4) {
            throw new IllegalArgumentException("Usage: move <x> <y> <distance>");
        }

        int x = Integer.parseInt(command[1]);
        int y = Integer.parseInt(command[2]);
        int distance = Integer.parseInt(command[3]);
        client.sendPacket(new PlayerMovePacket(x, y, distance, client.getPlayerID()));
    }

    private static void sendCardShow(GameClient client, String[] command) {
        if (command.length != 2) {
            throw new IllegalArgumentException("Usage: show <target-player-id>");
        }

        int targetID = Integer.parseInt(command[1]);
        client.sendPacket(new CardShowPacket(null, "test-client", "target-" + targetID, targetID));
    }

    private static void runDemo(GameClient client, String[] command) throws InterruptedException {
        if (command.length != 2) {
            throw new IllegalArgumentException("Usage: demo <target-player-id>");
        }

        int targetID = Integer.parseInt(command[1]);
        System.out.println("Sending GAME_START...");
        client.sendPacket(new GameStartPacket(true));
        Thread.sleep(250);

        System.out.println("Sending PLAYER_MOVE...");
        client.sendPacket(new PlayerMovePacket(4, 5, 2, client.getPlayerID()));
        Thread.sleep(250);

        System.out.println("Sending CARD_SHOW...");
        client.sendPacket(new CardShowPacket(null, "test-client", "target-" + targetID, targetID));
        Thread.sleep(250);

        System.out.println("Sending GAME_OVER...");
        client.sendPacket(new GameOverPacket(true, "demo-player"));
    }

    private static void waitForPlayerID(GameClient client) throws IOException {
        long deadline = System.currentTimeMillis() + 5000;
        while (client.getPlayerID() < 0 && client.isConnected()
                && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for join acknowledgment", e);
            }
        }

        if (client.getPlayerID() < 0) {
            throw new IOException("The server did not send JOIN_ACCEPTED within 5 seconds");
        }
    }

    private static int parsePort(String value) {
        int port = Integer.parseInt(value);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        return port;
    }

    private static void printUsage() {
        System.out.println("""
                Networking test tool

                Start a server:
                  java Networking.NetworkTestMain server [port]

                Start a client:
                  java Networking.NetworkTestMain client <server-ip> <port> <username> <computer-name>

                Example:
                  java Networking.NetworkTestMain server 23456
                  java Networking.NetworkTestMain client 192.168.1.20 23456 Luka Luka-PC
                """);
    }

    private static void printClientCommands() {
        System.out.println("""
                Client commands:
                  start                 Broadcast GAME_START
                  move <x> <y> <distance>  Broadcast PLAYER_MOVE
                  show <target-id>      Send CARD_SHOW to one client
                  over [winner]         Broadcast GAME_OVER
                  demo <target-id>      Send all test packet types
                  id                    Print this client's assigned ID
                  quit                  Disconnect and exit
                """);
    }

    private static final class ConsolePacketListener implements GameClient.PacketListener {

        @Override
        public void onPacket(Packet packet) {
            if (packet instanceof JoinAcceptedPacket acceptedPacket) {
                System.out.println("\nJOIN_ACCEPTED: assigned ID " + acceptedPacket.getPlayerID());
            } else if (packet instanceof PlayerJoinedPacket joinedPacket) {
                System.out.println("\nPLAYER_JOINED: " + joinedPacket.getPlayerName()
                        + " (" + joinedPacket.getComputerName() + "), ID "
                        + joinedPacket.getPlayerID());
            } else if (packet instanceof PlayerMovePacket movePacket) {
                System.out.println("\nPLAYER_MOVE: player " + movePacket.getPlayerID()
                        + " -> (" + movePacket.getPlayerPosX() + ", "
                        + movePacket.getPlayerPosY() + "), distance "
                        + movePacket.getMoveDistance());
            } else if (packet instanceof CardShowPacket cardShowPacket) {
                System.out.println("\nCARD_SHOW: " + cardShowPacket.getUser()
                        + " -> " + cardShowPacket.getTargetUser());
            } else if (packet instanceof GameStartPacket) {
                System.out.println("\nGAME_START received");
            } else if (packet instanceof GameOverPacket gameOverPacket) {
                System.out.println("\nGAME_OVER received. Winner: " + gameOverPacket.getWinner());
            } else {
                System.out.println("\nReceived packet: " + packet.getPacketType());
            }
            System.out.print("> ");
        }

        @Override
        public void onDisconnected() {
            System.out.println("\nDisconnected from server.");
        }

        @Override
        public void onError(Exception exception) {
            System.err.println("\nNetwork error: " + exception.getMessage());
        }
    }
}
