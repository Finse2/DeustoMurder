package Networking.common;

import Networking.Server;

import java.io.Serializable;
/**
 * This abstract class represents a packet that is sent between the client and server in a networking environment.
 * It contains a PacketType field that indicates the type of packet being sent.
 * This class is used to facilitate the communication between the client and server in a networking environment.
 */
public abstract class Packet implements Serializable {

    private static final long serialVersionUID = Server.serialVersionUID;
    private final PacketType type;
    /**
     * Constructor for the Packet class.
     *
     * @param type The type of packet being sent.
     */
    protected Packet(PacketType type) {
        this.type = type;
    }
    /**
     * Getter method for the type field.
     *
     * @return The type of packet being sent.
     */
    public PacketType getPacketType() {
        return type;
    }
}
