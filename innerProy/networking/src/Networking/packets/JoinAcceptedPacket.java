package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

/**
 * This class represents a packet that is sent when a user successfully joins The game.
 * It contains the player ID assigned to the user by the server.
 * This packet is used to inform the user of their player ID after they have joined the game.
 */
public final class JoinAcceptedPacket extends Packet {
    private static final long serialVersionUID = 1L;

    private final int playerID;

    /**
     * Constructor for the JoinAcceptedPacket class.
     *
     * @param playerID The player ID assigned to the user by the server.
     */
    public JoinAcceptedPacket(int playerID) {
        super(PacketType.JOIN_ACCEPTED);
        this.playerID = playerID;
    }
/**
     * Getter method for the playerID field.
     *
     * @return The player ID assigned to the user by the server.
     */
    public int getPlayerID() {
        return playerID;
    }
}
