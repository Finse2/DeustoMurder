package game.history;

import game.statistics.PlayerStatistics;
import game.statistics.StatisticsManager;

import java.nio.file.Paths;

public class HistoryMock {

    // IMPLEMENTACIONES:

    /*
    Generar partidas ficticias con varios jugadores
    Simular movimientos, turnos y acusaciones
    Crear eventos correctos e incorrectos para comprobar filtros
    Generar historiales grandes (1000-10000 eventos)
    Comprobar que la exportación CSV contiene los datos esperados
    Ejecutar escenarios de prueba repetibles
     */

    public static void main (String [] args) throws Exception {
        GameHistory history = new GameHistory();

        history.record(GameEvent.Type.GAME_STARTED, null, null, "The game has started");

        history.record(GameEvent.Type.PLAYER_MOVED, 1, "David", "Move made");

        history.record(GameEvent.Type.SUGGESTION_MADE, 1, "David", "A suggestion has been made");

        System.out.println("Events" + history.size());

        history.exportCSV(Paths.get("logs", "game-history.csv"));

        StatisticsManager manager = new StatisticsManager();

        manager.recordGameFinished(1, "David", true, 300000);

        manager.recordAccusation(1, "David", true);

        manager.recordGameFinished(2, "Antal", false, 420000);

        System.out.println("Classification: ");

        for (PlayerStatistics player : manager.getLeaderboard()) {
            System.out.println(player);
        }

        manager.exportCSV(Paths.get("logs", "leaderboard.csv"));
    }

}
