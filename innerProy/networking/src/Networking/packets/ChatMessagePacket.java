package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

/** Texto enviado entre clientes a través del servidor de la partida. */
public final class ChatMessagePacket extends Packet {

    private final int senderID;
    private final String senderName;
    private final String message;

    public ChatMessagePacket(int senderID, String senderName, String message) {
        super(PacketType.CHAT_MESSAGE);
        this.senderID = senderID;
        this.senderName = senderName;
        this.message = message;
    }

    public int getSenderID() {
        return senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }
}
