package Networking.server;

import Networking.common.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final GameServer server;
    private final int playerID;

    private volatile String userName;
    private volatile String computerName;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean disconnected;

    public ClientHandler(Socket socket, GameServer server, int playerID) {
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

    public synchronized void sendPacket(Packet packet) {
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

    public synchronized void disconnect() {
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

    public int getPlayerID() {
        return playerID;
    }

    public String getUserName() {
        return userName;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public Socket getSocket() {
        return socket;
    }

    public GameServer getServer() {
        return server;
    }

    public ObjectInputStream getInput() {
        return input;
    }

    public ObjectOutputStream getOutput() {
        return output;
    }
<<<<<<< HEAD

    public void setInput(ObjectInputStream input) {
        this.input = input;
    }
    public void setOutput(ObjectOutputStream output) {
        this.output = output;
    }

    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            disconnect();
            return;
        }

        try {
            while (!socket.isClosed()) {
                Object received = input.readObject();

                if (received instanceof Packet packet) {
                    server.handlePacket(packet, this);
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Unable to deserialize a packet from player " + playerID + ": " + e.getMessage());
        } catch (IOException e) {
            // A closed socket is the normal way for a client to disconnect.
        } finally {
            disconnect();
        }
    }

    public synchronized void sendPacket(Packet packet) {
        if (output == null) {
            return;
        }

        try {
            output.writeObject(packet);
            output.flush();
            output.reset();
        } catch (IOException e) {
            disconnect();
        }
    }

    public synchronized void disconnect() {

        if (disconnected) {
            return;
        }

        disconnected = true;

        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println(
                        "Error closing socket for player " + playerID + ": " + e.getMessage()
                );
            }
        }

        server.removeClient(this);
    }

    public int getPlayerID() {
        return playerID;
    }
    public String getUserName() {
        return userName;
    }
    public String getComputerName() {
        return computerName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }
    public Socket getSocket() {
        return socket;
    }
    public GameServer getServer() {
        return server;
    }
    public void setServer(GameServer server) {
        this.server = server;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
=======
>>>>>>> 0961104 (Complete networking implementation)
}
