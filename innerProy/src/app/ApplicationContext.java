
package app;

import achievements.AchievementManager;

import game.profile.PlayerIdentityManager;
import game.profile.PlayerProfileService;

import game.settings.SettingsService;
import game.statistics.PlayerStatisticsService;
import game.statistics.LegacyStatisticsMigrator;

import model.PlayerProfile;

import utils.JDBC.DatabaseService;
import utils.JDBC.DAO.MatchDAO;

import java.util.UUID;

public final class ApplicationContext
        implements AutoCloseable {

    private static final ApplicationContext INSTANCE =
            new ApplicationContext();

    private final DatabaseService database;

    private final PlayerProfileService profileService;

    private final PlayerStatisticsService statisticsService;

    private boolean initialized;

    private ApplicationContext() {

        this.database =
                DatabaseService.getInstance();

        this.profileService =
                new PlayerProfileService(
                        database,
                        new PlayerIdentityManager()
                );

        this.statisticsService =
                new PlayerStatisticsService(database);
    }

    public static ApplicationContext getInstance() {

        return INSTANCE;
    }

    // INITIALIZE APPLICATION

    public synchronized void initialize()
            throws Exception {

        if (initialized) {
            return;
        }

        try {

            database.initialize();

            profileService.initialize("Player");

            statisticsService.initialize();

            LegacyStatisticsMigrator migrator =
                    new LegacyStatisticsMigrator(database);

            migrator.migrate(
                    profileService.getLocalPlayerId()
            );

            initialized = true;

        } catch (Exception e) {

            database.shutdown();

            throw e;
        }
    }

    // GET DATABASE

    public synchronized DatabaseService getDatabase() {

        requireInitialized();

        return database;
    }

    // GET PROFILE SERVICE

    public synchronized PlayerProfileService
    getProfileService() {

        requireInitialized();

        return profileService;
    }

    // GET LOCAL PLAYER

    public synchronized PlayerProfile getLocalProfile()
            throws Exception {

        requireInitialized();

        return profileService.getProfile();
    }

    public synchronized UUID getLocalPlayerId() {

        requireInitialized();

        return profileService.getLocalPlayerId();
    }

    // GET STATISTICS SERVICE

    public synchronized PlayerStatisticsService
    getStatisticsService() {

        requireInitialized();

        return statisticsService;
    }

    // GET ACHIEVEMENT MANAGER

    public synchronized AchievementManager
    getAchievementManager() {

        requireInitialized();

        return statisticsService.getAchievementManager();
    }

    // GET SETTINGS SERVICE

    public synchronized SettingsService
    getSettingsService() {

        requireInitialized();

        return profileService.getSettingsService();
    }

    // GET MATCH DAO

    public synchronized MatchDAO getMatchDAO() {

        requireInitialized();

        return statisticsService.getMatchDAO();
    }

    // STATE

    public synchronized boolean isInitialized() {

        return initialized;
    }

    private void requireInitialized() {

        if (!initialized) {

            throw new IllegalStateException(
                    "Application context "
                            + "has not been initialized."
            );
        }
    }

    // SHUTDOWN

    public synchronized void shutdown() {

        initialized = false;

        database.shutdown();
    }

    @Override
    public void close() {

        shutdown();
    }
}
