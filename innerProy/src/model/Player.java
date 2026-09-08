package model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    final private Actor self;
    private int playerPosX;
    private int playerPosY;

    private List<Card> player_cards = new ArrayList<>();

    public Player(Actor self, List<Card> player_cards) {
        this.self = self;
        this.player_cards = player_cards;
    }
    public void addCard(Card card) {
        player_cards.add(card);
    }

    public void moveUP() {
        playerPosY++;
    }

    public void moveDOWN() {
        playerPosY--;
    }

    public void moveLEFT() {
        playerPosX--;
    }

    public void moveRIGHT() {
        playerPosX++;
    }

    public int getPlayerPosX() {
        return playerPosX;
    }

    public int getPlayerPosY() {
        return playerPosY;
    }

    public void setPlayerPosX(int PlayerPosX) {
        this.playerPosX = PlayerPosX;
    }

    public void setPlayerPosY(int PlayerPosY) {
        this.playerPosY = PlayerPosY;
    }


}
