package Networking.packets;


import Networking.common.Packet;
import Networking.common.PacketType;

public class JoinPacket extends Packet {
    private String ComputerName;
    private String UserName;

    public JoinPacket(String computerName, String UserName) {
        super(PacketType.JOIN);

        this.ComputerName = computerName;
        this.UserName = UserName;
    }

    public String getComputerName() {
        return ComputerName;
    }

    public String getUserName() {
        return UserName;
    }
}
