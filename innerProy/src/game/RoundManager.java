package game;

import model.Player;

import java.util.ArrayList;
import java.util.List;

public class RoundManager {

    private final List<Player> players;
    private int currentPlayerIndex;
    private boolean turnsStarted;

    public RoundManager(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("Players cannot be null");
        }

        this.players = new ArrayList<>(players);
        this.currentPlayerIndex = 0;
        this.turnsStarted = false;
    }

    // MÉTODO PARA COMENTAR LOS TURNOS
    public void startTurns() {
        if (players.isEmpty()) {
            throw new IllegalStateException("There are no players");
        }

        currentPlayerIndex = 0;
        turnsStarted = true;
    }

    // MÉTODO PARA CONSEGUIR EL JUGADOR ACTUAL
    public Player getCurrentPlayer (){
        if (!turnsStarted || players.isEmpty()) {
            return null;
        }

        return players.get(currentPlayerIndex);
    }

    public boolean isPlayerTurn (Player player) {
        return turnsStarted && player != null && getCurrentPlayer() == player;
    }

    public void nextTurn () {
        if (!turnsStarted || players.isEmpty()) {
            return;
        }

        currentPlayerIndex++;

        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
    }

    public void stopTurns() {
        turnsStarted = false;
    }

    public boolean areTurnsStarted() {
        return turnsStarted;
    }

}
