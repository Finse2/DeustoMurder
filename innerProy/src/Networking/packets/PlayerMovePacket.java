package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

public class PlayerMovePacket extends Packet {
    private static final long serialVersionUID = 1L;
    private final int playerPosX;
    private final int playerPosY;
    private final int moveDistance;
    private final int playerID;

    public PlayerMovePacket(int PlayerPosX, int PlayerPosY, int MoveDistance,int PlayerID) {
        super(PacketType.PLAYER_MOVE);
        this.playerPosX = PlayerPosX;
        this.playerPosY = PlayerPosY;
        this.moveDistance = MoveDistance;
        this.playerID = PlayerID;
    }

    public int getPlayerPosX() {
        return playerPosX;
    }

    public int getPlayerPosY() {
        return playerPosY;
    }

    public int getMoveDistance() {
        return moveDistance;
    }

    public int getPlayerID() {
        return playerID;
    }
}
