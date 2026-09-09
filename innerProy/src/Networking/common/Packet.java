package Networking.common;

import java.io.Serializable;

public abstract class Packet implements Serializable {

    private final PacketType type;

    public Packet(PacketType type) {
        this.type = type;
    }

    public PacketType getPacket() {
        return type;
    }

}
