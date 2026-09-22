package Networking;

/**lightweight wrapper class that exposes some of the functionality defined by the engine GameServer*/
public final class Server {

    public static final long serialVersionUID = 1L;

    private final GameServer server;

    public Server(int port) {
        server = new GameServer(port);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    public boolean isRunning() {
        return server.isRunning();
    }

    public int getClientCount() {
        return server.getClientCount();
    }
}
