
package game.core;

import model.Actor;
import model.Player;
import model.Room;
import model.Weapon;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameStartValidator {

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 6;

    public String validate(
            List<Player> players,
            List<Room> rooms,
            List<Actor> actors,
            List<Weapon> weapons,
            boolean alreadyInitialized
    ) {
        if (alreadyInitialized) {
            return "The game has already been initialized";
        }

        if (players == null || rooms == null ||
                actors == null || weapons == null) {
            return "Game lists cannot be null";
        }

        if (players.size() < MIN_PLAYERS ||
                players.size() > MAX_PLAYERS) {
            return "The number of players must be between 2 and 6";
        }

        Set<Player> uniquePlayers = new HashSet<>();

        for (Player player : players) {
            if (player == null) {
                return "Players cannot be null";
            }

            if (!uniquePlayers.add(player)) {
                return "Duplicate players are not allowed";
            }
        }

        if (rooms.isEmpty() || rooms.contains(null)) {
            return "Rooms cannot be empty or contain null values";
        }

        if (actors.isEmpty() || actors.contains(null)) {
            return "Actors cannot be empty or contain null values";
        }

        if (weapons.isEmpty() || weapons.contains(null)) {
            return "Weapons cannot be empty or contain null values";
        }

        return null;
    }

    public boolean isValid(
            List<Player> players,
            List<Room> rooms,
            List<Actor> actors,
            List<Weapon> weapons,
            boolean alreadyInitialized
    ) {
        return validate(
                players,
                rooms,
                actors,
                weapons,
                alreadyInitialized
        ) == null;
    }
}
