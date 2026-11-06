package activation.Screen.Lobby;
import Networking.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
/**
 * The LobbyControl class is responsible for managing the server-side control of a game lobby.
 * It provides methods to host a server, check its running status, and retrieve the number of connected clients.
 * The class implements AutoCloseable to ensure proper resource management by stopping the server when no longer needed.
 */
public final class LobbyControl implements AutoCloseable {

        private final Server server;
        private final Thread serverThread;
/**
         * Private constructor for the LobbyControl class.
         * Initializes the server and its associated thread.
         *
         * @param server The Server instance that manages the game lobby.
         * @param serverThread The Thread instance that runs the server.
         */
        private LobbyControl(Server server, Thread serverThread) {
            this.server = server;
            this.serverThread = serverThread;
        }
/**
         * Hosts a server on the specified port and returns a LobbyControl instance to manage it.
         * The method starts the server in a separate thread and waits for it to be fully running.
         *
         * @param port The port number on which to host the server.
         * @return A LobbyControl instance that manages the hosted server.
         * @throws IOException If an error occurs while starting the server or if the port is already in use.
         */
        public static LobbyControl host(int port) throws IOException {

            Server server = new Server(port);
            Thread thread = new Thread(server::start, "DeustoCluedo-server-01");

            thread.setDaemon(true);
            thread.start();

            long timeout = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            try {
                while (!server.isRunning()) {
                    if (!thread.isAlive()) {
                        throw new IOException("Server failed to start. Port already in use");
                    }

                    if (System.nanoTime() >= timeout) {
                        throw new IOException("Timeout while starting the server");
                    }

                    Thread.sleep(25);
                    }
                } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                server.stop();
                throw new IOException("Server interupted at startup. ", e);
            } catch (IOException e){
                server.stop();
                throw e;
            }
            return new LobbyControl(server, thread);
        }
        public boolean isRunning() {
            return server.isRunning();
        }
        public int getClientCount(){
            return server.getClientCount();
        }

    @Override
    public void close() {
            server.stop();
    }
}

