package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

import java.io.Serializable;
/*
* The following class is a packet that is sent when a user needs to show a card to another user.
* It contains the card to be shown, the user who is showing the card, the target user who will receive the card,
* and the target user's ID. This packet is used to facilitate the communication between users in the networking environment.
* */
public final class CardShowPacket extends Packet {
    private static final long serialVersionUID = 1L;

    private final String user;
    private final String targetUser;
    private final int targetUserID;
    private final Serializable showCard;

    // Constructor for the CardShowPacket class.
    public CardShowPacket(Serializable showCard, String user, String targetUser, int targetUserID) {
        super(PacketType.CARD_SHOW);
        this.showCard = showCard;
        this.user = user;
        this.targetUser = targetUser;
        this.targetUserID = targetUserID;
    }

    // Getter method for the showCard field.
    public Serializable getShowCard() {
        return showCard;
    }

    // Getter method for the user field.
    public String getUser() {
        return user;
    }
    // Getter method for the targetUser field.
    public String getTargetUser() {
        return targetUser;
    }
    // Getter method for the targetUserID field.
    public int getTargetUserID() {
        return targetUserID;
    }
}
