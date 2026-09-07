package model;

import ui.ActorsLook;

public class Actor {

    final private String name;
    final private ActorsLook look;

    public Actor(String name, ActorsLook look) {
        this.name = name;
        this.look = look;
    }
}
