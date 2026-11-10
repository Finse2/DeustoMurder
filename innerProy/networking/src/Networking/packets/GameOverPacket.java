package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;


public final class GameOverPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final boolean gameOver;
    private final String winner;
    private final int winnerID;
    private final String murderer;
    private final String weapon;
    private final String room;

    public GameOverPacket(boolean gameOver, String winner) {
        super(PacketType.GAME_OVER);
        this.gameOver = gameOver;
        this.winner = winner;
        this.winnerID = -1;
        this.murderer = null;
        this.weapon = null;
        this.room = null;
    }

    public GameOverPacket(int winnerID, String winnerName,
                          String murderer, String weapon, String room) {
        super(PacketType.GAME_OVER);
        if (winnerID < 0) {
            throw new IllegalArgumentException("Winner ID cannot be negative");
        }
        this.gameOver = true;
        this.winnerID = winnerID;
        this.winner = requireText(winnerName, "Winner name");
        this.murderer = requireText(murderer, "Murderer");
        this.weapon = requireText(weapon, "Weapon");
        this.room = requireText(room, "Room");
    }

    private static String requireText(String text, String fieldName) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return text.trim();
    }

    public boolean getGameOver() {
        return gameOver;
    }

    public String getWinner() {
        return winner;
    }


    public int getWinnerID() {
        return winnerID;
    }

    public String getWinnerName() {
        return winner;
    }

    public String getMurderer() {
        return murderer;
    }

    public String getWeapon() {
        return weapon;
    }

    public String getRoom() {
        return room;
    }
}
