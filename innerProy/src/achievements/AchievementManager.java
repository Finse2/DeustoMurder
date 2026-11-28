package achievements;

import game.statistics.StatisticsSnapshot;

import utils.JDBC.DAO.AchievementDAO;
import utils.JDBC.DAO.AchievementDefinition;
import utils.JDBC.DAO.StatisticsDAO;

import utils.JDBC.DatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import java.util.function.ToLongFunction;

    public final class AchievementManager {

        private final AchievementDAO achievementDAO;

        private final StatisticsDAO statisticsDAO;

        // STATISTICS-BASED ACHIEVEMENTS

        private static final Map<
                AchievementType,
                ToLongFunction<StatisticsSnapshot>
                > STATISTICS_MAPPINGS = Map.of(

                AchievementType.WIN_GAMES,
                StatisticsSnapshot::wins,

                AchievementType.CONSECUTIVE_WINS,
                StatisticsSnapshot::bestWinStreak,

                AchievementType.MAKE_INCORRECT_FINAL_ACCUSATIONS,
                StatisticsSnapshot::incorrectAccusations,

                AchievementType.TOTAL_COMPLETED_MATCHES,
                StatisticsSnapshot::gamesPlayed,

                AchievementType.TOTAL_FINAL_ACCUSATIONS,
                StatisticsSnapshot::accusationsMade
        );

        // ACHIEVEMENT UNLOCK EVENT

        public record AchievementUnlock(

                UUID playerId,

                String achievementCode,

                String achievementName,

                String previousLevel,

                String newLevel,

                long progress

        ) {}

        // CONSTRUCTORS

        public AchievementManager() {

            this(DatabaseService.getInstance());
        }

        public AchievementManager(DatabaseService database) {

            Objects.requireNonNull(database);

            this.achievementDAO =
                    new AchievementDAO(database);

            this.statisticsDAO =
                    new StatisticsDAO(database);
        }

        // INITIALIZE ACHIEVEMENT CATALOGUE

        public void initializeAchievements()
                throws Exception {

            for (AchievementType type :
                    AchievementType.values()) {

                AchievementDefinition definition =
                        createDefinition(type);

                achievementDAO.registerAchievement(
                        definition
                );
            }

            registerSpecialAchievements();
        }

        // CREATE DATABASE ACHIEVEMENT DEFINITION

        private AchievementDefinition createDefinition(
                AchievementType type
        ) {

            int[] requirements = type.getRequirements();

            if (requirements.length != 4) {

                throw new IllegalArgumentException(
                        "Expected four requirements for "
                                + type.name()
                );
            }

            return new AchievementDefinition(

                    type.name(),

                    formatName(type.name()),

                    getDescription(type),

                    type.isHidden(),

                    (long) requirements[0],

                    (long) requirements[1],

                    (long) requirements[2],

                    (long) requirements[3],

                    null
            );
        }

        // SPECIAL ACHIEVEMENTS

        private void registerSpecialAchievements()
                throws Exception {

            achievementDAO.registerAchievement(

                    new AchievementDefinition(

                            "LUCK_OF_THE_GODS",

                            "Luck of the Gods",

                            "Win five consecutive matches.",

                            true,

                            null,
                            null,
                            null,
                            5L,

                            null
                    )
            );
        }

        // SYNCHRONIZE ACHIEVEMENTS WITH STATISTICS

        public synchronized List<AchievementUnlock>
        synchronizeStatistics(UUID playerId)
                throws Exception {

            Objects.requireNonNull(playerId);

            StatisticsSnapshot statistics =
                    statisticsDAO.getOrCreateStatistics(
                            playerId
                    );

            List<AchievementUnlock> unlocks =
                    new ArrayList<>();

            for (var mapping :
                    STATISTICS_MAPPINGS.entrySet()) {

                AchievementType type =
                        mapping.getKey();

                long progress = mapping.getValue()
                        .applyAsLong(statistics);

                updateAchievement(

                        playerId,

                        type.name(),

                        progress

                ).ifPresent(unlocks::add);
            }

            // Special winning streak achievement.

            updateAchievement(

                    playerId,

                    "LUCK_OF_THE_GODS",

                    statistics.bestWinStreak()

            ).ifPresent(unlocks::add);

            return List.copyOf(unlocks);
        }

        // UPDATE SINGLE ACHIEVEMENT

        public synchronized java.util.Optional<AchievementUnlock>
        updateAchievement(

                UUID playerId,

                String achievementCode,

                long progress

        ) throws Exception {

            AchievementDAO.AchievementProgress previous =
                    achievementDAO.getProgress(

                            playerId,

                            achievementCode

                    ).orElseThrow(() ->

                            new IllegalArgumentException(

                                    "Unknown player or achievement: "
                                            + achievementCode
                            )
                    );

            achievementDAO.updateProgress(

                    playerId,

                    achievementCode,

                    progress
            );

            AchievementDAO.AchievementProgress updated =
                    achievementDAO.evaluateAndUnlock(

                            playerId,

                            achievementCode
                    );

            return detectUnlock(previous, updated);
        }

        // INCREMENT EVENT-BASED ACHIEVEMENT

        public synchronized java.util.Optional<AchievementUnlock>
        incrementAchievement(

                UUID playerId,

                AchievementType type,

                long amount

        ) throws Exception {

            Objects.requireNonNull(type);

            if (STATISTICS_MAPPINGS.containsKey(type)) {

                throw new IllegalArgumentException(

                        "This achievement must be synchronized "
                                + "from player statistics."
                );
            }

            AchievementDAO.AchievementProgress previous =
                    achievementDAO.getProgress(

                            playerId,

                            type.name()

                    ).orElseThrow();

            achievementDAO.incrementProgress(

                    playerId,

                    type.name(),

                    amount
            );

            AchievementDAO.AchievementProgress updated =
                    achievementDAO.evaluateAndUnlock(

                            playerId,

                            type.name()
                    );

            return detectUnlock(previous, updated);
        }

        // DETECT NEW UNLOCK

        private java.util.Optional<AchievementUnlock>
        detectUnlock(

                AchievementDAO.AchievementProgress previous,

                AchievementDAO.AchievementProgress updated

        ) {

            if (previous.level().equals(updated.level())) {

                return java.util.Optional.empty();
            }

            return java.util.Optional.of(

                    new AchievementUnlock(

                            updated.playerId(),

                            updated.definition().code(),

                            updated.definition().name(),

                            previous.level(),

                            updated.level(),

                            updated.progress()
                    )
            );
        }

        // GET ACHIEVEMENTS

        public List<AchievementDAO.AchievementProgress>
        getPlayerAchievements(UUID playerId)
                throws Exception {

            return achievementDAO.getPlayerAchievements(
                    playerId
            );
        }

        public List<AchievementDAO.AchievementProgress>
        getVisibleAchievements(UUID playerId)
                throws Exception {

            return achievementDAO.getVisibleAchievements(
                    playerId
            );
        }

        public List<AchievementDAO.AchievementProgress>
        getUnlockedAchievements(UUID playerId)
                throws Exception {

            return achievementDAO.getUnlockedAchievements(
                    playerId
            );
        }

        public List<AchievementDAO.AchievementProgress>
        getCompletedAchievements(UUID playerId)
                throws Exception {

            return getPlayerAchievements(playerId)

                    .stream()

                    .filter(
                            AchievementDAO
                                    .AchievementProgress::isCompleted
                    )

                    .toList();
        }

        // COMPLETION

        public double getCompletionPercentage(
                UUID playerId
        ) throws Exception {

            return achievementDAO.getCompletionPercentage(
                    playerId
            );
        }

        public double getMasteryPercentage(
                UUID playerId
        ) throws Exception {

            return achievementDAO.getMasteryPercentage(
                    playerId
            );
        }

        // CHECK UNLOCK STATUS

        public boolean isUnlocked(

                UUID playerId,

                AchievementType type

        ) throws Exception {

            return achievementDAO.isUnlocked(

                    playerId,

                    type.name()
            );
        }

        // DESCRIPTION MAPPING

        private String getDescription(
                AchievementType type
        ) {

            return switch (type) {

                case WIN_GAMES ->
                        "Win completed matches.";

                case CONSECUTIVE_WINS ->
                        "Win consecutive completed matches.";

                case MAKE_INCORRECT_FINAL_ACCUSATIONS ->
                        "Make incorrect final accusations.";

                case TOTAL_COMPLETED_MATCHES ->
                        "Complete matches.";

                case TOTAL_FINAL_ACCUSATIONS ->
                        "Make final accusations.";

                default ->
                        formatName(type.name());
            };
        }

        // DISPLAY NAME FORMATTING

        private String formatName(String name) {

            String[] words = name

                    .toLowerCase(Locale.ROOT)

                    .split("_");

            StringBuilder builder =
                    new StringBuilder();

            for (String word : words) {

                if (word.isEmpty()) {
                    continue;
                }

                if (!builder.isEmpty()) {
                    builder.append(" ");
                }

                builder.append(

                        Character.toUpperCase(
                                word.charAt(0)
                        )

                ).append(word.substring(1));
            }

            return builder.toString();
        }
    }
