package game.core;

import model.Player;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    // IMPLEMENTACIONES

    /*
    Guardar si la partida está preparada, iniciada, finalizada o en otro estado definido por las reglas
    Guardar el jugador que tiene el turno actual
    Guardar la solución correcta
    Mantener el estado de los jugadores, incluyendo eliminaciones o participación
    Registrar si la partida ha terminado
    Guardar el ganador, si existe
    Permitir consultar el estado de la partida
    Actualizar el estado de cuando empieza o termina una partida
    Evitar transiciones inválidas, como iniciar una partida ya finalizada sin reiniciarla por ejemplo
    Permitir restablecer el estado para una nueva partida
     */

    /*
    No debería implementar: creación de las habitaciones, personajes y armas, barajado o reparto de cartas, la lógica
    de validación de acusaciones, la ejecución completa de los turnos o el registro de acontecimiento (GameHistory)
     */

    public enum Status {
            READY, RUNNING, FINISHED
}

    private final List<Player> players;
    private Status status;
    private Player winner;

    public GameState (List <Player> players) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players cannot be null");
        }

        this.players = new ArrayList<>(players);
        this.status = Status.READY;
        this.winner = null;
    }

    // INICIAR LA PARTIDA

    public void start () {
        if (players.isEmpty()) {
            throw new IllegalStateException("There are no players");
        }

        status = Status.RUNNING;
    }

    // FINALIZAR LA PARTIDA

    public void finish() {
        if (status == Status.FINISHED) {
            return;
        }

        status = Status.FINISHED;
    }

    // REGISTRAR GANADOR

    public void setWinner (Player winner) {
        if (status != Status.RUNNING) {
            throw new IllegalStateException("The game is not running");
        }

        if (winner != null && !players.contains(winner)) {
            throw new IllegalArgumentException("Winner must belong to the game");
        }

        this.winner = winner;

        finish();
    }

    // CONSULTAR ESTADO

    public Status getStatus() {
        return status;
    }

    public boolean isStarted() {
        return status == Status.RUNNING;
    }

    public boolean isFinished() {
        return status == Status.FINISHED;
    }

    public boolean canContinue() {
        return status == Status.RUNNING;
    }

    public Player getWinner() {
        return winner;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

}
