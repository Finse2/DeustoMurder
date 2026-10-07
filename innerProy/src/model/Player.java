package model;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class Player {

    final private Actor self;
    private int playerPosX;
    private int playerPosY;
    private static int nextPlayerID = 0;
    private int playerID;
    private String name;
    private List <Card> cards;
    private List <Card> detectiveNotes;
    private Figure figure;

    public Player(Actor self, List<Card> cards, int playerID, String name) {
        this.self = self;
        cards = new ArrayList<>();
        detectiveNotes = new ArrayList<>();

        this.playerID = nextPlayerID;
        nextPlayerID++;

        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName() {
        this.name = name;
    }

    public void addCard(Card card) {
        cards.add(card);
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

    public int getPlayerID() {return playerID; }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public void setPlayerPosX(int PlayerPosX) {
        this.playerPosX = PlayerPosX;
    }

    public void setPlayerPosY(int PlayerPosY) {
        this.playerPosY = PlayerPosY;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure() {
        this.figure = figure;
    }

    public void addDetectiveNote(Card card) {
        if (!detectiveNotes.contains(card)) {
            detectiveNotes.add(card);
        }
    }

    public void removeDetectiveNote (Card card) {
        detectiveNotes.remove(card);
    }

    public boolean hasDetectiveNote (Card card) {
        return detectiveNotes.contains(card);
    }

    public List<Card> getDetectiveNotes() {
        return detectiveNotes;
    }


}
