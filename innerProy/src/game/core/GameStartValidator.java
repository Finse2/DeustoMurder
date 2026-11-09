
package game.core;

import model.Actor;
import model.Player;
import model.Room;
import model.Weapon;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static game.core.GameConfig.CARDS_PER_PLAYER;
import static game.core.GameConfig.MAX_PLAYERS;
import static game.core.GameConfig.MIN_PLAYERS;

public class GameStartValidator {

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
        if (players == null || rooms == null || actors == null || weapons == null) {
            return "Game lists cannot be null";
        }
        if (players.size() < MIN_PLAYERS || players.size() > MAX_PLAYERS) {
            return "The number of players must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS;
        }

        Set<Player> uniquePlayers = new HashSet<>();
        for (Player player : players) {
            if (player == null || !uniquePlayers.add(player)) {
                return "Players cannot be null or repeated";
            }
            if (!player.getCards().isEmpty()) {
                return "All players must have empty hands before starting";
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

        // TRES CARTAS SE RESERVAN PARA LA SOLUCIÓN (UNA DE CADA TIPO).
        int availableCards = rooms.size() + actors.size() + weapons.size() - 3;
        int requiredCards = players.size() * CARDS_PER_PLAYER;
        if (availableCards < requiredCards) {
            return "Not enough cards to deal " + CARDS_PER_PLAYER +
                    " to each of " + players.size() + " players: " +
                    "required " + requiredCards + ", available " + availableCards;
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
        return validate(players, rooms, actors, weapons, alreadyInitialized) == null;
    }
}
