package activation.Screen.lobby;
import Networking.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class LobbyControl implements AutoCloseable {

        private final Server server;
        private final Thread serverThread;

        private LobbyControl(Server server, Thread serverThread) {
            this.server = server;
            this.serverThread = serverThread;
        }

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

