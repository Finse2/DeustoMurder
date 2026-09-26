package Networking;

final class JoinPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final String computerName;
    private final String userName;

    JoinPacket(String computerName, String userName) {
        super(PacketType.JOIN);
        this.computerName = computerName;
        this.userName = userName;
    }

    String getComputerName() {
        return computerName;
    }

    String getUserName() {
        return userName;
    }
}
