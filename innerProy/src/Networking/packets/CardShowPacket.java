package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;
import model.Card;

import java.util.List;

public class CardShowPacket extends Packet {

    private String User;
    private Card showCard;

    public CardShowPacket(Card showCard, String User) {
        super(PacketType.CARD_SHOW);
        this.showCard = showCard;
        this.User = User;
    }

    public Card getShowCard() {
        return showCard;
    }

    public void setShowCard(Card showCard) {
        this.showCard = showCard;
    }

    public String getUser() {
        return User;
    }

    public void setUser() {
        this.User = User;
    }
}