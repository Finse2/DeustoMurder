package game.core;

import model.Actor;
import model.Room;
import model.Weapon;

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

    public static List<Room> createRooms () {
        List<Room> rooms = new ArrayList<>();

        rooms.add(new Room ("CRAI").occupyRectangle(0,0,7,4));
        rooms.add(new Room ("Baños").occupyRectangle(9,0,5,6));
        rooms.add(new Room ("Cafteria").occupyRectangle(16,0,7,5));

        Room maquinas = new Room("Maquinas").occupyRectangle(0,6,7,4);
        maquinas.free(6,6);
        maquinas.free(6,9);

        rooms.add(maquinas);
        rooms.add(new Room ("Laboratorio").occupyRectangle(9,7,5,6));

        Room deustoTech = new Room ("DeustoTech").occupyRectangle(16,7,7,7);

        deustoTech.free(16,13);
        deustoTech.free(17,13);
        rooms.add(deustoTech);

        rooms.add(new Room ("Aula 1").occupyRectangle(0,11,6,4));

        Room aula2 = new Room ("Aula 2").occupyRectangle(0,17,6,5);

        aula2.free(0,17);
        aula2.free(5,17);

        rooms.add(aula2);

        Room room9 = new Room ("Room 9").occupyRectangle(8,15,7,7);
        room9.free(8,21);
        room9.free(14,21);
        rooms.add(room9);

        rooms.add(
                new Room("Room 10")
                        .occupyRectangle(17, 16, 6, 6)
        );

        return rooms;

    }

    // CREAR PERSONAJES

    public static List<Actor> createActors() {
        List<Actor> actors = new ArrayList<>();

        for (int i = 0; i < GameConfig.ACTOR_NAMES.size(); i++) {
            actors.add(
                    new Actor(
                            GameConfig.ACTOR_NAMES.get(i),
                            GameConfig.PLAYER_COLORS.get(i)
                    )
            );
        }

        return actors;
    }

    public static List<Weapon> createWeapons () {
        List<Weapon> weapons = new ArrayList<>();

        for (String name : GameConfig.WEAPON_NAMES) {
            weapons.add(new Weapon(name));
        }

        return weapons;
    }
}
