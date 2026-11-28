package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;

import java.util.Objects;
import java.util.UUID;

public final class GameStartPacket extends Packet {

    private static final long serialVersionUID =
            Server.serialVersionUID;

    private final boolean startGame;
    private final UUID matchId;
    private final UUID hostId;
    private final long startedAt;

    // CLIENT START REQUEST

    public GameStartPacket(boolean startGame) {

        super(PacketType.GAME_START);

        this.startGame = startGame;
        this.matchId = null;
        this.hostId = null;
        this.startedAt = 0;
    }

    // SERVER START CONFIRMATION

    public GameStartPacket(
            UUID matchId,
            UUID hostId,
            long startedAt
    ) {

        super(PacketType.GAME_START);

        this.startGame = true;

        this.matchId =
                Objects.requireNonNull(matchId);

        this.hostId =
                Objects.requireNonNull(hostId);

        if (startedAt <= 0) {
            throw new IllegalArgumentException(
                    "Invalid match start time."
            );
        }

        this.startedAt = startedAt;
    }

    public boolean getGameStart() {
        return startGame;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public UUID getHostId() {
        return hostId;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public boolean isConfirmed() {

        return startGame
                && matchId != null
                && hostId != null
                && startedAt > 0;
    }
}