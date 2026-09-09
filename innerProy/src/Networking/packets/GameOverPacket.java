package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

public class GameOverPacket extends Packet {

    private boolean GameOver;

    public GameOverPacket(boolean GameOver) {
        super(PacketType.GAME_OVER);
        this.GameOver = GameOver;
    }

    public boolean getGameOver() {
        return GameOver;
    }

    public void getGameOver(boolean GameOver) {
        this.GameOver = GameOver;
    }
}


