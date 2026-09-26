package Networking;

final class PlayerJoinedPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final int playerID;
    private final String playerName;
    private final String computerName;

    PlayerJoinedPacket(int playerID, String playerName, String computerName) {
        super(PacketType.PLAYER_JOINED);
        this.playerID = playerID;
        this.playerName = playerName;
        this.computerName = computerName;
    }

    int getPlayerID() {
        return playerID;
    }

    String getPlayerName() {
        return playerName;
    }

    String getComputerName() {
        return computerName;
    }
}
