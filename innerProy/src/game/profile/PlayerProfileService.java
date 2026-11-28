
package game.profile;

import achievements.AchievementManager;

import game.settings.SettingsService;
import game.statistics.StatisticsSnapshot;

import model.PlayerProfile;

import utils.JDBC.DatabaseService;
import utils.JDBC.DAO.PlayerDAO;
import utils.JDBC.DAO.StatisticsDAO;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Objects;
import java.util.UUID;

public final class PlayerProfileService {

    private final PlayerIdentityManager identityManager;
    private final PlayerDAO playerDAO;
    private final StatisticsDAO statisticsDAO;
    private final AchievementManager achievementManager;
    private final SettingsService settingsService;

    private final Path profileImagePath;

    private UUID localPlayerId;

    private boolean initialized;

    // CONSTRUCTORS

    public PlayerProfileService() {

        this(
                DatabaseService.getInstance(),
                new PlayerIdentityManager()
        );
    }

    public PlayerProfileService(
            DatabaseService database,
            PlayerIdentityManager identityManager
    ) {

        this(
                database,
                identityManager,
                Path.of(
                        System.getProperty("user.home"),
                        ".deustoCluedo",
                        "profile.png"
                )
        );
    }

    public PlayerProfileService(
            DatabaseService database,
            PlayerIdentityManager identityManager,
            Path profileImagePath
    ) {

        Objects.requireNonNull(database);

        this.identityManager =
                Objects.requireNonNull(identityManager);

        this.profileImagePath =
                Objects.requireNonNull(profileImagePath);

        this.playerDAO =
                new PlayerDAO(database);

        this.statisticsDAO =
                new StatisticsDAO(database);

        this.achievementManager =
                new AchievementManager(database);

        this.settingsService =
                new SettingsService(database);
    }

    // INITIALIZE LOCAL PLAYER

    public synchronized PlayerProfile initialize(
            String defaultName
    ) throws Exception {

        if (initialized) {

            return getProfile();
        }

        localPlayerId =
                identityManager.getOrCreateIdentity();

        PlayerProfile profile =
                playerDAO.getPlayer(localPlayerId)
                        .orElse(null);

        if (profile == null) {

            profile = playerDAO.getOrCreatePlayer(
                    localPlayerId,
                    defaultName
            );
        }

        // Import an existing profile picture.

        if (!profile.hasProfileImage()
                && Files.isRegularFile(profileImagePath)) {

            playerDAO.updateProfileImage(
                    localPlayerId,
                    profileImagePath.toAbsolutePath()
                            .toString()
            );
        }

        // INITIALIZE STATISTICS

        statisticsDAO.getOrCreateStatistics(
                localPlayerId
        );

        // INITIALIZE ACHIEVEMENTS

        achievementManager.initializeAchievements();

        achievementManager.synchronizeStatistics(
                localPlayerId
        );

        // INITIALIZE SETTINGS

        settingsService.initialize(
                localPlayerId
        );

        // UPDATE LAST SEEN

        playerDAO.updateLastSeen(
                localPlayerId
        );

        initialized = true;

        return getProfile();
    }

    // GET LOCAL PROFILE

    public synchronized PlayerProfile getProfile()
            throws Exception {

        requireInitialized();

        return playerDAO.getPlayer(localPlayerId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Local profile does not exist."
                        )
                );
    }

    // GET PERMANENT PLAYER ID

    public synchronized UUID getLocalPlayerId() {

        requireInitialized();

        return localPlayerId;
    }

    // GET DISPLAY NAME

    public String getDisplayName()
            throws Exception {

        return getProfile().getDisplayName();
    }

    // CHANGE DISPLAY NAME

    public synchronized void updateName(
            String newName
    ) throws Exception {

        requireInitialized();

        Objects.requireNonNull(newName);

        String normalized = newName.trim();

        if (normalized.isEmpty()
                || normalized.length() > 64) {

            throw new IllegalArgumentException(
                    "Player name must contain "
                            + "between 1 and 64 characters."
            );
        }

        if (!playerDAO.updateName(
                localPlayerId,
                normalized
        )) {

            throw new IllegalStateException(
                    "Could not update player name."
            );
        }
    }

    // UPDATE PROFILE IMAGE REFERENCE

    public synchronized void updateProfileImage(
            Path image
    ) throws Exception {

        requireInitialized();

        Objects.requireNonNull(image);

        if (!Files.isRegularFile(image)) {

            throw new IllegalArgumentException(
                    "Profile image does not exist."
            );
        }

        if (!playerDAO.updateProfileImage(
                localPlayerId,
                image.toAbsolutePath().toString()
        )) {

            throw new IllegalStateException(
                    "Could not update profile image."
            );
        }
    }

    // REMOVE PROFILE IMAGE REFERENCE

    public synchronized void removeProfileImage()
            throws Exception {

        requireInitialized();

        playerDAO.removeProfileImage(
                localPlayerId
        );
    }

    // GET LOCAL STATISTICS

    public synchronized StatisticsSnapshot getStatistics()
            throws Exception {

        requireInitialized();

        return statisticsDAO.getOrCreateStatistics(
                localPlayerId
        );
    }

    // GET SERVICES

    public SettingsService getSettingsService() {

        return settingsService;
    }

    public AchievementManager getAchievementManager() {

        return achievementManager;
    }

    public synchronized boolean isInitialized() {

        return initialized;
    }

    // STATE VALIDATION

    private void requireInitialized() {

        if (!initialized) {

            throw new IllegalStateException(
                    "Player profile service "
                            + "has not been initialized."
            );
        }
    }
}
