package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;
/**
 * This class represents a packet that is sent when a player moves in the game.
 * It contains the new position of the player and the distance they moved.
 * This packet is used to update the positions of players on the client-side.
 */
public final class PlayerMovePacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final int playerPosX;
    private final int playerPosY;
    private final int moveDistance;
    private final int playerID;

    /**
     * Constructor for the PlayerMovePacket class.
     *
     * @param playerPosX The new X position of the player.
     * @param playerPosY The new Y position of the player.
     * @param moveDistance The distance the player moved.
     * @param playerID The ID of the player who moved.
     */
    public PlayerMovePacket(int playerPosX, int playerPosY, int moveDistance, int playerID) {
        super(PacketType.PLAYER_MOVE);
        this.playerPosX = playerPosX;
        this.playerPosY = playerPosY;
        this.moveDistance = moveDistance;
        this.playerID = playerID;
    }
/**
     * Getter method for the playerPosX field.
     *
     * @return The new X position of the player.
     */
    public int getPlayerPosX() {
        return playerPosX;
    }

    /**
     * Getter method for the playerPosY field.
     *
     * @return The new Y position of the player.
     */
    public int getPlayerPosY() {
        return playerPosY;
    }

    /**
     * Getter method for the moveDistance field.
     *
     * @return The distance the player moved.
     */
    public int getMoveDistance() {
        return moveDistance;
    }

    /**
     * Getter method for the playerID field.
     *
     * @return The ID of the player who moved.
     */
    public int getPlayerID() {
        return playerID;
    }
}
