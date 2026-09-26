package Networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

final class NetworkTestMain {

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
        Server server = new Server(port);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("Starting test server. Press Ctrl+C to stop.");
        server.start();
    }

    private static void runClient(String[] args) throws IOException {
        if (args.length < 5) {
            printUsage();
            return;
        }

        String host = args[1];
        int port = parsePort(args[2]);
        String userName = args[3];
        String computerName = args[4];

        Client client = new Client(host, port, new ConsoleClientListener());
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

    private static void runClientCommands(Client client) throws IOException {
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
                    case "start" -> client.startGame();
                    case "move" -> sendMove(client, command);
                    case "show" -> sendCardShow(client, command);
                    case "over" -> {
                        String winner = command.length >= 2 ? command[1] : "unknown";
                        client.endGame(winner);
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

    private static void sendMove(Client client, String[] command) {
        if (command.length != 4) {
            throw new IllegalArgumentException("Usage: move <x> <y> <distance>");
        }

        int x = Integer.parseInt(command[1]);
        int y = Integer.parseInt(command[2]);
        int distance = Integer.parseInt(command[3]);
        client.movePlayer(x, y, distance);
    }

    private static void sendCardShow(Client client, String[] command) {
        if (command.length != 2) {
            throw new IllegalArgumentException("Usage: show <target-player-id>");
        }

        int targetID = Integer.parseInt(command[1]);
        client.showCard(null, targetID);
    }

    private static void waitForPlayerID(Client client) throws IOException {
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
                """);
    }

    private static void printClientCommands() {
        System.out.println("""
                Client commands:
                  start                    Broadcast GAME_START
                  move <x> <y> <distance>  Broadcast PLAYER_MOVE
                  show <target-id>         Send CARD_SHOW to one client
                  over [winner]            Broadcast GAME_OVER
                  id                       Print this client's assigned ID
                  quit                     Disconnect and exit
                """);
    }

    private static final class ConsoleClientListener implements Client.Listener {
        @Override
        public void onConnected(int playerID) {
            System.out.println("\nJOIN_ACCEPTED: assigned ID " + playerID);
        }

        @Override
        public void onPlayerJoined(int playerID, String playerName, String computerName) {
            System.out.println("\nPLAYER_JOINED: " + playerName
                    + " (" + computerName + "), ID " + playerID);
        }

        @Override
        public void onPlayerMoved(int playerID, int x, int y, int distance) {
            System.out.println("\nPLAYER_MOVE: player " + playerID
                    + " -> (" + x + ", " + y + "), distance " + distance);
        }

        @Override
        public void onCardShown(model.Card card, String fromUser, String targetUser, int targetPlayerID) {
            System.out.println("\nCARD_SHOW: " + fromUser + " -> " + targetUser);
        }

        @Override
        public void onGameStarted() {
            System.out.println("\nGAME_START received");
        }

        @Override
        public void onGameOver(String winner) {
            System.out.println("\nGAME_OVER received. Winner: " + winner);
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
