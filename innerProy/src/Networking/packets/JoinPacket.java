package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;
/**
 * This class represents a packet that is sent when a user wants to join the game.
 * It contains the computer name and user name of the user who wants to join the game.
 * This packet is used to facilitate the communication between the client and server when a user wants to join the game.
 */
public final class JoinPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final String computerName;
    private final String userName;
/**
     * Constructor for the JoinPacket class.
     *
     * @param computerName The name of the computer from which the user is joining.
     * @param userName The name of the user who wants to join the game.
     */
    public JoinPacket(String computerName, String userName) {
        super(PacketType.JOIN);
        this.computerName = computerName;
        this.userName = userName;
    }

    /**
     * Getter method for the computerName field.
     *
     * @return The name of the computer from which the user is joining.
     */
    public String getComputerName() {
        return computerName;
    }

    /**
     * Getter method for the userName field.
     *
     * @return The name of the user who wants to join the game.
     */
    public String getUserName() {
        return userName;
    }
}
