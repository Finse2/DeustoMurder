package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;

public final class GameOverPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final boolean gameOver;
    private final String winner;

    public GameOverPacket(boolean gameOver, String winner) {
        super(PacketType.GAME_OVER);
        this.gameOver = gameOver;
        this.winner = winner;
    }

    public boolean getGameOver() {
        return gameOver;
    }

    public String getWinner() {
        return winner;
    }
}
