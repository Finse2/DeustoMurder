
package mockClasses;

import model.PlayerProfile;
import utils.JDBC.DatabaseService;
import utils.JDBC.DAO.PlayerDAO;

import java.util.List;
import java.util.UUID;

public class PlayerDAOMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== PLAYER DAO TEST ==="
        );

        DatabaseService service =
                DatabaseService.getInstance();

        UUID playerId = UUID.randomUUID();

        String testName =
                "JDBC O'Connor_" + playerId;

        PlayerDAO dao = new PlayerDAO(service);

        try {

            service.initialize();

            long initialCount = dao.getPlayerCount();

            // TEST 1: CREATE PLAYER

            PlayerProfile created =
                    dao.createPlayer(
                            playerId,
                            testName
                    );

            check(
                    created.getPlayerId().equals(playerId),
                    "Player UUID"
            );

            check(
                    created.getDisplayName().equals(testName),
                    "Player creation"
            );

            pass("Player creation");

            // TEST 2: PLAYER EXISTS

            check(
                    dao.playerExists(playerId),
                    "Player existence"
            );

            pass("Player existence");

            // TEST 3: RETRIEVE PLAYER

            PlayerProfile retrieved =
                    dao.getPlayer(playerId)
                            .orElseThrow();

            check(
                    retrieved.getDisplayName().equals(testName),
                    "Player retrieval"
            );

            pass("Player retrieval");

            // TEST 4: GET OR CREATE

            PlayerProfile existing =
                    dao.getOrCreatePlayer(
                            playerId,
                            "Different Name"
                    );

            check(
                    existing.getPlayerId().equals(playerId),
                    "Persistent identity"
            );

            check(
                    existing.getDisplayName().equals(testName),
                    "Existing name preservation"
            );

            check(
                    dao.getPlayerCount() == initialCount + 1,
                    "Duplicate player prevention"
            );

            pass("Get or create");

            // TEST 5: UPDATE NAME

            String updatedName =
                    "Updated Player " + playerId;

            check(
                    dao.updateName(playerId, updatedName),
                    "Name update result"
            );

            check(
                    dao.getPlayer(playerId)
                            .orElseThrow()
                            .getDisplayName()
                            .equals(updatedName),

                    "Name update persistence"
            );

            pass("Name update");

            // TEST 6: PROFILE IMAGE

            check(
                    dao.updateProfileImage(
                            playerId,
                            "profiles/test.png"
                    ),

                    "Profile image update"
            );

            PlayerProfile withImage =
                    dao.getPlayer(playerId)
                            .orElseThrow();

            check(
                    withImage.hasProfileImage(),
                    "Profile image presence"
            );

            check(
                    "profiles/test.png".equals(
                            withImage.getProfileImage()
                    ),

                    "Profile image persistence"
            );

            pass("Profile image update");

            // TEST 7: REMOVE PROFILE IMAGE

            check(
                    dao.removeProfileImage(playerId),
                    "Profile image removal"
            );

            check(
                    !dao.getPlayer(playerId)
                            .orElseThrow()
                            .hasProfileImage(),

                    "Profile image removal persistence"
            );

            pass("Profile image removal");

            // TEST 8: LAST SEEN

            long previousLastSeen =
                    dao.getPlayer(playerId)
                            .orElseThrow()
                            .getLastSeen();

            Thread.sleep(25);

            check(
                    dao.updateLastSeen(playerId),
                    "Last seen update"
            );

            check(
                    dao.getPlayer(playerId)
                            .orElseThrow()
                            .getLastSeen() > previousLastSeen,

                    "Last seen persistence"
            );

            pass("Last seen update");

            // TEST 9: SEARCH PLAYERS

            List<PlayerProfile> results =
                    dao.searchPlayers(updatedName);

            check(
                    results.stream().anyMatch(
                            player -> player.getPlayerId()
                                    .equals(playerId)
                    ),

                    "Player search"
            );

            pass("Player search");

            // TEST 10: GET ALL PLAYERS

            check(
                    dao.getAllPlayers()
                            .stream()
                            .anyMatch(
                                    player ->
                                            player.getPlayerId()
                                                    .equals(playerId)
                            ),

                    "Get all players"
            );

            pass("Get all players");

            // TEST 11: COUNT PLAYERS

            check(
                    dao.getPlayerCount() == initialCount + 1,
                    "Player count"
            );

            pass("Player count");

            // TEST 12: INVALID NAME

            boolean rejected = false;

            try {

                dao.updateName(playerId, "   ");

            } catch (IllegalArgumentException e) {

                rejected = true;
            }

            check(
                    rejected,
                    "Empty name validation"
            );

            pass("Invalid name rejected");

            // TEST 13: DELETE PLAYER

            check(
                    dao.deletePlayer(playerId),
                    "Player deletion"
            );

            check(
                    !dao.playerExists(playerId),
                    "Player removal"
            );

            check(
                    dao.getPlayer(playerId).isEmpty(),
                    "Deleted player retrieval"
            );

            check(
                    dao.getPlayerCount() == initialCount,
                    "Player count after deletion"
            );

            pass("Player deletion");

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            try {

                if (service.isInitialized()
                        && dao.playerExists(playerId)) {

                    dao.deletePlayer(playerId);
                }

            } finally {

                service.shutdown();
            }
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

        System.out.println("[PASS] " + message);
    }
}
