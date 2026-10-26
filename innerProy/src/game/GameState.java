package game;

import model.Player;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    private final List<Player> players;
    private boolean started;
    private boolean finished;

    public GameState (List <Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("Players cannot be null");
        }

        this.players = new ArrayList<>(players);
        this.started = false;
        this.finished = false;
    }

    // MÉTODO PARA EMPEZAR LA PARTIDA
    public void start () {
        if (players.isEmpty()) {
            throw new IllegalStateException("There are no players");
        }

        if (finished) {
            throw new IllegalStateException("The game has finished");
        }

        started = true;
    }

    // MÉTODO PARA FINALIZAR LA PARTIDA
    public void finish() {
        finished = true;
        started = false;
    }

    // OTROS MÉTODOS APARTE
    public boolean isStarted(){
        return started;
    }

    public boolean isFinished(){
        return finished;
    }

    public boolean canContinue() {
        return started && !finished;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

}
