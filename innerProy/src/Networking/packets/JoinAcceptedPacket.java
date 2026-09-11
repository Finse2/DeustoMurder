package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

public class JoinAcceptedPacket extends Packet {
    private static final long serialVersionUID = 1L;
    private final int playerID;

    public JoinAcceptedPacket(int playerID) {
        super(PacketType.JOIN_ACCEPTED);
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return playerID;
    }
}
