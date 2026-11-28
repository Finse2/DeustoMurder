
package Networking.match;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import java.util.concurrent.TimeUnit;

public final class NetworkMatchManager {

    // MATCH STATES

    public enum State {

        IDLE,

        IN_PROGRESS,

        COMPLETED,

        CANCELLED
    }

    // INITIAL PARTICIPANT INFORMATION

    public record Participant(

            int sessionId,

            UUID profileId,

            String displayName,

            String characterName

    ) {

        public Participant {

            Objects.requireNonNull(profileId);

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
        }
    }

    // MUTABLE MATCH STATISTICS

    private static final class PlayerData {

        private final Participant participant;

        private String characterName;

        private long turnsPlayed;

        private long diceRolls;

        private long movesMade;

        private long suggestionsMade;

        private long correctAccusations;

        private long incorrectAccusations;

        private long pointsEarned;

        private PlayerData(
                Participant participant
        ) {

            this.participant = participant;

            this.characterName =
                    participant.characterName();
        }

        private NetworkPlayerResult snapshot(
                UUID winnerId
        ) {

            boolean won =
                    participant.profileId()
                            .equals(winnerId);

            return new NetworkPlayerResult(

                    participant.sessionId(),

                    participant.profileId(),

                    participant.displayName(),

                    characterName,

                    won
                            ? NetworkPlayerResult.Result.WIN
                            : NetworkPlayerResult.Result.LOSS,

                    turnsPlayed,

                    diceRolls,

                    movesMade,

                    suggestionsMade,

                    correctAccusations,

                    incorrectAccusations,

                    pointsEarned
            );
        }
    }

    // MATCH FIELDS

    private final Map<Integer, PlayerData> players =
            new LinkedHashMap<>();

    private State state = State.IDLE;

    private UUID matchId;

    private UUID hostId;

    private long startedAt;

    private long startNanos;

    private NetworkMatchResult completedResult;

    // START MATCH

    public synchronized UUID startMatch(

            UUID hostId,

            List<Participant> participants

    ) {

        if (state == State.IN_PROGRESS) {

            throw new IllegalStateException(
                    "A match is already in progress."
            );
        }

        Objects.requireNonNull(hostId);

        participants = List.copyOf(participants);

        if (participants.size() < 2) {

            throw new IllegalArgumentException(
                    "At least two players are required."
            );
        }

        // Validate before modifying current state.

        Set<Integer> sessionIds = new HashSet<>();

        Set<UUID> profileIds = new HashSet<>();

        for (Participant participant : participants) {

            if (!sessionIds.add(
                    participant.sessionId()
            )) {

                throw new IllegalArgumentException(
                        "Duplicate session ID."
                );
            }

            if (!profileIds.add(
                    participant.profileId()
            )) {

                throw new IllegalArgumentException(
                        "Duplicate profile ID."
                );
            }
        }

        if (!profileIds.contains(hostId)) {

            throw new IllegalArgumentException(
                    "Host must participate in the match."
            );
        }

        players.clear();

        for (Participant participant : participants) {

            players.put(

                    participant.sessionId(),

                    new PlayerData(participant)
            );
        }

        this.hostId = hostId;

        matchId = UUID.randomUUID();

        startedAt = System.currentTimeMillis();

        startNanos = System.nanoTime();

        completedResult = null;

        state = State.IN_PROGRESS;

        return matchId;
    }

    // CHARACTER SELECTION

    public synchronized void setCharacter(

            int sessionId,

            String characterName

    ) {

        if (characterName == null
                || characterName.isBlank()) {

            throw new IllegalArgumentException(
                    "Character name cannot be blank."
            );
        }

        requirePlayer(sessionId).characterName =
                characterName.trim();
    }

    // RECORD TURN

    public synchronized void recordTurn(
            int sessionId
    ) {

        PlayerData player =
                requirePlayer(sessionId);

        player.turnsPlayed =
                Math.incrementExact(
                        player.turnsPlayed
                );
    }

    // RECORD DICE ROLL

    public synchronized void recordDiceRoll(
            int sessionId
    ) {

        PlayerData player =
                requirePlayer(sessionId);

        player.diceRolls =
                Math.incrementExact(
                        player.diceRolls
                );
    }

    // RECORD MOVEMENT

    public synchronized void recordMovement(

            int sessionId,

            long tilesMoved

    ) {

        if (tilesMoved < 0) {

            throw new IllegalArgumentException(
                    "Movement cannot be negative."
            );
        }

        PlayerData player =
                requirePlayer(sessionId);

        player.movesMade =
                Math.addExact(

                        player.movesMade,

                        tilesMoved
                );
    }

    // RECORD SUGGESTION

    public synchronized void recordSuggestion(
            int sessionId
    ) {

        PlayerData player =
                requirePlayer(sessionId);

        player.suggestionsMade =
                Math.incrementExact(
                        player.suggestionsMade
                );
    }

    // RECORD ACCUSATION

