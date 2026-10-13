package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;

public final class JoinPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final String computerName;
    private final String userName;

    public JoinPacket(String computerName, String userName) {
        super(PacketType.JOIN);
        this.computerName = computerName;
        this.userName = userName;
    }

    public String getComputerName() {
        return computerName;
    }

    public String getUserName() {
        return userName;
    }
}
