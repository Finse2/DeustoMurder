package Networking;

final class PlayerMovePacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final int playerPosX;
    private final int playerPosY;
    private final int moveDistance;
    private final int playerID;

    PlayerMovePacket(int playerPosX, int playerPosY, int moveDistance, int playerID) {
        super(PacketType.PLAYER_MOVE);
        this.playerPosX = playerPosX;
        this.playerPosY = playerPosY;
        this.moveDistance = moveDistance;
        this.playerID = playerID;
    }

    int getPlayerPosX() {
        return playerPosX;
    }

    int getPlayerPosY() {
        return playerPosY;
    }

    int getMoveDistance() {
        return moveDistance;
    }

    int getPlayerID() {
        return playerID;
    }
}
