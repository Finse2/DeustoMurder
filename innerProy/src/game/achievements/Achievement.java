package game.achievements;

import java.util.List;

public class Achievement {

    private AchievementType achievementType;
    private String name;
    private String description;
    private boolean secret;

    // CONSTRUCTORES

    public Achievement (AchievementType type, String name, String description, boolean secret) {
        this.achievementType = type;
        this.name = name;
        this.description = description;
        this.secret = secret;
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

    public boolean isSecret() {
        return secret;
    }

    public boolean hasStages(Achievement achievement) {
        return !achievement.isSecret();
    }



}
