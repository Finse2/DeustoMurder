package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

public class JoinAcceptedPacket extends Packet {
    private int playerID;

    public JoinAcceptedPacket(int playerID) {
        super(PacketType.JOIN_ACCEPTED);
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return playerID;
    }
}
