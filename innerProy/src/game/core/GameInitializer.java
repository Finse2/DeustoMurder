package game.core;

import game.accusations.AccusationValidator;
import game.cards.DeckManager;
import game.dice.GameRandomizer;
import game.history.GameEvent;
import game.turns.MoveValidator;
import game.turns.RoundManager;
import model.*;
import ui.match.BoardLayout;

import java.util.ArrayList;
import java.util.List;

public class GameInitializer {

    // ESTA CLASE SE ENCARGA DE:

    /*
    Coordinar el proceso de inicialización
    Solicitar la validación de los requisitos iniciales
    Preparar las habitaciones, los personajes y las armas mediante GameSetup
    Inicializar a los jugadores y sus posiciones
    Generar o recuperar la solución secreta
    Crear y barajar la baraja mediante DeckManager
    Repartir las cartas a los jugadores
    Guardar las cartas sobrantes
    Inicializar el estado de la partida
    Establecer el primer jugador y preparar el primer turno
    Registrar el evento de inicio
    Devolver un resultado que indique si la inicialización ha sido correcta
     */

    private final GameStartValidator startValidator;
    private final GameRandomizer randomizer;

    public GameInitializer() {
        this.startValidator = new GameStartValidator();
        this.randomizer = new GameRandomizer();
    }

    // INICIALIZAR UNA PARTIDA

    public GameStartResult initialize(Game game) {
        if (game == null) {
            return GameStartResult.failure("Game cannot be null");
        }

        // OBTENER LOS JUGADORES

        List<Player> players = game.getPlayers();

        // CREAR LOS ELEMENTOS DEL JUEGO

        List <Room> rooms = GameSetup.createRooms();
        List <Actor> actors = GameSetup.createActors();
        List <Weapon> weapons = GameSetup.createWeapons();

        // VALIDAR LA PARTIDA

        String error = startValidator.validate(
                players,
                rooms,
                actors,
                weapons,
                game.isInitialized()
        );

        if (error != null) {
            return GameStartResult.failure(error);
        }

        try {
            List<Card> solution = new ArrayList<>();

            Room solutionRoom = randomizer.selectRandom(rooms);
            Actor solutionActor = randomizer.selectRandom(actors);
            Weapon solutionWeapon = randomizer.selectRandom(weapons);

            solution.add(solutionRoom);
            solution.add(solutionActor);
            solution.add(solutionWeapon);

            rooms.remove(solutionRoom);
            actors.remove(solutionActor);
            weapons.remove(solutionWeapon);
            
            // CONSTRUIR LA BARAJA
            
            List<Card> cards = new ArrayList<>();
            
            cards.addAll(rooms);
            cards.addAll(actors);
            cards.addAll(weapons);
            
            DeckManager deckManager = new DeckManager(cards);
            
            // BARAJAR
            
            randomizer.shuffle(cards);
            
            // REPARTIR LAS CARTAS
            
            deckManager.dealCards(players);
            
            // GUARDAR LAS CARTAS SOBRANTES
            
            List<Card> clues = deckManager.getRemainingCards();
            
            // CREAR EL ESTADO DE LA PARTIDA
            
            GameState gameState = new GameState(players);
            
            // CREAR EL CONTROL DE TURNOS 

            RoundManager roundManager = new RoundManager(players);
            
            // CREAR LOS VALIDADORES

            AccusationValidator accusationValidator = new AccusationValidator();
            
            BoardLayout board = game.getBoard();

            MoveValidator moveValidator = null;
            
            if (board != null) {
                moveValidator = new MoveValidator(board, roundManager);
            }
            
            // INICIAR EL ESTADO 
            
            gameState.start();
            roundManager.startTurns();
            
            // GUARDAR LOS DATOS DE GAME 
            
            game.configure(
                    rooms,actors,weapons,solution, cards, clues, gameState, roundManager, moveValidator, accusationValidator 
            );
            
            // REGISTRAR EL EVENETO 
            
            game.getGameHistory().record(GameEvent.Type.GAME_STARTED, null, null, "The game has started");
            
            return GameStartResult.success();
        } catch (RuntimeException exception) {
            return GameStartResult.failure("Game initialization failed: " + exception.getMessage());
        }


    }

}
