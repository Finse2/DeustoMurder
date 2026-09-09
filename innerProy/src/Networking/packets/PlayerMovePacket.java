package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

public class PlayerMovePacket extends Packet {
    private int PlayerPosX;
    private int PlayerPosY;
    private int MoveDistance;
    private int PlayerID;

    public PlayerMovePacket(int PlayerPosX, int PlayerPosY, int MoveDistance,int PlayerID) {
        super(PacketType.PLAYER_MOVE);
        this.PlayerPosX = PlayerPosX;
        this.PlayerPosY = PlayerPosY;
        this.MoveDistance = MoveDistance;
        this.PlayerID = PlayerID;

    }

    public void setPlayerPosX(int PlayerPosX) {
        this.PlayerPosX = PlayerPosX;
    }

    public void setPlayerPosY(int PlayerPosY) {
        this.PlayerPosY = PlayerPosY;
    }

    public void setMoveDistance(int MoveDistance) {
        this.MoveDistance = MoveDistance;
    }

    public void setPlayerID(int PlayerID) {
        this.PlayerID = PlayerID;
    }

    public int getPlayerPosX() {
        return PlayerPosX;
    }

    public int getPlayerPosY() {
        return PlayerPosY;
    }

    public int getMoveDistance() {
        return MoveDistance;
    }

    public int getPlayerID() {
        return PlayerID;
    }
}

