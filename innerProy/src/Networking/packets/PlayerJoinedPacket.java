package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

public class PlayerJoinedPacket extends Packet {
    private static final long serialVersionUID = 1L;
    private final int playerID;
    private final String playerName;
    private final String computerName;

    public PlayerJoinedPacket(String playerName, String computerName) {
        this(0, playerName, computerName);
    }

    public PlayerJoinedPacket(int playerID, String playerName, String computerName) {
        super(PacketType.PLAYER_JOINED);
        this.playerID = playerID;
        this.playerName = playerName;
        this.computerName = computerName;
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
}
