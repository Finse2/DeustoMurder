package Networking.common;

import Networking.Server;

import java.io.Serializable;

public abstract class Packet implements Serializable {

    private static final long serialVersionUID = Server.serialVersionUID;
    private final PacketType type;

    protected Packet(PacketType type) {
        this.type = type;
    }

    public PacketType getPacketType() {
        return type;
    }
}
