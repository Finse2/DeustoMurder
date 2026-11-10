package game.achievements;

import java.util.EnumSet;
import java.util.Set;

public class PlayersAchievements{


    //usamos EnumSet para evitar duplicados
    private final Set<AchievementType> unlocked = EnumSet.noneOf(AchievementType.class);

    public void addAchievement(AchievementType type) {
        if (unlocked.add(type)) {
            System.out.println("Achievement unlocked: " + type);
        }
    }

    public boolean hasAchievement(AchievementType type) {
        return unlocked.contains(type);
    }
}