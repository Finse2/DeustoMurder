package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;
/*
* This class represents a packet that is sent when a player joins the game.
* It contains the player ID, player name, and computer name of the player who joined.
* This packet is used to inform other players of the new player's arrival.
*/
public final class PlayerJoinedPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final int playerID;
    private final String playerName;
    private final String computerName;

    /**
     * Constructor for the PlayerJoinedPacket class.
     *
     * @param playerID The ID of the player who joined.
     * @param playerName The name of the player who joined.
     * @param computerName The name of the computer from which the player joined.
     */
    public PlayerJoinedPacket(int playerID, String playerName, String computerName) {
        super(PacketType.PLAYER_JOINED);
        this.playerID = playerID;
        this.playerName = playerName;
        this.computerName = computerName;
    }
// Getter methods for the playerID, playerName, and computerName fields.
    public int getPlayerID() {
        return playerID;
    }
// Getter method for the playerName field.
    public String getPlayerName() {
        return playerName;
    }
// Getter method for the computerName field.
    public String getComputerName() {
        return computerName;
    }
}
