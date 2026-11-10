package game.achievements;

import java.util.List;

public class AchievementRepository {

   // private static final String ACHIEVEMENTS_FILE;
   // private static final String STAGES_FILE;
   // private static final String PLAYER_ACHIEVEMENTS_FILE;

    // Lee achievements.csv y crea los objetos Achievement
    public List<Achievement> loadAchievements() {
        return null;
    }

    // Lee achievement_stages.csv y recupera las etapas
    public List<AchievementStage> loadStages() {
        return null;
    }

    // Recupera el progreso de un jugador
    public PlayersAchievements loadPlayerAchievements(String playerId) {
        return null;
    }

    // Guarda el progreso de un jugador
    public void savePlayerAchievements(PlayersAchievements player) {

    }

    // Guarda el progreso de varios jugadores
    public void saveAllPlayerAchievements(
            List<PlayersAchievements> players
    ) {

    }

}
