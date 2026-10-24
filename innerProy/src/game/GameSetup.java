package game;

import model.Actor;
import model.Weapon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Initial data for the existing board preview (not multiplayer or turn setup). */
public final class GameSetup {

    private GameSetup() {
    }

    public static List<Actor> createActors() {
        String[] names = {"Garaizar", "Luka", "David", "Andrada", "Antal", "Bringas"};
        Color[] colors = {
                Color.PINK, Color.MAGENTA, Color.BLUE,
                Color.RED, Color.GREEN, Color.YELLOW
        };

        List<Actor> actors = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            actors.add(new Actor(names[i], colors[i]));
        }
        return actors;
    }

    public static List<Weapon> createWeapons() {
        String[] names = {
                "Machete", "Tiza", "Teclado", "Cable", "Silla", "Destornillador"
        };
        List<Weapon> weapons = new ArrayList<>();
        for (String name : names) {
            weapons.add(new Weapon(name));
        }
        return weapons;
    }
}
