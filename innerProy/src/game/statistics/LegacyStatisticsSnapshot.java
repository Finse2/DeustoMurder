package game.statistics;

public record LegacyStatisticsSnapshot(

        String playerName,

        long gamesPlayed,

        long wins,

        long losses,

        long accusationsMade,

        long correctAccusations,

        long incorrectAccusations,

        long totalGameDurationMillis

) {

    public LegacyStatisticsSnapshot {

        if (gamesPlayed < 0
                || wins < 0
                || losses < 0
                || accusationsMade < 0
                || correctAccusations < 0
                || incorrectAccusations < 0
                || totalGameDurationMillis < 0) {

            throw new IllegalArgumentException(
                    "Legacy statistics cannot be negative."
            );
        }

        if (wins > gamesPlayed - losses) {

            throw new IllegalArgumentException(
                    "Wins and losses exceed games played."
            );
        }

        if (correctAccusations >
                accusationsMade - incorrectAccusations) {

            throw new IllegalArgumentException(
                    "Invalid accusation statistics."
            );
        }

        playerName = playerName == null
                || playerName.isBlank()
                ? "Player"
                : playerName.trim();
    }
}