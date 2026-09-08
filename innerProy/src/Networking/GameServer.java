package Networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {

    private ServerSocket serverSocket;

    public void start(int port) {

        try {
            serverSocket = new ServerSocket(port);

            System.out.println("Server started on port " + port);

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println(
                        "Player connected: "
                                + clientSocket.getInetAddress()
                );

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}