package Networking.client;

import Networking.common.Packet;
import Networking.packets.JoinAcceptedPacket;
import Networking.packets.JoinPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameClient {

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

    public GameClient(String host, int port) {
        this(host, port, null);
    }

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

    private void notifyError(Exception exception) {
        if (listener != null) {
            listener.onError(exception);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public int getPlayerID() {
        return playerID;
    }
}
