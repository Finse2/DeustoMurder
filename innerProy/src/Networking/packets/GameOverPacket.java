package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

public class GameOverPacket extends Packet {
    private static final long serialVersionUID = 1L;

    private final boolean gameOver;
    private final String winner;

    public GameOverPacket(boolean GameOver, String winner) {
        super(PacketType.GAME_OVER);
        this.gameOver = GameOver;
        this.winner = winner;
    }

    public String getWinner() {
        return winner;
    }

    public boolean getGameOver() {
        return gameOver;
    }
}

