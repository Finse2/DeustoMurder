package game.statistics;

public class PlayerStatistics {

    // IMPLEMENTACIONES:

    /*
    Registrar partidas abandonadas
    Calcular la racha actual y la mejor racha de victorias
    Contar movimientos realizados y movimientos inválidos
    Calcular el número medio de turnos por partida
    Registrar tiempo total jugado y tiempo medio por turno
    Guardar la última partida jugada
    Calcular el porcentaje de acusaciones correctas
    Añadir un sistema de puntos (ELO)
     */

    private int playerId;
    private String playerName;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private int accusationsMade;
    private int correctAccusations;
    private int incorrectAccusations;
    private long totalGameDurationMillis;

    public PlayerStatistics (int playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName == null ? "" : playerName;
    }

    /**
     * Restore a previously saved local profile. Validate invariants so damaged
     * files cannot turn the win rate or counters into inconsistent values.
     */
    public static PlayerStatistics fromPersisted(
            int playerId, String playerName, int gamesPlayed, int wins, int losses,
            int accusationsMade, int correctAccusations, int incorrectAccusations,
            long totalGameDurationMillis) {
        if (gamesPlayed < 0 || wins < 0 || losses < 0 || wins + (long) losses != gamesPlayed
                || accusationsMade < 0 || correctAccusations < 0 || incorrectAccusations < 0
                || correctAccusations + (long) incorrectAccusations != accusationsMade
                || totalGameDurationMillis < 0) {
            throw new IllegalArgumentException("Invalid saved player statistics");
        }
        PlayerStatistics result = new PlayerStatistics(playerId, playerName);
        result.gamesPlayed = gamesPlayed;
        result.wins = wins;
        result.losses = losses;
        result.accusationsMade = accusationsMade;
        result.correctAccusations = correctAccusations;
        result.incorrectAccusations = incorrectAccusations;
        result.totalGameDurationMillis = totalGameDurationMillis;
        return result;
    }

    public synchronized long getTotalGameDurationMillis() {
        return totalGameDurationMillis;
    }

    public synchronized void updateName (String name) {
        if (name != null && !name.trim().isEmpty()) {
            playerName = name.trim();
        }
    }

    public synchronized void recordGameFinished (boolean won, long durationMillis) {
        gamesPlayed++;

        if (won) {
            wins++;
        } else {
            losses++;
        }

        totalGameDurationMillis += Math.max (0, durationMillis);
    }

    public synchronized void recordAccusation (boolean correct) {
        accusationsMade++;

        if (correct) {
            correctAccusations++;
        } else {
             incorrectAccusations++;
            }
        }

        public int getPlayerId(){
        return playerId;
    }

    public synchronized String getPlayerName() {
        return playerName;
    }

    public synchronized int getGamesPlayed() {
        return gamesPlayed;
    }

    public synchronized int getWins() {
        return wins;
    }

    public synchronized int getLosses() {
        return losses;
    }

    public synchronized int getAccusationsMade() {
        return accusationsMade;
    }

    public synchronized int getCorrectAccusations() {
        return correctAccusations;
    }

    public synchronized int getIncorrectAccusations() {
        return incorrectAccusations;
    }

    public synchronized double getWinRate() {
        if (gamesPlayed == 0) {
            return 0;
        }

        return (double) wins / gamesPlayed;
    }

    public synchronized double getAccusationAccuracy() {
        if (accusationsMade == 0) {
            return 0;
        }

        return (double) correctAccusations / accusationsMade;
    }

    public synchronized long getAverageGameDurationMillis () {
        if (gamesPlayed == 0) {
            return 0;
        }

        return totalGameDurationMillis / gamesPlayed;
    }

    @Override

    public synchronized String toString () {
        return "PlayerStatistics{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", gamesPlayed=" + gamesPlayed +
                ", wins=" + wins +
                ", losses=" + losses +
                ", winRate=" +
                String.format("%.1f%%", getWinRate() * 100) +
                '}';
    }

}
