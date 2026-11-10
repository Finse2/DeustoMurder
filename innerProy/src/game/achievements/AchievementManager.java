package game.achievements;

import java.util.List;
import java.util.Map;

public class AchievementManager {

   // private final AchievementRepository achievementRepository;
    private Map<AchievementType, Achievement> achievements;
    private Map<String, PlayersAchievements> playersAchievements;

    // Inicializa el repositorio y carga el catálogo
    public AchievementManager(AchievementRepository repository) {
    }

    // Carga los logros y sus etapas desde los CSV
    public void loadAchievements() {

    }

    // Devuelve un logro a partir de su identificador
    public Achievement getAchievement(AchievementType type) {
        return null;
    }
    // Devuelve todos los logros disponibles
    public List<Achievement> getAllAchievements() {
        return null;
    }

    // Devuelve los logros de una categoría
    public List<Achievement> getAchievementsByCategory(
            AchievementCategory category
    ) {
        return null;
    }

    // Carga el progreso de un jugador
    public void loadPlayer(String playerId) {

    }

    // Devuelve los logros y el progreso de un jugador
    public PlayersAchievements getPlayerAchievements(String playerId) {
        return null;
    }

    // Guarda el progreso de un jugador
    public void savePlayer(String playerId) {

    }

    // Recibe una notificación de un manager externo
    public void addAchievement(AchievementEvent event) {

    }

    // Actualiza el progreso de un logro
    public void updateProgress(
            String playerId,
            AchievementType type,
            int amount
    ) {

    }

    // Comprueba qué etapas se pueden desbloquear
    public void checkAchievementStages(
            String playerId,
            AchievementType type
    ) {

    }

    // Desbloquea una etapa y concede sus puntos
    public void unlockStage(
            String playerId,
            AchievementType type,
            int stage
    ) {

    }


}
