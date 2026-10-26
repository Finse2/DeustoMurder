package game.statistics;

import model.Player;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class StatisticsManager {

    // IMPLEMENTACIONES:

    /*
    Ranking por victorias y win-rate
    Consultar los mejores jugadores de una categoría
    Filtrar estadísticas por periodo
    Añadir un sistema de puntos configurable
    Importar estadísticas desde un CSV
    Guardar y recuperar estadísticas entre ejecuciones
    Detectar estadísticas inconsistentes
    Generar informes generales de todas las partidas
     */

    private final Map<Integer, PlayerStatistics> statisticsByPlayerId = new HashMap<>();
    private final Map<String, Integer> gamesPlayed = new HashMap<>();
    private final Map<String, Integer> gamesWon = new HashMap<>();

    public synchronized PlayerStatistics getOrCreate(int playerId, String playerName) {
        PlayerStatistics statistics = statisticsByPlayerId.get(playerId);

        if (statistics == null) {
            statistics = new PlayerStatistics(playerId, playerName);

            statisticsByPlayerId.put(playerId, statistics);
        } else {
            statistics.updateName(playerName);
        }

        return statistics;
    }

    public synchronized void recordGameFinished(int playerId, String playerName, boolean won, long durationMillis) {
        getOrCreate(playerId, playerName).recordGameFinished(won, durationMillis);
    }

    public synchronized void recordAccusation(int playerId, String playerName, boolean correct) {
        getOrCreate(playerId, playerName).recordAccusation(correct);
    }

    public synchronized PlayerStatistics getStatistics(int playerId) {
        return statisticsByPlayerId.get(playerId);
    }

    public synchronized List<PlayerStatistics> getAllStatistics() {
        return new ArrayList<>(statisticsByPlayerId.values());
    }

    public synchronized List<PlayerStatistics> getLeaderboard() {
        List<PlayerStatistics> result = getAllStatistics();

        result.sort(Comparator.comparingInt(
                        PlayerStatistics::getWins).reversed()
                .thenComparing(
                        Comparator.comparingDouble(
                                PlayerStatistics::getWinRate).reversed())
                .thenComparing(
                        PlayerStatistics::getPlayerName,
                        String.CASE_INSENSITIVE_ORDER));

        return result;
    }

    public synchronized int getRegisteredPlayerCount() {
        return statisticsByPlayerId.size();
    }

    public synchronized void removePlayer(int playerId) {
        statisticsByPlayerId.remove(playerId);
    }

    public synchronized void exportCSV(Path destination) throws IOException {
        Objects.requireNonNull(destination);

        Path parent = destination.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            writer.write("playerId,playerName,gamesPlayed,wins,losses," +
                    "winRate,accusationsMade,correctAccusations," +
                    "incorrectAccusations,averageGameDurationMillis");

            writer.newLine();

            for (PlayerStatistics stats : getLeaderboard()) {
                writer.write(stats.getPlayerId() + ",");
                writer.write(csv(stats.getPlayerName()) + ",");
                writer.write(stats.getGamesPlayed() + ",");
                writer.write(stats.getWins() + ",");
                writer.write(stats.getLosses() + ",");
                writer.write(stats.getWinRate() + ",");
                writer.write(stats.getAccusationsMade() + ",");
                writer.write(stats.getCorrectAccusations() + ",");
                writer.write(stats.getIncorrectAccusations() + ",");
                writer.write(
                        stats.getAverageGameDurationMillis() + "");
                writer.newLine();
            }
        }
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public void recordGame(List<Player> players, Player winner) {
        for (Player player : players) {
            String playerName = player.getName();

            gamesPlayed.put(playerName, gamesPlayed.getOrDefault(playerName, 0) + 1);

            gamesWon.putIfAbsent(playerName, 0);

            if (winner != null && winner.equals(player)) {
                gamesWon.put(playerName, gamesWon.get(playerName) + 1);
            }
        }
    }
}
