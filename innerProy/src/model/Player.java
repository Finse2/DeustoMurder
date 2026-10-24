package model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    final private Actor self;
    private final Piece piece;
    private static int nextPlayerID = 0;
    private int playerID;
    private String name;
    private List<Card> player_cards;
    private final List<Card> detectiveNotes = new ArrayList<>();

    public Player(Actor self, List<Card> player_cards, int playerID) {
        this(self, player_cards, playerID, self == null ? null : self.getName());
    }

    public Player(Actor self, List<Card> player_cards, int playerID, String name) {
        this.self = self;
        this.player_cards = player_cards == null ? new ArrayList<>() : player_cards;
        this.playerID = nextPlayerID;
        this.name = name;
        this.piece = new Piece(this);
        nextPlayerID++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCard(Card card) {
        player_cards.add(card);
    }

    public void moveUP() {
        piece.setPosition(piece.getX(), piece.getY() + 1);
    }

    public void moveDOWN() {
        piece.setPosition(piece.getX(), piece.getY() - 1);
    }

    public void moveLEFT() {
        piece.setPosition(piece.getX() - 1, piece.getY());
    }

    public void moveRIGHT() {
        piece.setPosition(piece.getX() + 1, piece.getY());
    }

    public int getPlayerPosX() {
        return piece.getX();
    }

    public int getPlayerPosY() {
        return piece.getY();
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public void setPlayerPosX(int playerPosX) {
        piece.setPosition(playerPosX, piece.getY());
    }

    public void setPlayerPosY(int playerPosY) {
        piece.setPosition(piece.getX(), playerPosY);
    }

    Actor getSelf() {
        return self;
    }

    public Piece getPiece() {
        return piece;
    }

    public void addDetectiveNote(Card card) {
        if (!detectiveNotes.contains(card)) {
            detectiveNotes.add(card);
        }
    }

    public void removeDetectiveNote(Card card) {
        detectiveNotes.remove(card);
    }

    public boolean hasDetectiveNote(Card card) {
        return detectiveNotes.contains(card);
    }

    public List<Card> getDetectiveNotes() {
        return detectiveNotes;
    }
}
