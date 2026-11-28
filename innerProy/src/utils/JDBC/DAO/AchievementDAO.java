
package utils.JDBC.DAO;

import utils.JDBC.DBManager;
import utils.JDBC.DatabaseService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class AchievementDAO {

    private final DatabaseService database;

    // ACHIEVEMENT PROGRESSION MODEL

    public record AchievementProgress(

            UUID playerId,
            AchievementDefinition definition,
            long progress,
            String level,
            Long unlockedAt,
            Long updatedAt

    ) {

        public boolean isUnlocked() {
            return !"LOCKED".equals(level);
        }

        public boolean isCompleted() {
            return level.equals(definition.getHighestLevel());
        }

        public String getNextLevel() {

            String[] levels = {
                    "BRONZE",
                    "GOLD",
                    "DIAMOND",
                    "CHAMP"
            };

            for (String candidate : levels) {

                if (rank(candidate) > rank(level)
                        && definition.getRequirement(candidate) != null) {

                    return candidate;
                }
            }

            return null;
        }

        public Long getNextRequirement() {

            String nextLevel = getNextLevel();

            if (nextLevel == null) {
                return null;
            }

            return definition.getRequirement(nextLevel);
        }

        public double getProgressPercentage() {

            Long requirement = getNextRequirement();

            if (requirement == null) {
                return 100.0;
            }

            return Math.min(
                    100.0,
                    100.0 * progress / requirement
            );
        }
    }

    // CONSTRUCTORS

    public AchievementDAO() {
        this(DatabaseService.getInstance());
    }

    public AchievementDAO(DatabaseService database) {

        this.database = Objects.requireNonNull(
                database, "database"
        );
    }

    // REGISTER ACHIEVEMENT

    public void registerAchievement(
            AchievementDefinition definition
    ) throws Exception {

        Objects.requireNonNull(definition);

        database.execute(db -> {

            db.executeUpdate("""
                    INSERT INTO achievements (

                        achievement_code,
                        name,
                        description,
                        hidden,

                        bronze_requirement,
                        gold_requirement,
                        diamond_requirement,
                        champ_requirement,

                        icon_path

                    )

                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)

                    ON CONFLICT(achievement_code)
                    DO NOTHING
                    """,

                    definition.code(),
                    definition.name(),
                    definition.description(),

                    definition.hidden() ? 1 : 0,

                    definition.bronzeRequirement(),
                    definition.goldRequirement(),
                    definition.diamondRequirement(),
                    definition.champRequirement(),

                    definition.iconPath()
            );

            AchievementDefinition existing =
                    findAchievement(
                            db,
                            definition.code()
                    ).orElseThrow(() ->
                            new SQLException(
                                    "Achievement registration failed."
                            )
                    );

            // Prevent silently changing an existing
            // achievement's requirements.

            if (!existing.equals(definition)) {

                throw new SQLException(
                        "Conflicting achievement definition: "
                                + definition.code()
                );
            }

            return null;
        });
    }

    // GET ACHIEVEMENT

    public Optional<AchievementDefinition> getAchievement(
            String achievementCode
    ) throws Exception {

        Objects.requireNonNull(achievementCode);

        return database.execute(db ->
                findAchievement(db, achievementCode)
        );
    }

    private Optional<AchievementDefinition> findAchievement(
            DBManager db,
            String achievementCode
    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT *
                FROM achievements
                WHERE achievement_code = ?
                """,

                achievementCode
        )) {

            if (rs.next()) {
                return Optional.of(mapDefinition(rs));
            }

            return Optional.empty();
        }
    }

    // GET ALL ACHIEVEMENTS

    public List<AchievementDefinition> getAllAchievements()
            throws Exception {

        return database.execute(db -> {

            List<AchievementDefinition> definitions =
                    new ArrayList<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT *
                    FROM achievements

                    ORDER BY
                        hidden ASC,
                        name COLLATE NOCASE,
                        achievement_code
                    """)) {

                while (rs.next()) {
                    definitions.add(mapDefinition(rs));
                }
            }

            return List.copyOf(definitions);
        });
    }

    // GET PLAYER PROGRESS

    public Optional<AchievementProgress> getProgress(
            UUID playerId,
            String achievementCode
    ) throws Exception {

        Objects.requireNonNull(playerId);
        Objects.requireNonNull(achievementCode);

        return database.execute(db ->
                findProgress(
                        db,
                        playerId,
                        achievementCode
                )
        );
    }

    private Optional<AchievementProgress> findProgress(
            DBManager db,
            UUID playerId,
            String achievementCode
    ) throws SQLException {

        try (ResultSet rs = db.executeQuery("""
                SELECT

                    a.*,

                    p.player_id AS profile_id,

                    COALESCE(pa.progress, 0)
                        AS player_progress,

                    COALESCE(pa.level, 'LOCKED')
                        AS player_level,

                    pa.unlocked_at,
                    pa.updated_at

                FROM players p

                CROSS JOIN achievements a

                LEFT JOIN player_achievements pa

                    ON pa.player_id = p.player_id

                    AND pa.achievement_code =
                        a.achievement_code

                WHERE p.player_id = ?

                  AND a.achievement_code = ?
                """,

                playerId.toString(),
                achievementCode
        )) {

            if (rs.next()) {
                return Optional.of(mapProgress(rs));
            }

            return Optional.empty();
        }
    }

    // UPDATE ABSOLUTE PROGRESS

    public AchievementProgress updateProgress(
            UUID playerId,
            String achievementCode,
            long progress
    ) throws Exception {

        return saveProgress(
                playerId,
                achievementCode,
                progress,
                false
        );
    }

    // INCREMENT PROGRESS

    public AchievementProgress incrementProgress(
            UUID playerId,
            String achievementCode,
            long amount
    ) throws Exception {

        return saveProgress(
                playerId,
                achievementCode,
                amount,
                true
        );
    }

    // SHARED PROGRESS UPDATE

    private AchievementProgress saveProgress(
            UUID playerId,
            String achievementCode,
            long amount,
            boolean increment
    ) throws Exception {

        Objects.requireNonNull(playerId);
        Objects.requireNonNull(achievementCode);

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Achievement progress cannot be negative."
            );
        }

        return database.execute(db -> {

            long now = System.currentTimeMillis();

            int affected = db.executeUpdate("""
                    INSERT INTO player_achievements (

                        player_id,
                        achievement_code,
                        progress,
                        level,
                        updated_at
                    )

                    SELECT

                        p.player_id,
                        a.achievement_code,
                        ?,
                        'LOCKED',
                        ?

                    FROM players p

                    CROSS JOIN achievements a

                    WHERE p.player_id = ?

                      AND a.achievement_code = ?

                    ON CONFLICT(
                        player_id,
                        achievement_code
                    )

                    DO UPDATE SET

                        progress = CASE

                            WHEN ? = 1 THEN

                                player_achievements.progress
                                + excluded.progress

                            ELSE MAX(

                                player_achievements.progress,
                                excluded.progress
                            )

                        END,

                        updated_at = MAX(
                            player_achievements.updated_at,
                            excluded.updated_at
                        )
                    """,

                    amount,
                    now,

                    playerId.toString(),
                    achievementCode,

                    increment ? 1 : 0
            );

            if (affected != 1) {

                throw new SQLException(
                        "Unknown player or achievement."
                );
            }

            return requireProgress(
                    db,
                    playerId,
                    achievementCode
            );
        });
    }

    // EVALUATE AND UNLOCK ELIGIBLE LEVEL

    public AchievementProgress evaluateAndUnlock(
            UUID playerId,
            String achievementCode
    ) throws Exception {

        AchievementProgress current =
                getProgress(playerId, achievementCode)

                        .orElseThrow(() ->
                                new SQLException(
                                        "Unknown player or achievement."
                                )
                        );

        String earnedLevel =
                current.definition().evaluateLevel(
                        current.progress()
                );

        if (rank(earnedLevel) <= rank(current.level())) {

            return current;
        }

        return unlockLevel(
                playerId,
                achievementCode,
                earnedLevel
        );
    }

    // UNLOCK ACHIEVEMENT LEVEL

    public AchievementProgress unlockLevel(
            UUID playerId,
            String achievementCode,
            String level
    ) throws Exception {

        Objects.requireNonNull(playerId);
        Objects.requireNonNull(achievementCode);
        Objects.requireNonNull(level);

        int targetRank = rank(level);

        if (targetRank == 0) {

            throw new IllegalArgumentException(
                    "Cannot unlock the LOCKED level."
            );
        }

        return database.execute(db -> {

            AchievementProgress current =
                    requireProgress(
                            db,
                            playerId,
                            achievementCode
                    );

            Long requirement =
                    current.definition()
                            .getRequirement(level);

            if (requirement == null) {

                throw new IllegalArgumentException(
                        "Achievement does not support level: "
                                + level
                );
            }

            // Never downgrade an existing achievement.

            if (rank(current.level()) >= targetRank) {
                return current;
            }

            if (current.progress() < requirement) {

                throw new IllegalStateException(
                        "Achievement requirement not satisfied."
                );
            }

            long now = System.currentTimeMillis();

            // The SQL condition prevents a downgrade
            // if another operation unlocks a higher
            // level before this update is executed.

            db.executeUpdate("""
                    UPDATE player_achievements

                    SET

                        level = ?,

                        unlocked_at = COALESCE(
                            unlocked_at,
                            ?
                        ),

                        updated_at = MAX(
                            updated_at,
                            ?
                        )

                    WHERE player_id = ?

                      AND achievement_code = ?

                      AND progress >= ?

                      AND (

                          CASE level

                              WHEN 'LOCKED' THEN 0
                              WHEN 'BRONZE' THEN 1
                              WHEN 'GOLD' THEN 2
                              WHEN 'DIAMOND' THEN 3
                              WHEN 'CHAMP' THEN 4

                          END

                      ) < ?
                    """,

                    level,
                    now,
                    now,

                    playerId.toString(),
                    achievementCode,

                    requirement,
                    targetRank
            );

            AchievementProgress updated =
                    requireProgress(
                            db,
                            playerId,
                            achievementCode
                    );

            if (rank(updated.level()) < targetRank) {

                throw new SQLException(
                        "Achievement level could not be unlocked."
                );
            }

            return updated;
        });
    }

    // COMPATIBILITY WITH EXISTING ENUM

    public AchievementProgress unlockLevel(
            UUID playerId,
            String achievementCode,
            Enum<?> level
    ) throws Exception {

        Objects.requireNonNull(level);

        return unlockLevel(
                playerId,
                achievementCode,
                level.name()
        );
    }

    // GET ALL PLAYER ACHIEVEMENTS

    public List<AchievementProgress> getPlayerAchievements(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db -> {

            List<AchievementProgress> achievements =
                    new ArrayList<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT

                        a.*,

                        p.player_id AS profile_id,

                        COALESCE(pa.progress, 0)
                            AS player_progress,

                        COALESCE(pa.level, 'LOCKED')
                            AS player_level,

                        pa.unlocked_at,
                        pa.updated_at

                    FROM players p

                    CROSS JOIN achievements a

                    LEFT JOIN player_achievements pa

                        ON pa.player_id = p.player_id

                        AND pa.achievement_code =
                            a.achievement_code

                    WHERE p.player_id = ?

                    ORDER BY

                        a.hidden ASC,

                        a.name COLLATE NOCASE,

                        a.achievement_code
                    """,

                    playerId.toString()
            )) {

                while (rs.next()) {
                    achievements.add(mapProgress(rs));
                }
            }

            return List.copyOf(achievements);
        });
    }

    // GET UNLOCKED ACHIEVEMENTS

    public List<AchievementProgress> getUnlockedAchievements(
            UUID playerId
    ) throws Exception {

        return getPlayerAchievements(playerId)
                .stream()
                .filter(AchievementProgress::isUnlocked)
                .toList();
    }

    // GET VISIBLE ACHIEVEMENTS

    public List<AchievementProgress> getVisibleAchievements(
            UUID playerId
    ) throws Exception {

        return getPlayerAchievements(playerId)
                .stream()
                .filter(achievement ->
                        !achievement.definition().hidden()
                                || achievement.isUnlocked()
                )
                .toList();
    }

    // CHECK IF ACHIEVEMENT IS UNLOCKED

    public boolean isUnlocked(
            UUID playerId,
            String achievementCode
    ) throws Exception {

        return getProgress(
                playerId,
                achievementCode
        )
                .map(AchievementProgress::isUnlocked)
                .orElse(false);
    }

    // ACHIEVEMENT COMPLETION

    public double getCompletionPercentage(
            UUID playerId
    ) throws Exception {

        List<AchievementProgress> achievements =
                getPlayerAchievements(playerId);

        if (achievements.isEmpty()) {
            return 0.0;
        }

        long unlocked = achievements.stream()
                .filter(AchievementProgress::isUnlocked)
                .count();

        return 100.0 * unlocked / achievements.size();
    }

    // ACHIEVEMENT MASTERY

    public double getMasteryPercentage(
            UUID playerId
    ) throws Exception {

        List<AchievementProgress> achievements =
                getPlayerAchievements(playerId);

        if (achievements.isEmpty()) {
            return 0.0;
        }

        long completed = achievements.stream()
                .filter(AchievementProgress::isCompleted)
                .count();

        return 100.0 * completed / achievements.size();
    }

    // RESULT SET MAPPING

    private AchievementDefinition mapDefinition(
            ResultSet rs
    ) throws SQLException {

        return new AchievementDefinition(

                rs.getString("achievement_code"),

                rs.getString("name"),

                rs.getString("description"),

                rs.getInt("hidden") == 1,

                nullableLong(rs, "bronze_requirement"),

                nullableLong(rs, "gold_requirement"),

                nullableLong(rs, "diamond_requirement"),

                nullableLong(rs, "champ_requirement"),

                rs.getString("icon_path")
        );
    }

    private AchievementProgress mapProgress(
            ResultSet rs
    ) throws SQLException {

        return new AchievementProgress(

                UUID.fromString(
                        rs.getString("profile_id")
                ),

                mapDefinition(rs),

                rs.getLong("player_progress"),

                rs.getString("player_level"),

                nullableLong(rs, "unlocked_at"),

                nullableLong(rs, "updated_at")
        );
    }

    private static Long nullableLong(
            ResultSet rs,
            String column
    ) throws SQLException {

        long value = rs.getLong(column);

        return rs.wasNull() ? null : value;
    }

    // INTERNAL UTILITIES

    private AchievementProgress requireProgress(
            DBManager db,
            UUID playerId,
            String achievementCode
    ) throws SQLException {

        return findProgress(
                db,
                playerId,
                achievementCode

        ).orElseThrow(() ->

                new SQLException(
                        "Unknown player or achievement."
                )
        );
    }

    private static int rank(String level) {

        return switch (level) {

            case "LOCKED" -> 0;
            case "BRONZE" -> 1;
            case "GOLD" -> 2;
            case "DIAMOND" -> 3;
            case "CHAMP" -> 4;

            default -> throw new IllegalArgumentException(
                    "Unknown achievement level: " + level
            );
        };
    }
}
