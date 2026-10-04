package model;

import java.awt.Color;

public class Actor extends Card {

    final private String name;
    final private Color color;

    public Actor(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}
