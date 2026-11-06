package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;
/*
* This class represents a packet that is sent when the game is over.
* It contains a boolean indicating whether the game is over and the name of the winner.
* */
public final class GameOverPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final boolean gameOver;
    private final String winner;

    // Constructor for the GameOverPacket class.
    public GameOverPacket(boolean gameOver, String winner) {
        super(PacketType.GAME_OVER);
        this.gameOver = gameOver;
        this.winner = winner;
    }
    // Getter method for the gameOver field.
    public boolean getGameOver() {
        return gameOver;
    }

    // Getter method for the winner field.
    public String getWinner() {
        return winner;
    }
}
