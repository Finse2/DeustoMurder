package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;

public final class PlayerMovePacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final int playerPosX;
    private final int playerPosY;
    private final int moveDistance;
    private final int playerID;

    public PlayerMovePacket(int playerPosX, int playerPosY, int moveDistance, int playerID) {
        super(PacketType.PLAYER_MOVE);
        this.playerPosX = playerPosX;
        this.playerPosY = playerPosY;
        this.moveDistance = moveDistance;
        this.playerID = playerID;
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
