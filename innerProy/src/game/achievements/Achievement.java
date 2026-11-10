package game.achievements;

import java.util.List;

public class Achievement {

    private AchievementType achievementType;
    private String name;
    private String description;
    private AchievementCategory category;
    private boolean secret;
    private List<AchievementStage> stages;

    // CONSTRUCTORES

    public Achievement (AchievementType type, String name, String description, AchievementCategory category, boolean secret, List<AchievementStage> stages) {
        this.achievementType = type;
        this.name = name;
        this.description = description;
        this.category = category;
        this.secret = secret;
        this.stages = stages;
    }

    // GETTERS

    public AchievementType getAchievementType() {
        return achievementType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AchievementCategory getCategory() {
        return category;
    }

    public boolean isSecret() {
        return secret;
    }

    public List<AchievementStage> getStages() {
        return stages;
    }

    public boolean hasStages(Achievement achievement) {
        return !achievement.isSecret();
    }



}
