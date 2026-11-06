package game.turns;

import model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoundManager {

    private final List<Player> players;
    private int currentPlayerIndex;
    private boolean turnsActive;

    public RoundManager(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException(
                    "The player list cannot be null"
            );
        }

        this.players = new ArrayList<>(players);
        this.currentPlayerIndex = 0;
        this.turnsActive = false;
    }

    // INICIAR TURNOS

    public void startTurns() {
        currentPlayerIndex = 0;
        turnsActive = true;
    }

    // OBTENER EL JUGADOR ACTUAL

    public Player getCurrentPlayer() {
        if (!turnsActive || players.isEmpty()) {
            return null;
        }

        return players.get(currentPlayerIndex);
    }

    // COMPROBAR TURNO

    public boolean isPlayerTurn(Player player) {
        return turnsActive
                && player != null
                && getCurrentPlayer() == player;
    }

    // SIGUIENTE TURNO

    public void nextTurn() {
        if (!turnsActive || players.isEmpty()) {
            return;
        }

        currentPlayerIndex =
                (currentPlayerIndex + 1) % players.size();
    }

    // DETENER TURNOS

    public void stopTurns() {

        turnsActive = false;
    }

    // GETTERS

    public boolean areTurnsActive() {

        return turnsActive;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public List<Player> getPlayers() {
        
        return Collections.unmodifiableList(players);
    }
}
