
package mockClasses;

import Networking.Client;
import Networking.server.GameServer;

import Networking.match.NetworkMatchResult;
import Networking.packets.GameStartPacket;

import java.net.ServerSocket;
import java.util.UUID;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class NetworkMatchIntegrationMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== NETWORK MATCH INTEGRATION TEST ==="
        );

        UUID hostId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();

        // Automatically select an available port.

        int port;

        try (ServerSocket socket = new ServerSocket(0)) {

            port = socket.getLocalPort();
        }

        GameServer server =
                new GameServer(port, hostId);

        Thread serverThread = new Thread(
                server::start,
                "match-integration-server"
        );

        serverThread.setDaemon(true);

        // CONNECTION FUTURES

        CompletableFuture<Integer> hostConnected =
                new CompletableFuture<>();

        CompletableFuture<Integer> guestConnected =
                new CompletableFuture<>();

        // MATCH START FUTURES

        CompletableFuture<GameStartPacket> hostStarted =
                new CompletableFuture<>();

        CompletableFuture<GameStartPacket> guestStarted =
                new CompletableFuture<>();

        // MATCH RESULT FUTURES

        CompletableFuture<NetworkMatchResult> hostResult =
                new CompletableFuture<>();

        CompletableFuture<NetworkMatchResult> guestResult =
                new CompletableFuture<>();

        // AUTHORIZATION FUTURES

        CompletableFuture<Void> unauthorizedStart =
                new CompletableFuture<>();

        CompletableFuture<Void> unauthorizedFinish =
                new CompletableFuture<>();

        // CREATE HOST CLIENT

        Client host = new Client(

                "127.0.0.1",
                port,

                new Client.Listener() {

                    @Override
                    public void onConnected(int playerID) {

                        hostConnected.complete(playerID);
                    }

                    @Override
                    public void onGameStarted(
                            GameStartPacket packet
                    ) {

                        hostStarted.complete(packet);
                    }

                    @Override
                    public void onMatchCompleted(
                            NetworkMatchResult result
                    ) {

                        hostResult.complete(result);
                    }
                }
        );

        // CREATE GUEST CLIENT

        Client guest = new Client(

                "127.0.0.1",
                port,

                new Client.Listener() {

                    @Override
                    public void onConnected(int playerID) {

                        guestConnected.complete(playerID);
                    }

                    @Override
                    public void onGameStarted(
                            GameStartPacket packet
                    ) {

                        guestStarted.complete(packet);
                    }

                    @Override
                    public void onMatchCompleted(
                            NetworkMatchResult result
                    ) {

                        guestResult.complete(result);
                    }
                }
        );

        // LISTEN FOR AUTHORIZATION ERRORS

        guest.addChatPacketListener(packet -> {

            if ("Only the host can start the game."
                    .equals(packet.getMessage())) {

                unauthorizedStart.complete(null);
            }

            if ("Only the host can finalize the match."
                    .equals(packet.getMessage())) {

                unauthorizedFinish.complete(null);
            }
        });

        serverThread.start();

        try {

            // TEST 1: SERVER STARTUP

            long deadline =
                    System.nanoTime()
                            + TimeUnit.SECONDS.toNanos(5);

            while (!server.isRunning()) {

                if (System.nanoTime() >= deadline) {

                    throw new AssertionError(
                            "Server startup timed out."
                    );
                }

                Thread.sleep(25);
            }

            pass("Server startup");

            // TEST 2: CONNECT BOTH PLAYERS

            host.connect(
                    "Host Detective",
                    "Host-PC",
                    hostId
            );

            guest.connect(
                    "Guest Detective",
                    "Guest-PC",
                    guestId
            );

            int hostSessionId =
                    hostConnected.get(
                            5,
                            TimeUnit.SECONDS
                    );

            int guestSessionId =
                    guestConnected.get(
                            5,
                            TimeUnit.SECONDS
                    );

            check(

                    hostSessionId != guestSessionId

                            && server.getConnectedProfiles()
                            .size() == 2,

                    "Participants were not registered."
            );

            pass("Both participants connected");

            // TEST 3: UNAUTHORIZED MATCH START

            guest.startGame();

            unauthorizedStart.get(
                    5,
                    TimeUnit.SECONDS
            );

            check(

                    !hostStarted.isDone()

                            && !guestStarted.isDone(),

                    "Guest started an unauthorized match."
            );

            pass("Unauthorized match start rejected");

            // TEST 4: HOST STARTS MATCH

            host.startGame();

            GameStartPacket first =
                    hostStarted.get(
                            5,
                            TimeUnit.SECONDS
                    );

            GameStartPacket second =
                    guestStarted.get(
                            5,
                            TimeUnit.SECONDS
                    );

            check(

                    first.isConfirmed()

                            && second.isConfirmed(),

                    "Match start was not confirmed."
            );

            pass("Host successfully starts match");

            // TEST 5: SHARED MATCH UUID

            check(

                    first.getMatchId()
                            .equals(second.getMatchId())

                            && first.getMatchId()
                            .equals(host.getCurrentMatchId())

                            && first.getMatchId()
                            .equals(guest.getCurrentMatchId()),

                    "Clients received different match IDs."
            );

            pass("Both clients received identical match UUID");

            System.out.println(
                    "Match UUID: " + first.getMatchId()
            );

            // TEST 6: HOST IDENTITY

            check(

                    hostId.equals(first.getHostId())

                            && hostId.equals(second.getHostId()),

                    "Invalid host identity."
            );

            pass("Correct host identity transmitted");

            // TEST 7: UNAUTHORIZED COMPLETION

            guest.endGame(

                    guestSessionId,

                    "Guest Detective",

                    "Character A",

                    "Weapon A",

                    "Room A"
            );

            unauthorizedFinish.get(
                    5,
                    TimeUnit.SECONDS
            );

            check(

                    server.getCompletedMatch().isEmpty()

                            && !hostResult.isDone()

                            && !guestResult.isDone(),

                    "Guest finalized the match."
            );

            pass("Unauthorized completion rejected");

            // TEST 8: HOST COMPLETES MATCH

            // The host declares the guest as the winner.

            host.endGame(

                    guestSessionId,

                    "Guest Detective",

                    "Character A",

                    "Weapon A",

                    "Room A"
            );

            NetworkMatchResult result1 =
                    hostResult.get(
                            5,
                            TimeUnit.SECONDS
                    );

            NetworkMatchResult result2 =
                    guestResult.get(
                            5,
                            TimeUnit.SECONDS
                    );

            check(

                    result1.winnerId()
                            .equals(guestId),

                    "Incorrect winner."
            );

            pass("Host successfully completes match");

            // TEST 9: IDENTICAL FINAL RESULTS

            check(

                    result1.equals(result2)

                            && result1.equals(

                            server.getCompletedMatch()
                                    .orElseThrow()
                    ),

                    "Completed results differ."
            );

            pass("Identical final results received");

            // TEST 10: PARTICIPANT OUTCOMES

            check(

                    result1.getPlayer(hostId)

                            .orElseThrow()

                            .hasLost()

                            && result1.getPlayer(guestId)

                            .orElseThrow()

                            .hasWon(),

                    "Incorrect participant outcomes."
            );

            pass("Winner and loser correctly assigned");

            // TEST 11: MATCH UUID PRESERVED

            check(

                    result1.matchId()
                            .equals(first.getMatchId()),

                    "Match ID changed during completion."
            );

            pass("Match UUID preserved throughout lifecycle");

            // TEST 12: FINAL SOLUTION

            check(

                    "Character A".equals(result1.murderer())

                            && "Weapon A".equals(result1.weapon())

                            && "Room A".equals(result1.room()),

                    "Murder solution was not preserved."
            );

            pass("Final murder solution transmitted");

            // PRINT FINAL MATCH INFORMATION

            System.out.println();
            System.out.println("=== FINAL MATCH RESULT ===");

            System.out.println(
                    "Match ID: " + result1.matchId()
            );

            System.out.println(
                    "Host ID: " + result1.hostId()
            );

            System.out.println(
                    "Winner ID: " + result1.winnerId()
            );

            System.out.println(
                    "Winner: " + result1.getWinnerName()
            );

            System.out.println(
                    "Duration: "
                            + result1.durationMillis()
                            + " ms"
            );

            System.out.println(
                    "Participants: "
                            + result1.getPlayerCount()
            );

            System.out.println(
                    "Murderer: " + result1.murderer()
            );

            System.out.println(
                    "Weapon: " + result1.weapon()
            );

            System.out.println(
                    "Room: " + result1.room()
            );

            System.out.println();

            for (var player : result1.players()) {

                System.out.println(

                        player.displayName()

                                + " | "

                                + player.profileId()

                                + " | "

                                + player.result()
                );
            }

            System.out.println();
            System.out.println("All assertions passed.");

        } finally {

            guest.disconnect();

            host.disconnect();

            server.stop();

            serverThread.join(5000);
        }
    }

    private static void check(
            boolean condition,
            String message
    ) {

        if (!condition) {

            throw new AssertionError(message);
        }
    }

    private static void pass(String message) {

        System.out.println(
                "[PASS] " + message
        );
    }
}