    public synchronized void recordAccusation(

            int sessionId,

            boolean correct

    ) {

        PlayerData player =
                requirePlayer(sessionId);

        if (correct) {

            player.correctAccusations =
                    Math.incrementExact(
                            player.correctAccusations
                    );

        } else {

            player.incorrectAccusations =
                    Math.incrementExact(
                            player.incorrectAccusations
                    );
        }
    }

    // RECORD POINTS

    public synchronized void addPoints(

            int sessionId,

            long points

    ) {

        if (points < 0) {

            throw new IllegalArgumentException(
                    "Points cannot be negative."
            );
        }

        PlayerData player =
                requirePlayer(sessionId);

        player.pointsEarned =
                Math.addExact(

                        player.pointsEarned,

                        points
                );
    }

    // COMPLETE MATCH

    public synchronized NetworkMatchResult completeMatch(

            UUID expectedMatchId,

            int winnerSessionId,

            String murderer,

            String weapon,

            String room

    ) {

        requireMatchId(expectedMatchId);

        // Duplicate completion requests return
        // the original immutable match result.

        if (state == State.COMPLETED) {

            if (completedResult.getWinnerSessionId()
                    != winnerSessionId

                    || !Objects.equals(
                    completedResult.murderer(),
                    murderer
            )

                    || !Objects.equals(
                    completedResult.weapon(),
                    weapon
            )

                    || !Objects.equals(
                    completedResult.room(),
                    room
            )) {

                throw new IllegalStateException(
                        "Conflicting match completion."
                );
            }

            return completedResult;
        }

        requireActiveMatch();

        PlayerData winner =
                requirePlayer(winnerSessionId);

        UUID winnerId =
                winner.participant.profileId();

        long finishedAt =
                Math.max(

                        startedAt,

                        System.currentTimeMillis()
                );

        long durationMillis =
                Math.max(

                        0,

                        TimeUnit.NANOSECONDS.toMillis(

                                System.nanoTime()
                                        - startNanos
                        )
                );

        List<NetworkPlayerResult> results =
                new ArrayList<>();

        for (PlayerData player : players.values()) {

            results.add(
                    player.snapshot(winnerId)
            );
        }

        completedResult =
                new NetworkMatchResult(

                        matchId,

                        hostId,

                        winnerId,

                        startedAt,

                        finishedAt,

                        durationMillis,

                        murderer,

                        weapon,

                        room,

                        results
                );

        state = State.COMPLETED;

        return completedResult;
    }

    // CANCEL MATCH

    public synchronized boolean cancelMatch(
            UUID expectedMatchId
    ) {

        requireMatchId(expectedMatchId);

        if (state == State.CANCELLED) {

            return false;
        }

        requireActiveMatch();

        state = State.CANCELLED;

        return true;
    }

    // RESET MANAGER

    public synchronized void reset() {

        if (state == State.IN_PROGRESS) {

            throw new IllegalStateException(
                    "Cannot reset an active match."
            );
        }

        players.clear();

        matchId = null;

        hostId = null;

        startedAt = 0;

        startNanos = 0;

        completedResult = null;

        state = State.IDLE;
    }

    // VALIDATION

    private void requireActiveMatch() {

        if (state != State.IN_PROGRESS) {

            throw new IllegalStateException(
                    "There is no active match."
            );
        }
    }

    private void requireMatchId(
            UUID expectedMatchId
    ) {

        if (matchId == null
                || !matchId.equals(expectedMatchId)) {

            throw new IllegalArgumentException(
                    "Incorrect match UUID."
            );
        }
    }

    private PlayerData requirePlayer(
            int sessionId
    ) {

        requireActiveMatch();

        PlayerData player =
                players.get(sessionId);

        if (player == null) {

            throw new IllegalArgumentException(
                    "Unknown match participant: "
                            + sessionId
            );
        }

        return player;
    }

    // GETTERS

    public synchronized State getState() {

        return state;
    }

    public synchronized boolean isInProgress() {

        return state == State.IN_PROGRESS;
    }

    public synchronized UUID getMatchId() {

        return matchId;
    }

    public synchronized UUID getHostId() {

        return hostId;
    }

    public synchronized long getStartedAt() {

        return startedAt;
    }

    public synchronized int getPlayerCount() {

        return players.size();
    }

    public synchronized boolean containsPlayer(
            int sessionId
    ) {

        return players.containsKey(sessionId);
    }

    public synchronized Optional<NetworkMatchResult>
    getCompletedResult() {

        return Optional.ofNullable(
                completedResult
        );
    }

    public synchronized List<Participant>
    getParticipants() {

        List<Participant> result =
                new ArrayList<>();

        for (PlayerData player : players.values()) {

            Participant participant =
                    player.participant;

            result.add(

                    new Participant(

                            participant.sessionId(),

                            participant.profileId(),

                            participant.displayName(),

                            player.characterName
                    )
            );
        }

        return List.copyOf(result);
    }
}
