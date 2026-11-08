package Networking.client;

import Networking.common.Packet;
import Networking.packets.JoinAcceptedPacket;
import Networking.packets.JoinPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
/**
 * This class represents a client that connects to a game server.
 * It manages the connection to the server, sending and receiving packets,
 * and notifying listeners of incoming packets or errors.
 */
public final class GameClient {
/**
     * This interface defines the methods that a listener must implement to receive notifications
     * about incoming packets, disconnections, and errors from the GameClient.
     */
    public interface PacketListener {
        void onPacket(Packet packet);

        default void onDisconnected() {
        }

        default void onError(Exception exception) {
            exception.printStackTrace();
        }
    }

    private final String host;
    private final int port;
    private final PacketListener listener;

    private volatile boolean connected;
    private volatile int playerID = -1;

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Thread receiveThread;

/**
     * Constructor for the GameClient class.
     *
     * @param host The hostname or IP address of the server to connect to.
     * @param port The port number of the server to connect to.
     * @param listener The listener that will receive notifications about incoming packets, disconnections, and errors.
     * @throws IllegalArgumentException if the host is null or blank, or if the port is not between 1 and 65535.
     */
    public GameClient(String host, int port, PacketListener listener) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host cannot be blank");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.host = host;
        this.port = port;
        this.listener = listener;
    }
/**
     * Connects to the server with the specified user name and computer name.
     * This method establishes a socket connection to the server, initializes the input and output streams,
     * starts a thread to listen for incoming packets, and sends a JoinPacket to the server.
     *
     * @param userName The name of the user who wants to join the game.
     * @param computerName The name of the computer from which the user is joining.
     * @throws IOException if an I/O error occurs when creating the socket or streams.
     * @throws IllegalStateException if the client is already connected.
     */
    public synchronized void connect(String userName, String computerName) throws IOException {
        if (connected) {
            throw new IllegalStateException("Client is already connected");
        }

        Socket newSocket = new Socket(host, port);
        try {
            ObjectOutputStream newOutput = new ObjectOutputStream(newSocket.getOutputStream());
            newOutput.flush();
            ObjectInputStream newInput = new ObjectInputStream(newSocket.getInputStream());

            socket = newSocket;
            output = newOutput;
            input = newInput;
            connected = true;

            receiveThread = new Thread(this::receiveLoop, "game-client-receiver");
            receiveThread.setDaemon(true);
            receiveThread.start();

            sendPacket(new JoinPacket(computerName, userName));
        } catch (IOException | RuntimeException e) {
            try {
                newSocket.close();
            } catch (IOException closeException) {
                e.addSuppressed(closeException);
            }
            throw e;
        }
    }
/**
     * Sends a packet to the server.
     *
     * @param packet The packet to send.
     * @throws IllegalStateException if the client is not connected.
     */
    public synchronized void sendPacket(Packet packet) {
        if (!connected || output == null) {
            throw new IllegalStateException("Client is not connected");
        }

        try {
            output.writeObject(packet);
            output.flush();
            output.reset();
        } catch (IOException e) {
            notifyError(e);
            disconnect();
        }
    }
/**
     * Disconnects from the server.
     * This method closes the socket and input/output streams, and notifies the listener of the disconnection.
     */
    public synchronized void disconnect() {
        if (!connected && socket == null) {
            return;
        }
        connected = false;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                notifyError(e);
            } finally {
                socket = null;
                input = null;
                output = null;
            }
        }

        if (listener != null) {
            listener.onDisconnected();
        }
    }
/**
     * The receive loop that continuously listens for incoming packets from the server.
     * When a packet is received, it is dispatched to the listener. If an error occurs, it is notified to the listener.
     */
    private void receiveLoop() {
        try {
            while (connected) {
                Object received = input.readObject();
                if (!(received instanceof Packet packet)) {
                    notifyError(new IOException("Received non-packet data"));
                    continue;
                }

                if (packet instanceof JoinAcceptedPacket acceptedPacket) {
                    playerID = acceptedPacket.getPlayerID();
                }
                if (listener != null) {
                    listener.onPacket(packet);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            if (connected) {
                notifyError(e);
            }
        } finally {
            disconnect();
        }
    }
/**
     * Notifies the listener of an error.
     *
     * @param exception The exception that occurred.
     */
    private void notifyError(Exception exception) {
        if (listener != null) {
            listener.onError(exception);
        }
    }
/**
     * Checks if the client is currently connected to the server.
     *
     * @return true if the client is connected, false otherwise.
     */
    public boolean isConnected() {
        return connected;
    }
/**
     * Gets the player ID assigned to this client by the server.
     *
     * @return The player ID, or -1 if the client is not connected or has not received a player ID.
     */
    public int getPlayerID() {
        return playerID;
    }
}
