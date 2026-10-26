package game.core;

import model.Actor;
import model.Weapon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Initial data for the existing board preview (not multiplayer or turn setup). */
public final class GameSetup {

    // ESTA CLASE IMPLEMENTA:

    /*
    Crear la lista de habitaciones, personajes y armas
    Genera la solución secreta
    Mantiene las listas completas de habitaciones, personajes y armas, sin eliminar de ellas las cartas secretas
    Proporcionar acceso a las listas de elementos creados
    Comprobar que existen suficientes cartas para construir la baraja
    Proporcionar los datos que necesita DeckManager para crear la baraja
    Preparar los elementos necesarios para una nueva partida
    Evitar duplicar habitaciones, personajes o armas al reinicializar el juego
     */

    /*
    No debería implementar: reparto de cartas (DeckManager), control de turnos (RoundManager), validación de
    acusaciones (AccusationValidator) y seguimiento del estado de la partida durante el juego (GameState)
     */

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
