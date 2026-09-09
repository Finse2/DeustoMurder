package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

public class GameStartPacket extends Packet {

    private boolean StartGame;

    public GameStartPacket(boolean StartGame) {
        super(PacketType.GAME_START);
        this.StartGame = StartGame;
    }

    public boolean getGameStart() {
        return StartGame;
    }

    public void setStartGame(boolean StartGame) {
        this.StartGame = StartGame;
    }
}
