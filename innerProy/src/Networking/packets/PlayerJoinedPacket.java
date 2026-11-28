
package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;

import java.util.Objects;
import java.util.UUID;

public final class PlayerJoinedPacket extends Packet {

    private static final long serialVersionUID =
            Server.serialVersionUID;

    private final int playerID;

    private final String playerName;

    private final String computerName;

    private final UUID profileId;

    public PlayerJoinedPacket(
            int playerID,
            String playerName,
            String computerName,
            UUID profileId
    ) {

        super(PacketType.PLAYER_JOINED);

        this.playerID = playerID;

        this.playerName = playerName;

        this.computerName = computerName;

        this.profileId = Objects.requireNonNull(
                profileId,
                "Profile ID cannot be null"
        );
    }

    public int getPlayerID() {

        return playerID;
    }

    public String getPlayerName() {

        return playerName;
    }

    public String getComputerName() {

        return computerName;
    }

    public UUID getProfileId() {

        return profileId;
    }
}
