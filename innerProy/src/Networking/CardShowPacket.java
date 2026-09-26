package Networking;

import model.Card;

final class CardShowPacket extends Packet {
    private static final long serialVersionUID = 1L;

    private final String user;
    private final String targetUser;
    private final int targetUserID;
    private final Card showCard;

    CardShowPacket(Card showCard, String user, String targetUser, int targetUserID) {
        super(PacketType.CARD_SHOW);
        this.showCard = showCard;
        this.user = user;
        this.targetUser = targetUser;
        this.targetUserID = targetUserID;
    }

    Card getShowCard() {
        return showCard;
    }

    String getUser() {
        return user;
    }

    String getTargetUser() {
        return targetUser;
    }

    int getTargetUserID() {
        return targetUserID;
    }
}
