package game.statistics;

import achievements.AchievementManager;

import utils.JDBC.DatabaseService;
import utils.JDBC.DAO.StatisticsDAO;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import java.util.logging.Logger;

public final class LegacyStatisticsMigrator {

    private static final Logger LOGGER =
            Logger.getLogger(
                    LegacyStatisticsMigrator.class.getName()
            );

    private static final Path DEFAULT_PATH = Path.of(

            System.getProperty("user.home"),

            ".deustoCluedo",

            "statistics.properties"
    );

    public enum MigrationResult {

        IMPORTED,

        ALREADY_IMPORTED,

        NO_LEGACY_FILE
    }

    private final StatisticsDAO statisticsDAO;

    private final AchievementManager achievementManager;

    private final Path legacyFile;

    // CONSTRUCTORS

    public LegacyStatisticsMigrator() {

        this(
                DatabaseService.getInstance(),
                DEFAULT_PATH
        );
    }

    public LegacyStatisticsMigrator(
            DatabaseService database
    ) {

        this(database, DEFAULT_PATH);
    }

    public LegacyStatisticsMigrator(

            DatabaseService database,

            Path legacyFile

    ) {

        Objects.requireNonNull(database);

        this.legacyFile =
                Objects.requireNonNull(legacyFile);

        this.statisticsDAO =
                new StatisticsDAO(database);

        this.achievementManager =
                new AchievementManager(database);
    }

    // MIGRATE EXISTING STATISTICS

    public MigrationResult migrate(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        if (statisticsDAO.hasImportedLegacyStatistics(
                playerId
        )) {

            achievementManager.initializeAchievements();

            achievementManager.synchronizeStatistics(
                    playerId
            );

            return MigrationResult.ALREADY_IMPORTED;
        }

        if (Files.notExists(legacyFile)) {

            LOGGER.info(
                    "No legacy statistics file found."
            );

            return MigrationResult.NO_LEGACY_FILE;
        }

        if (!Files.isRegularFile(legacyFile)) {

            throw new IOException(
                    "Legacy statistics path is not a file."
            );
        }

        LegacyStatisticsSnapshot legacy =
                readLegacyStatistics();

        boolean imported =
                statisticsDAO.importLegacyStatistics(

                        playerId,

                        legacy
                );

        // Achievement synchronization is repeatable.
        // If it fails, the statistics will not be
        // imported again.

        achievementManager.initializeAchievements();

        achievementManager.synchronizeStatistics(
                playerId
        );

        if (imported) {

            LOGGER.info(
                    "Legacy statistics imported successfully."
            );

            return MigrationResult.IMPORTED;
        }

        return MigrationResult.ALREADY_IMPORTED;
    }

    // READ LEGACY FILE

    public LegacyStatisticsSnapshot readLegacyStatistics()
            throws IOException {

        Properties properties = new Properties();

        try (InputStream input =
                     Files.newInputStream(legacyFile)) {

            properties.load(input);
        }

        try {

            return new LegacyStatisticsSnapshot(

                    properties.getProperty(
                            "playerName",
                            "Player"
                    ),

                    readLong(
                            properties,
                            "gamesPlayed"
                    ),

                    readLong(
                            properties,
                            "wins"
                    ),

                    readLong(
                            properties,
                            "losses"
                    ),

                    readLong(
                            properties,
                            "accusationsMade"
                    ),

                    readLong(
                            properties,
                            "correctAccusations"
                    ),

                    readLong(
                            properties,
                            "incorrectAccusations"
                    ),

                    readLong(
                            properties,
                            "totalGameDurationMillis"
                    )
            );

        } catch (IllegalArgumentException e) {

            throw new IOException(

                    "Invalid legacy statistics file: "

                            + legacyFile,

                    e
            );
        }
    }

    private static long readLong(

            Properties properties,

            String key

    ) {

        String value = properties.getProperty(key);

        if (value == null) {

            throw new IllegalArgumentException(
                    "Missing legacy statistic: " + key
            );
        }

        return Long.parseLong(value.trim());
    }

    public Path getLegacyFile() {

        return legacyFile;
    }
}