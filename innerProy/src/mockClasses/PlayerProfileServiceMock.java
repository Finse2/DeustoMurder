
package mockClasses;

import game.profile.PlayerIdentityManager;
import game.profile.PlayerProfileService;

import game.settings.SettingKey;
import game.settings.SettingsService;

import model.PlayerProfile;

import utils.JDBC.DatabaseService;
import utils.JDBC.DAO.PlayerDAO;
import utils.JDBC.DAO.StatisticsDAO;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.UUID;

public final class PlayerProfileServiceMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== PLAYER PROFILE SERVICE TEST ==="
        );

        DatabaseService database =
                DatabaseService.getInstance();

        Path testDirectory =
                Files.createTempDirectory(
                        "deustomurder-profile-test"
                );

        Path identityFile =
                testDirectory.resolve("player.id");

        Path imageFile =
                testDirectory.resolve("profile.png");

        UUID playerId = null;

        try {

            database.initialize();

            PlayerIdentityManager identityManager =
                    new PlayerIdentityManager(
                            identityFile
                    );

            // TEST 1: CREATE IDENTITY

            playerId =
                    identityManager.getOrCreateIdentity();

            check(
                    Files.isRegularFile(identityFile),
                    "Identity file not created"
            );

            pass("Permanent identity created");

            // TEST 2: REPEATED IDENTITY REQUEST

            check(
                    playerId.equals(
                            identityManager
                                    .getOrCreateIdentity()
                    ),
                    "Identity changed"
            );

            pass("Repeated identity retrieval");

            // TEST 3: NEW MANAGER

            PlayerIdentityManager secondManager =
                    new PlayerIdentityManager(
                            identityFile
                    );

            check(
                    playerId.equals(
                            secondManager
                                    .getOrCreateIdentity()
                    ),
                    "Identity not preserved"
            );

            pass("Identity survives manager recreation");

            // TEST 4: PROFILE INITIALIZATION

            PlayerProfileService profiles =
                    new PlayerProfileService(
                            database,
                            identityManager,
                            imageFile
                    );

            PlayerProfile profile =
                    profiles.initialize(
                            "Profile Test Player"
                    );

            check(
                    profile.getPlayerId().equals(playerId)
                            && profiles.isInitialized()
                            && profile.getDisplayName()
                            .equals("Profile Test Player"),
                    "Profile initialization failed"
            );

            pass("Persistent profile initialized");

            // TEST 5: STATISTICS INITIALIZATION

            check(
                    profiles.getStatistics()
                            .gamesPlayed() == 0,
                    "Initial statistics invalid"
            );

            pass("Player statistics initialized");

            // TEST 6: SETTINGS INITIALIZATION

            SettingsService settings =
                    profiles.getSettingsService();

            check(
                    settings.getInt(
                            playerId,
                            SettingKey.MASTER_VOLUME
                    ) == 100,
                    "Default volume invalid"
            );

            pass("Player settings initialized");

            // TEST 7: ACHIEVEMENT INITIALIZATION

            check(
                    !profiles.getAchievementManager()
                            .getPlayerAchievements(playerId)
                            .isEmpty(),
                    "Achievements missing"
            );

            pass("Player achievements initialized");

            // TEST 8: CHANGE DISPLAY NAME

            profiles.updateName("Renamed Player");

            check(
                    profiles.getDisplayName()
                            .equals("Renamed Player")
                            && profiles.getLocalPlayerId()
                            .equals(playerId),
                    "Renaming changed identity"
            );

            pass("Name change preserves identity");

            // TEST 9: INVALID NAME

            boolean rejected = false;

            try {

                profiles.updateName("   ");

            } catch (IllegalArgumentException e) {

                rejected = true;
            }

            check(
                    rejected,
                    "Empty name accepted"
            );

            pass("Invalid player name rejected");

            // TEST 10: SAVE SETTINGS

            settings.setInt(
                    playerId,
                    SettingKey.MASTER_VOLUME,
                    65
            );

            check(
                    settings.getInt(
                            playerId,
                            SettingKey.MASTER_VOLUME
                    ) == 65,
                    "Settings update failed"
            );

            pass("Profile settings updated");

            // TEST 11: RECORD STATISTICS

            StatisticsDAO statisticsDAO =
                    new StatisticsDAO(database);

            statisticsDAO.recordWin(
                    playerId,
                    120_000L,
                    100
            );

            profiles.getAchievementManager()
                    .synchronizeStatistics(playerId);

            check(
                    profiles.getStatistics().wins() == 1,
                    "Statistics update failed"
            );

            pass("Profile statistics updated");

            // TEST 12: REPEATED INITIALIZATION

            profiles.initialize("Wrong Default Name");

            check(
                    profiles.getDisplayName()
                            .equals("Renamed Player")
                            && profiles.getStatistics()
                            .wins() == 1,
                    "Initialization overwrote profile"
            );

            pass("Repeated initialization preserves data");

            // TEST 13: SERVER RESTART

            database.shutdown();

            database.initialize();

            PlayerProfileService restartedProfiles =
                    new PlayerProfileService(
                            database,
                            new PlayerIdentityManager(
                                    identityFile
                            ),
                            imageFile
                    );

            restartedProfiles.initialize(
                    "Another Default Name"
            );

            check(
                    restartedProfiles.getLocalPlayerId()
                            .equals(playerId)
                            && restartedProfiles.getDisplayName()
                            .equals("Renamed Player"),
                    "Profile persistence failed"
            );

            pass("Identity and profile survive restart");

            // TEST 14: STATISTICS PERSISTENCE

            check(
                    restartedProfiles.getStatistics()
                            .wins() == 1
                            && restartedProfiles.getStatistics()
                            .totalPoints() == 100,
                    "Statistics lost after restart"
            );

            pass("Statistics survive restart");

            // TEST 15: SETTINGS PERSISTENCE

            check(
                    restartedProfiles.getSettingsService()
                            .getInt(
                                    playerId,
                                    SettingKey.MASTER_VOLUME
                            ) == 65,
                    "Settings lost after restart"
            );

            pass("Settings survive restart");

            // TEST 16: ACHIEVEMENT PERSISTENCE

            boolean unlocked =
                    restartedProfiles
                            .getAchievementManager()
                            .getPlayerAchievements(playerId)
                            .stream()
                            .anyMatch(a ->
                                    a.definition().code()
                                            .equals("WIN_GAMES")
                                            && a.level()
                                            .equals("BRONZE")
                            );

            check(
                    unlocked,
                    "Achievement lost after restart"
            );

            pass("Achievements survive restart");

            // TEST 17: CORRUPTED IDENTITY

            Path corruptedFile =
                    testDirectory.resolve(
                            "corrupted.id"
                    );

            Files.writeString(
                    corruptedFile,
                    "invalid-uuid"
            );

            rejected = false;

            try {

                new PlayerIdentityManager(
                        corruptedFile
                ).getOrCreateIdentity();

            } catch (IOException e) {

                rejected = true;
            }

            check(
                    rejected
                            && Files.readString(corruptedFile)
                            .equals("invalid-uuid"),
                    "Corrupted identity overwritten"
            );

            pass("Corrupted identity safely rejected");

            Files.deleteIfExists(corruptedFile);

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            try {

                if (database.isInitialized()
                        && playerId != null) {

                    final UUID cleanupId = playerId;

                    database.execute(db -> {

                        db.executeUpdate("""
                                DELETE FROM player_settings
                                WHERE player_id = ?
                                """,
                                cleanupId.toString()
                        );

                        db.executeUpdate("""
                                DELETE FROM player_achievements
                                WHERE player_id = ?
                                """,
                                cleanupId.toString()
                        );

                        db.executeUpdate("""
                                DELETE FROM player_statistics
                                WHERE player_id = ?
                                """,
                                cleanupId.toString()
                        );

                        return null;
                    });

                    PlayerDAO playerDAO =
                            new PlayerDAO(database);

                    if (playerDAO.playerExists(cleanupId)) {

                        playerDAO.deletePlayer(cleanupId);
                    }
                }

            } finally {

                database.shutdown();

                Files.deleteIfExists(identityFile);

                Files.deleteIfExists(
                        testDirectory.resolve(
                                "corrupted.id"
                        )
                );

                Files.deleteIfExists(imageFile);

                Files.deleteIfExists(testDirectory);
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

        System.out.println(
                "[PASS] " + message
        );
    }
}
