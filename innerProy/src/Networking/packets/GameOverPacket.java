
package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;
import Networking.match.NetworkMatchResult;

import java.util.Objects;
import java.util.UUID;

public final class GameOverPacket extends Packet {

    private static final long serialVersionUID =
            Server.serialVersionUID;

    private final boolean gameOver;
    private final String winner;
    private final int winnerID;

    private final String murderer;
    private final String weapon;
    private final String room;

    private final UUID matchId;

    private final NetworkMatchResult matchResult;

    // LEGACY COMPLETION REQUEST

    public GameOverPacket(
            boolean gameOver,
            String winner
    ) {

        super(PacketType.GAME_OVER);

        this.gameOver = gameOver;
        this.winner = winner;
        this.winnerID = -1;

        this.murderer = null;
        this.weapon = null;
        this.room = null;

        this.matchId = null;
        this.matchResult = null;
    }

    // LEGACY DETAILED RESULT

    public GameOverPacket(
            int winnerID,
            String winnerName,
            String murderer,
            String weapon,
            String room
    ) {

        super(PacketType.GAME_OVER);

        if (winnerID < 0) {

            throw new IllegalArgumentException(
                    "Winner ID cannot be negative."
            );
        }

        this.gameOver = true;
        this.winnerID = winnerID;

        this.winner = requireText(
                winnerName,
                "Winner name"
        );

        this.murderer = requireText(
                murderer,
                "Murderer"
        );

        this.weapon = requireText(
                weapon,
                "Weapon"
        );

        this.room = requireText(
                room,
                "Room"
        );

        this.matchId = null;
        this.matchResult = null;
    }

    // MATCH-AWARE COMPLETION REQUEST

    public GameOverPacket(
            UUID matchId,
            int winnerID,
            String winnerName,
            String murderer,
            String weapon,
            String room
    ) {

        super(PacketType.GAME_OVER);

        if (winnerID < 0) {

            throw new IllegalArgumentException(
                    "Winner ID cannot be negative."
            );
        }

        this.gameOver = true;

        this.matchId =
                Objects.requireNonNull(matchId);

        this.winnerID = winnerID;

        this.winner = requireText(
                winnerName,
                "Winner name"
        );

        this.murderer = murderer;
        this.weapon = weapon;
        this.room = room;

        this.matchResult = null;
    }

    // AUTHORITATIVE SERVER RESULT

    public GameOverPacket(
            NetworkMatchResult result
    ) {

        super(PacketType.GAME_OVER);

        this.matchResult =
                Objects.requireNonNull(result);

        this.gameOver = true;

        this.matchId = result.matchId();

        this.winnerID =
                result.getWinnerSessionId();

        this.winner =
                result.getWinnerName();

        this.murderer = result.murderer();

        this.weapon = result.weapon();

        this.room = result.room();
    }

    // VALIDATION

    private static String requireText(
            String text,
            String fieldName
    ) {

        if (text == null || text.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " cannot be blank."
            );
        }

        return text.trim();
    }

    // GETTERS

    public boolean getGameOver() {
        return gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public String getWinnerName() {
        return winner;
    }

    public int getWinnerID() {
        return winnerID;
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

    public UUID getMatchId() {
        return matchId;
    }

    public NetworkMatchResult getMatchResult() {
        return matchResult;
    }

    public boolean hasMatchResult() {
        return matchResult != null;
    }

    public boolean isConfirmed() {

        return gameOver
                && matchId != null
                && matchResult != null
                && matchId.equals(
                matchResult.matchId()
        );
    }
}
