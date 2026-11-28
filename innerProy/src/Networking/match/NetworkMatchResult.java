
package Networking.match;

import java.io.Serializable;

import java.time.Duration;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record NetworkMatchResult(

        UUID matchId,

        UUID hostId,

        UUID winnerId,

        long startedAt,

        long finishedAt,

        long durationMillis,

        String murderer,

        String weapon,

        String room,

        List<NetworkPlayerResult> players

) implements Serializable {

    private static final long serialVersionUID = 1L;

    public NetworkMatchResult {

        Objects.requireNonNull(matchId);
        Objects.requireNonNull(hostId);
        Objects.requireNonNull(winnerId);
        Objects.requireNonNull(players);

        if (startedAt < 0
                || finishedAt < startedAt
                || durationMillis < 0) {

            throw new IllegalArgumentException(
                    "Invalid match timestamps."
            );
        }

        // Defensive copy prevents modification.

        players = List.copyOf(players);

        if (players.size() < 2) {

            throw new IllegalArgumentException(
                    "A match requires at least two players."
            );
        }

        Set<UUID> profileIds = new HashSet<>();

        Set<Integer> sessionIds = new HashSet<>();

        int winnerCount = 0;

        for (NetworkPlayerResult player : players) {

            if (!profileIds.add(player.profileId())) {

                throw new IllegalArgumentException(
                        "Duplicate player profile."
                );
            }

            if (!sessionIds.add(player.sessionId())) {

                throw new IllegalArgumentException(
                        "Duplicate session ID."
                );
            }

            if (player.hasWon()) {

                winnerCount++;

                if (!winnerId.equals(
                        player.profileId()
                )) {

                    throw new IllegalArgumentException(
                            "Winner identity mismatch."
                    );
                }
            }
        }

        if (!profileIds.contains(hostId)) {

            throw new IllegalArgumentException(
                    "Host is not a match participant."
            );
        }

        if (winnerCount != 1
                || !profileIds.contains(winnerId)) {

            throw new IllegalArgumentException(
                    "A match must have exactly one winner."
            );
        }
    }

    // PLAYER LOOKUP

    public Optional<NetworkPlayerResult> getPlayer(
            UUID profileId
    ) {

        return players.stream()

                .filter(player ->
                        player.profileId()
                                .equals(profileId)
                )

                .findFirst();
    }

    public Optional<NetworkPlayerResult> getPlayer(
            int sessionId
    ) {

        return players.stream()

                .filter(player ->
                        player.sessionId() == sessionId
                )

                .findFirst();
    }

    // WINNER INFORMATION

    public NetworkPlayerResult getWinner() {

        return getPlayer(winnerId)

                .orElseThrow();
    }

    public String getWinnerName() {

        return getWinner().displayName();
    }

    public int getWinnerSessionId() {

        return getWinner().sessionId();
    }

    // MATCH INFORMATION

    public int getPlayerCount() {

        return players.size();
    }

    public Duration getDuration() {

        return Duration.ofMillis(
                durationMillis
        );
    }

    public boolean containsPlayer(
            UUID profileId
    ) {

        return getPlayer(profileId)
                .isPresent();
    }

    public boolean isWinner(
            UUID profileId
    ) {

        return winnerId.equals(profileId);
    }
}
