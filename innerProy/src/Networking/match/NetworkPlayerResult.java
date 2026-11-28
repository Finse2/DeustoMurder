
package Networking.match;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record NetworkPlayerResult(

        int sessionId,

        UUID profileId,

        String displayName,

        String characterName,

        Result result,

        long turnsPlayed,

        long diceRolls,

        long movesMade,

        long suggestionsMade,

        long correctAccusations,

        long incorrectAccusations,

        long pointsEarned

) implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Result {

        WIN,

        LOSS
    }

    public NetworkPlayerResult {

        Objects.requireNonNull(profileId);
        Objects.requireNonNull(result);

        if (sessionId < 0) {

            throw new IllegalArgumentException(
                    "Invalid session ID."
            );
        }

        if (displayName == null
                || displayName.isBlank()) {

            throw new IllegalArgumentException(
                    "Player name cannot be blank."
            );
        }

        displayName = displayName.trim();

        if (turnsPlayed < 0
                || diceRolls < 0
                || movesMade < 0
                || suggestionsMade < 0
                || correctAccusations < 0
                || incorrectAccusations < 0
                || pointsEarned < 0) {

            throw new IllegalArgumentException(
                    "Player statistics cannot be negative."
            );
        }
    }

    public boolean hasWon() {

        return result == Result.WIN;
    }

    public boolean hasLost() {

        return result == Result.LOSS;
    }

    public long getTotalAccusations() {

        return correctAccusations
                + incorrectAccusations;
    }

    public double getAccusationAccuracy() {

        long total = getTotalAccusations();

        if (total == 0) {

            return 0.0;
        }

        return 100.0
                * correctAccusations
                / total;
    }
}
