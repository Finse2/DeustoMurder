
package game.statistics;

import java.time.Duration;
import java.time.Instant;

import java.util.Objects;
import java.util.UUID;

public record MatchSnapshot(

        UUID matchId,

        UUID hostId,

        UUID winnerId,

        long startedAt,

        Long finishedAt,

        long durationMillis,

        Status status,

        String solutionActor,

        String solutionWeapon,

        String solutionRoom

) {

    public enum Status {

        IN_PROGRESS,

        COMPLETED,

        CANCELLED
    }

    public MatchSnapshot {

        Objects.requireNonNull(matchId);

        Objects.requireNonNull(status);
    }

    public boolean isInProgress() {

        return status == Status.IN_PROGRESS;
    }

    public boolean isCompleted() {

        return status == Status.COMPLETED;
    }

    public boolean isCancelled() {

        return status == Status.CANCELLED;
    }

    public Instant getStartTime() {

        return Instant.ofEpochMilli(startedAt);
    }

    public Instant getFinishTime() {

        return finishedAt == null

                ? null

                : Instant.ofEpochMilli(finishedAt);
    }

    public Duration getDuration() {

        return Duration.ofMillis(durationMillis);
    }

    public boolean isWinner(UUID playerId) {

        return winnerId != null

                && winnerId.equals(playerId);
    }

    public String getFormattedDuration() {

        Duration duration = getDuration();

        return String.format(

                "%02d:%02d:%02d",

                duration.toHours(),

                duration.toMinutesPart(),

                duration.toSecondsPart()
        );
    }
}
