package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;
import Networking.common.ChatChannel;

import java.util.Objects;

public final class ChatMessagePacket extends Packet {

    private static final long serialVersionUID = 1L;

    private final int senderID;
    private final String senderName;
    private final String message;

    private final ChatChannel channel;
    private final String recipient;

    // GENERAL MESSAGE CONSTRUCTOR

    public ChatMessagePacket(
            int senderID,
            String senderName,
            String message
    ) {

        this(
                senderID,
                senderName,
                message,
                ChatChannel.GENERAL,
                null
        );
    }

    // COMPLETE CONSTRUCTOR

    public ChatMessagePacket(
            int senderID,
            String senderName,
            String message,
            ChatChannel channel,
            String recipient
    ) {

        super(PacketType.CHAT_MESSAGE);

        this.senderID = senderID;
        this.senderName = senderName;
        this.message = message;
        this.channel = Objects.requireNonNull(channel);
        this.recipient = recipient;
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

    public ChatChannel getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }
}