package Networking.packets;


import Networking.common.Packet;
import Networking.common.PacketType;

public class JoinPacket extends Packet {
    private static final long serialVersionUID = 1L;
    private final String computerName;
    private final String userName;

    public JoinPacket(String computerName, String userName) {
        super(PacketType.JOIN);
        this.computerName = computerName;
        this.userName = userName;
    }

    public JoinPacket(String computerName, String userName, int ignoredPlayerID) {
        this(computerName, userName);
    }

    public String getComputerName() {
        return computerName;
    }

    public String getUserName() {
        return userName;
    }
}
