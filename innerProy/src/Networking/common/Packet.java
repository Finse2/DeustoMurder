package Networking.common;

import java.io.Serializable;

public abstract class Packet implements Serializable {

    private static final long serialVersionUID = 1L;
    private final PacketType type;

    public Packet(PacketType type) {
        this.type = type;
    }

    public PacketType getPacket() {
        return type;
    }

    public PacketType getPacketType() {
        return type;
    }
}
