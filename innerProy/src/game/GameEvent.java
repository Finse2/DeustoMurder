package game;

import java.time.Instant;
import java.util.Objects;

public class GameEvent {

    public enum Type {
        GAME_STARTED,
        PLAYER_JOINED,
        PLAYER_LEFT,
        PLAYER_MOVED,
        TURN_STARTED,
        TURN_ENDED,
        SUGGESTION_MADE,
        SUGGESTION_REFUTED,
        ACCUSATION_MADE,
        ACCUSATION_CORRECT,
        ACCUSATION_INCORRECT,
        PLAYER_DISCONNECTED,
        GAME_FINISHED,
        CUSTOM
    }

    private final Instant timestamp;
    private final Type type;
    private final Integer playerId;
    private final String playerName;
    private final String description;

    public GameEvent (Type type, Integer playerId, String playerName, String description) {
        this.timestamp = Instant.now();
        this.type = Objects.requireNonNull(type);
        this.playerId = playerId;
        this.playerName = playerName == null ? "" : playerName;
        this.description = description == null ? "" : description;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public Integer getPlayerId () {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDescription() {
        return description;
    }

    @Override

    public String toString () {
        String player = playerId == null
                ? "System"
                : playerName + " (#" + playerId + ")";

        return "[" + timestamp + "] "
                + type + " | "
                + player + " | "
                + description;
    }
}
