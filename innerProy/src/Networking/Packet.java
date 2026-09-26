package Networking;

import java.io.Serializable;

abstract class Packet implements Serializable {

    private static final long serialVersionUID = Server.serialVersionUID;
    private final PacketType type;

    Packet(PacketType type) {
        this.type = type;
    }

    PacketType getPacketType() {
        return type;
    }
}
