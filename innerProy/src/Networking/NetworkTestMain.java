package Networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
/**
 * This class serves as the main entry point for testing the networking functionality of the application.
 * It allows users to start a server or a client, and provides an interactive command-line interface for testing.
 * The class handles user input, manages server and client instances, and provides feedback on network events.
 * THIS TESTING CLASS IS MADE WITH CHAT-GPT, IT'S ONLY PURPOSE IS TO TEST THE NETWORKING FUNCTIONALITY, IT IS NOT PART OF THE FINAL GAME.
 */
final class NetworkTestMain {

    private NetworkTestMain() {}
    /**
     * The main method that starts the application.
     * It processes command-line arguments to determine whether to start a server or a client,
     * or to run in interactive mode if no arguments are provided.
     *
     * @param args Command-line arguments specifying the mode and parameters for the server or client.
     * @throws Exception If an error occurs during execution.
     */
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
/**
     * Runs the interactive setup for starting a server or client.
     * It prompts the user for input to choose the mode and relevant parameters.
     *
     * @throws Exception If an error occurs during execution.
     */
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
/**
     * Reads a port number from the user input.
     * If the user provides no input, the default port 23456 is used.
     * The method validates the input to ensure it is a valid port number.
     *
     * @param reader BufferedReader for reading user input.
     * @return The port number to use for the server or client.
     * @throws IOException If an I/O error occurs while reading input.
     */
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
/**
     * Starts the server with the specified command-line arguments.
     * It initializes the server on the given port and sets up a shutdown hook to stop the server gracefully.
     *
     * @param args Command-line arguments specifying the port for the server.
     */
    private static void runServer(String[] args) {
        int port = args.length >= 2 ? parsePort(args[1]) : 23456;
        Server server = new Server(port);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("Starting test server. Press Ctrl+C to stop.");
        server.start();
    }
/**
     * Starts the client with the specified command-line arguments.
     * It connects to the server using the provided host, port, username, and computer name.
     *
     * @param args Command-line arguments specifying the server host, port, username, and computer name.
     * @throws IOException If an I/O error occurs during connection or communication with the server.
     */
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
/**
     * Runs the command loop for the client, allowing the user to input commands to interact with the server.
     * It processes commands such as starting the game, moving players, showing cards, and ending the game.
     *
     * @param client The Client instance to send commands to.
     * @throws IOException If an I/O error occurs while reading user input or communicating with the server.
     */
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
/**
     * Sends a player move command to the server.
     * It parses the command arguments to extract the x, y coordinates and distance, and sends the move packet.
     *
     * @param client  The Client instance to send the move command to.
     * @param command The command arguments containing the move parameters.
     */
    private static void sendMove(Client client, String[] command) {
        if (command.length != 4) {
            throw new IllegalArgumentException("Usage: move <x> <y> <distance>");
        }

        int x = Integer.parseInt(command[1]);
        int y = Integer.parseInt(command[2]);
        int distance = Integer.parseInt(command[3]);
        client.movePlayer(x, y, distance);
    }
/**
     * Sends a card show command to the server.
     * It parses the command arguments to extract the target player ID and sends the card show packet.
     *
     * @param client  The Client instance to send the card show command to.
     * @param command The command arguments containing the target player ID.
     */
    private static void sendCardShow(Client client, String[] command) {
        if (command.length != 2) {
            throw new IllegalArgumentException("Usage: show <target-player-id>");
        }

        int targetID = Integer.parseInt(command[1]);
        client.showCard(null, targetID);
    }
/**
     * Waits for the client to receive a player ID from the server after sending a join request.
     * It checks periodically for up to 5 seconds and throws an exception if the player ID is not received in time.
     *
     * @param client The Client instance to wait for the player ID.
     * @throws IOException If the player ID is not received within the timeout period or if interrupted.
     */
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
// If the player ID is still not received after the timeout, throw an exception.
        if (client.getPlayerID() < 0) {
            throw new IOException("The server did not send JOIN_ACCEPTED within 5 seconds");
        }
    }
/**
     * Parses a string value into an integer port number and validates it.
     * The port number must be between 1 and 65535.
     *
     * @param value The string representation of the port number.
     * @return The parsed port number as an integer.
     * @throws IllegalArgumentException If the port number is not valid.
     */
    private static int parsePort(String value) {
        int port = Integer.parseInt(value);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        return port;
    }
/**
     * Prints the usage instructions for the networking test tool.
     * It provides information on how to start a server or client and the required command-line arguments.
     */
    private static void printUsage() {
        System.out.println("""
                Networking test tool

                Start a server:
                  java Networking.NetworkTestMain server [port]

                Start a client:
                  java Networking.NetworkTestMain client <server-ip> <port> <username> <computer-name>
                """);
    }
/**
     * Prints the available client commands for interacting with the server.
     * It provides a list of commands that can be used to test various networking functionalities.
     */
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
/**
     * A simple implementation of the Client.Listener interface that prints network events to the console.
     * It provides feedback on connection status, player actions, and errors during client-server communication.
     */
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
        public void onCardShown(Serializable card, String fromUser, String targetUser, int targetPlayerID) {
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
