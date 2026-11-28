
package game.statistics;

import java.util.Objects;
import java.util.UUID;

public record MatchPlayerSnapshot(

        UUID matchId,

        UUID playerId,

        String characterName,

        Result result,

        long turnsPlayed,

        long diceRolls,

        long movesMade,

        long suggestionsMade,

        long correctAccusations,

        long incorrectAccusations,

        long pointsEarned

) {

    public enum Result {

        PENDING,

        WIN,

        LOSS,

        DRAW,

        DISCONNECTED
    }

    public MatchPlayerSnapshot {

        Objects.requireNonNull(matchId);

        Objects.requireNonNull(playerId);

        Objects.requireNonNull(result);
    }

    public long getTotalAccusations() {

        return correctAccusations
                + incorrectAccusations;
    }

    public boolean hasWon() {

        return result == Result.WIN;
    }

    public boolean hasLost() {

        return result == Result.LOSS;
    }

    public double getAccusationAccuracy() {

        long total = getTotalAccusations();

        if (total == 0) {

            return 0.0;
        }

        return 100.0 * correctAccusations
                / total;
    }
}
