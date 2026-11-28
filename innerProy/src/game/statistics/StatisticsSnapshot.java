
package game.statistics;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public record StatisticsSnapshot(

        UUID playerId,

        long gamesPlayed,
        long wins,
        long losses,

        long accusationsMade,
        long correctAccusations,
        long incorrectAccusations,

        long currentWinStreak,
        long bestWinStreak,

        long totalGameDurationMillis,
        long totalPoints

) {

    public StatisticsSnapshot {

        Objects.requireNonNull(
                playerId, "playerId"
        );
    }

    // WIN RATE

    public double getWinRate() {

        if (gamesPlayed == 0) {
            return 0.0;
        }

        return 100.0 * wins / gamesPlayed;
    }

    // LOSS RATE

    public double getLossRate() {

        if (gamesPlayed == 0) {
            return 0.0;
        }

        return 100.0 * losses / gamesPlayed;
    }

    // ACCUSATION ACCURACY

    public double getAccusationAccuracy() {

        if (accusationsMade == 0) {
            return 0.0;
        }

        return 100.0 * correctAccusations
                / accusationsMade;
    }

    // AVERAGE GAME DURATION

    public long getAverageGameDurationMillis() {

        if (gamesPlayed == 0) {
            return 0;
        }

        return totalGameDurationMillis / gamesPlayed;
    }

    public Duration getAverageGameDuration() {

        return Duration.ofMillis(
                getAverageGameDurationMillis()
        );
    }

    public Duration getTotalGameDuration() {

        return Duration.ofMillis(
                totalGameDurationMillis
        );
    }

    // AVERAGE POINTS

    public double getAveragePoints() {

        if (gamesPlayed == 0) {
            return 0.0;
        }

        return (double) totalPoints / gamesPlayed;
    }

    // FORMATTING

    public String getFormattedTotalGameDuration() {

        return formatDuration(
                getTotalGameDuration()
        );
    }

    public String getFormattedAverageGameDuration() {

        return formatDuration(
                getAverageGameDuration()
        );
    }

    private static String formatDuration(
            Duration duration
    ) {

        return String.format(
                "%02d:%02d:%02d",

                duration.toHours(),

                duration.toMinutesPart(),

                duration.toSecondsPart()
        );
    }
}
