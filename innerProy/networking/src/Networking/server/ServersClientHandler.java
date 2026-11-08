package Networking.server;

import Networking.common.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
/**
 * This class handles the communication between the server and a connected client.
 * It manages the input and output streams for the socket connection, processes incoming packets,
 * and sends packets to the client. It also handles disconnection of the client.
 */
final class ServersClientHandler implements Runnable {

    private final Socket socket;    //connects to the Client

    private final GameServer server;

    private final int playerID; //unique ID that each machine has, if 2 clients have the same playerID then we throw both of them out

    /**the name the client chooses for itself*/
    private volatile String userName;

    private volatile String computerName;

    /**input for the socket*/
    private ObjectInputStream input;
    /**output for the socket*/
    private ObjectOutputStream output;

    /**boolean stated weather the selected player is active*/
    private boolean disconnected;
/**
     * Constructor for the ServersClientHandler class.
     *
     * @param socket The socket connection to the client.
     * @param server The GameServer instance managing this client.
     * @param playerID The unique player ID assigned to this client.
     */
    ServersClientHandler(Socket socket, GameServer server, int playerID) {
        this.socket = socket;
        this.server = server;
        this.playerID = playerID;
    }
/**
     * The main loop for handling incoming packets from the client.
     * It reads packets from the input stream and passes them to the server for processing.
     * If an error occurs or the connection is lost, it disconnects the client.
     */
    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            while (!socket.isClosed()) {
                Object received = input.readObject();
                if (!(received instanceof Packet packet)) {
                    System.err.println("Ignoring non-packet data from player " + playerID);
                    continue;
                }
                server.handlePacket(packet, this);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Unable to deserialize a packet from player " + playerID + ": " + e.getMessage());
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Connection lost for player " + playerID + ": " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    /**send binary through a packet*/
    synchronized void sendPacket(Packet packet) {
        if (packet == null || disconnected || output == null) {
            return;
        }

        try {
            output.writeObject(packet);
            output.flush();
            output.reset();
        } catch (IOException e) {
            System.err.println("Failed to send packet to player " + playerID + ": " + e.getMessage());
            disconnect();
        }
    }

    /**disconnects a player from the server*/
    synchronized void disconnect() {
        if (disconnected) {
            return;
        }
        disconnected = true;

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket for player " + playerID + ": " + e.getMessage());
        } finally {
            server.removeClient(this);
        }
    }

// Getters and Setters for playerID.
    int getPlayerID() {
        return playerID;
    }
// Getter for the user name.
    String getUserName() {
        return userName;
    }
// Getter for the computer name.
    String getComputerName() {
        return computerName;
    }
// Setter for the user name.
    void setUserName(String userName) {
        this.userName = userName;
    }
// Setter for the computer name.
    void setComputerName(String computerName) {
        this.computerName = computerName;
    }
}
