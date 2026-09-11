package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;
import model.Card;

public class CardShowPacket extends Packet {
    private static final long serialVersionUID = 1L;

    private final String user;
    private final String targetUser;
    private final int targetUserID;
    private final Card showCard;

    public CardShowPacket(Card showCard, String User, String targetUser, int targetUserID) {
        super(PacketType.CARD_SHOW);
        this.showCard = showCard;
        this.user = User;
        this.targetUser = targetUser;
        this.targetUserID = targetUserID;

    }

    public Card getShowCard() {
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