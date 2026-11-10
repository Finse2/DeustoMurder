package achievements;

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


/*
en lugar de que Game determine cuándo se consigue FIRST_WIN, dejaría que StatisticsManager detecte los logros relacionados con estadísticas.

algo como:

public void recordWin() {
    wins++;

    if (wins == 1)
        achievementConsumer.accept(AchievementType.FIRST_WIN);

    if (wins == 10)
        achievementConsumer.accept(AchievementType.TEN_WINS);
}
así centraría las condiciones en lugar de tenerlo disperso que no es eficiente

solo habría que distribuir Consumable <AchievementType> y nos ahorraríamos cleanups

algo así, y estaría listo;

AchievementManager manager = new AchievementManager();

Consumer<AchievementType> achievementConsumer = manager::addAchievement;

de esta manera todo el codigo estará en esta clase y no tendreis que hacer nada mas que spamear Consumer<AchievementType>-s afuera
 */