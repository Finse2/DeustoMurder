package game;

public class PlayerStatistics {

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
