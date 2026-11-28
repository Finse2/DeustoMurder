
package mockClasses;

import Networking.Client;
import Networking.server.GameServer;

import java.net.ServerSocket;

import java.util.UUID;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class MultiplayerIdentityMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== MULTIPLAYER IDENTITY TEST ==="
        );

        int port;

        try (ServerSocket socket = new ServerSocket(0)) {

            port = socket.getLocalPort();
        }

        GameServer server = new GameServer(port);

        Thread serverThread = new Thread(
                server::start,
                "identity-test-server"
        );

        serverThread.setDaemon(true);

        serverThread.start();

        UUID identity1 = UUID.randomUUID();

        UUID identity2 = UUID.randomUUID();

        CompletableFuture<Integer> accepted1 =
                new CompletableFuture<>();

        CompletableFuture<Integer> accepted2 =
                new CompletableFuture<>();

        CompletableFuture<UUID> joinedIdentity =
                new CompletableFuture<>();

        CompletableFuture<Void> rejected =
                new CompletableFuture<>();

        // FIRST PLAYER

        Client player1 = new Client(

                "127.0.0.1",
                port,

                new Client.Listener() {

                    @Override
                    public void onConnected(int playerID) {

                        accepted1.complete(playerID);
                    }

                    @Override
                    public void onPlayerJoined(
                            int playerID,
                            String playerName,
                            String computerName,
                            UUID profileId
                    ) {

                        joinedIdentity.complete(profileId);
                    }
                }
        );

        // SECOND PLAYER

        Client player2 = new Client(

                "127.0.0.1",
                port,

                new Client.Listener() {

                    @Override
                    public void onConnected(int playerID) {

                        accepted2.complete(playerID);
                    }
                }
        );

        // DUPLICATE PLAYER

        Client duplicate = new Client(

                "127.0.0.1",
                port,

                new Client.Listener() {

                    @Override
                    public void onDisconnected() {

                        rejected.complete(null);
                    }

                    @Override
                    public void onError(Exception exception) {

                        // Connection rejection is expected.
                    }
                }
        );

        try {

            // TEST 1: SERVER STARTUP

            long deadline =
                    System.nanoTime()
                            + TimeUnit.SECONDS.toNanos(5);

            while (!server.isRunning()) {

                if (System.nanoTime() >= deadline) {

                    throw new IllegalStateException(
                            "Server startup timed out."
                    );
                }

                Thread.sleep(25);
            }

            pass("Server startup");

            // TEST 2: FIRST PLAYER IDENTITY

            player1.connect(

                    "Detective One",
                    "Test-PC-1",
                    identity1
            );

            int session1 = accepted1.get(
                    5,
                    TimeUnit.SECONDS
            );

            check(

                    server.getProfileId(session1)
                            .orElseThrow()
                            .equals(identity1),

                    "Incorrect first profile ID"
            );

            pass("First player identity registered");

            // TEST 3: SECOND PLAYER IDENTITY

            player2.connect(

                    "Detective Two",
                    "Test-PC-2",
                    identity2
            );

            int session2 = accepted2.get(
                    5,
                    TimeUnit.SECONDS
            );

            check(

                    server.getProfileId(session2)
                            .orElseThrow()
                            .equals(identity2),

                    "Incorrect second profile ID"
            );

            pass("Second player identity registered");

            // TEST 4: SESSION ID ISOLATION

            check(

                    session1 != session2,

                    "Duplicate session IDs"
            );

            pass("Independent session IDs");

            // TEST 5: PLAYER JOIN NOTIFICATION

            check(

                    joinedIdentity.get(
                            5,
                            TimeUnit.SECONDS
                    ).equals(identity2),

                    "Profile ID missing from notification"
            );

            pass("Player joined UUID transmitted");

            // TEST 6: SERVER PROFILE MAP

            check(

                    server.getConnectedProfiles()
                            .size() == 2,

                    "Incorrect profile count"
            );

            pass("Server tracks connected profiles");

            // TEST 7: LOCAL CLIENT UUID

            check(

                    identity1.equals(player1.getProfileId())

                            && identity2.equals(player2.getProfileId()),

                    "Client UUID does not match profile"
            );

            pass("Clients preserve their profile UUIDs");

            // TEST 8: DUPLICATE PROFILE REJECTION

            duplicate.connect(

                    "Different Name",
                    "Test-PC-3",

                    // Intentionally reuse player one's UUID.
                    identity1
            );

            rejected.get(
                    5,
                    TimeUnit.SECONDS
            );

            check(

                    !duplicate.isConnected()

                            && server.getConnectedProfiles()
                            .size() == 2,

                    "Duplicate identity accepted"
            );

            pass("Duplicate profile connection rejected");

            // TEST 9: ORIGINAL CONNECTION UNAFFECTED

            check(

                    player1.isConnected()

                            && server.getProfileId(session1)
                            .orElseThrow()
                            .equals(identity1),

                    "Original connection affected"
            );

            pass("Original connection preserved");

            // TEST 10: PROFILE MAP CONTENTS

            var profiles =
                    server.getConnectedProfiles();

            check(

                    identity1.equals(profiles.get(session1))

                            && identity2.equals(profiles.get(session2)),

                    "Incorrect UUID mapping"
            );

            pass("Session-to-profile mapping verified");

            // TEST 11: UNKNOWN PLAYER LOOKUP

            check(

                    server.getProfileId(-1).isEmpty(),

                    "Unknown player returned a profile"
            );

            pass("Unknown player lookup handled");

            // TEST 12: DISCONNECT CLEANUP

            player2.disconnect();

            long disconnectDeadline =
                    System.nanoTime()
                            + TimeUnit.SECONDS.toNanos(5);

            while (server.getProfileId(session2).isPresent()) {

                if (System.nanoTime() >= disconnectDeadline) {

                    throw new AssertionError(
                            "Disconnected profile was not removed."
                    );
                }

                Thread.sleep(25);
            }

            check(

                    server.getConnectedProfiles()
                            .size() == 1,

                    "Incorrect remaining player count"
            );

            pass("Disconnected profile removed");

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            duplicate.disconnect();

            player2.disconnect();

            player1.disconnect();

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
