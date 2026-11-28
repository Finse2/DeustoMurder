
package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;

import java.util.Objects;
import java.util.UUID;

public final class JoinPacket extends Packet {

    private static final long serialVersionUID =
            Server.serialVersionUID;

    private final String computerName;

    private final String userName;

    private final UUID profileId;

    public JoinPacket(
            String computerName,
            String userName,
            UUID profileId
    ) {

        super(PacketType.JOIN);

        this.computerName = computerName;

        this.userName = userName;

        this.profileId = Objects.requireNonNull(
                profileId,
                "Profile ID cannot be null"
        );
    }

    public String getComputerName() {

        return computerName;
    }

    public String getUserName() {

        return userName;
    }

    public UUID getProfileId() {

        return profileId;
    }
}
