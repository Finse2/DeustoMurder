
package game.statistics;

import achievements.AchievementManager;

import utils.JDBC.DatabaseService;
import utils.JDBC.DAO.StatisticsDAO;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class PlayerStatisticsService {

    private final StatisticsDAO statisticsDAO;

    private final AchievementManager achievementManager;

    // CONSTRUCTORS

    public PlayerStatisticsService() {

        this(DatabaseService.getInstance());
    }

    public PlayerStatisticsService(
            DatabaseService database
    ) {

        Objects.requireNonNull(database);

        this.statisticsDAO =
                new StatisticsDAO(database);

        this.achievementManager =
                new AchievementManager(database);
    }

    // INITIALIZATION

    public void initialize()
            throws Exception {

        achievementManager.initializeAchievements();
    }

    // RECORD WIN

    public List<AchievementManager.AchievementUnlock>
    recordWin(

            UUID playerId,

            long durationMillis,

            long points

    ) throws Exception {

        statisticsDAO.recordWin(

                playerId,

                durationMillis,

                points
        );

        return achievementManager.synchronizeStatistics(
                playerId
        );
    }

    // RECORD LOSS

    public List<AchievementManager.AchievementUnlock>
    recordLoss(

            UUID playerId,

            long durationMillis,

            long points

    ) throws Exception {

        statisticsDAO.recordLoss(

                playerId,

                durationMillis,

                points
        );

        return achievementManager.synchronizeStatistics(
                playerId
        );
    }

    // RECORD DRAW

    public List<AchievementManager.AchievementUnlock>
    recordDraw(

            UUID playerId,

            long durationMillis,

            long points

    ) throws Exception {

        statisticsDAO.recordDraw(

                playerId,

                durationMillis,

                points
        );

        return achievementManager.synchronizeStatistics(
                playerId
        );
    }

    // RECORD ACCUSATION

    public List<AchievementManager.AchievementUnlock>
    recordAccusation(

            UUID playerId,

            boolean correct

    ) throws Exception {

        statisticsDAO.recordAccusation(

                playerId,

                correct
        );

        return achievementManager.synchronizeStatistics(
                playerId
        );
    }

    // GET STATISTICS

    public StatisticsSnapshot getStatistics(
            UUID playerId
    ) throws Exception {

        return statisticsDAO.getOrCreateStatistics(
                playerId
        );
    }

    // RECOVER MISSED ACHIEVEMENTS

    public List<AchievementManager.AchievementUnlock>
    synchronizeAchievements(UUID playerId)
            throws Exception {

        return achievementManager.synchronizeStatistics(
                playerId
        );
    }

    public AchievementManager getAchievementManager() {

        return achievementManager;
    }
}
