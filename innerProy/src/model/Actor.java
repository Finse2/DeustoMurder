package model;

import ui.ActorsLook;

import java.awt.*;

public class Actor extends Card {

    final private String name;
    final private Color color;

    public Actor(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName () {
        return this.name;
    }

    public Color getColor() {
        return this.color; 
    }
}
