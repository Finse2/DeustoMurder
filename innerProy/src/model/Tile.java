package model;

/**
 * object that visually represents a tile of the map*/
public class Tile {

    Actor actorOnThisTile = null;

    public Tile(Actor actorOnThisTile) {
        this.actorOnThisTile = actorOnThisTile;
    }
}

