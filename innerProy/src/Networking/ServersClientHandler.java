package Networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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

    ServersClientHandler(Socket socket, GameServer server, int playerID) {
        this.socket = socket;
        this.server = server;
        this.playerID = playerID;
    }

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

    //getters

    int getPlayerID() {
        return playerID;
    }

    String getUserName() {
        return userName;
    }

    String getComputerName() {
        return computerName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    void setComputerName(String computerName) {
        this.computerName = computerName;
    }
}
