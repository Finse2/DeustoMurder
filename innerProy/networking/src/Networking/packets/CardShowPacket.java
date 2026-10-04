package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

import java.io.Serializable;

public final class CardShowPacket extends Packet {
    private static final long serialVersionUID = 1L;

    private final String user;
    private final String targetUser;
    private final int targetUserID;
    private final Serializable showCard;

    public CardShowPacket(Serializable showCard, String user, String targetUser, int targetUserID) {
        super(PacketType.CARD_SHOW);
        this.showCard = showCard;
        this.user = user;
        this.targetUser = targetUser;
        this.targetUserID = targetUserID;
    }

    public Serializable getShowCard() {
        return showCard;
    }

    public String getUser() {
        return user;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public int getTargetUserID() {
        return targetUserID;
    }
}
