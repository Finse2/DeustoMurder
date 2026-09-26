package Networking;

final class JoinAcceptedPacket extends Packet {
    private static final long serialVersionUID = 1L;

    private final int playerID;

    JoinAcceptedPacket(int playerID) {
        super(PacketType.JOIN_ACCEPTED);
        this.playerID = playerID;
    }

    int getPlayerID() {
        return playerID;
    }
}
