package model;

import java.awt.Color;

public class Piece {

    private final Player owner;
    private int x;
    private int y;

    Piece(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Color getColor() {
        return owner.getSelf().getColor();
    }
}
