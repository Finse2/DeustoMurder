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

    public void start () {
        if (players.isEmpty()) {
            throw new IllegalStateException("There are no players");
        }

        if (finished) {
            throw new IllegalStateException("The game has finished");
        }

        started = true;
    }

    public void finish() {
        finished = true;
        started = false;
    }

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
