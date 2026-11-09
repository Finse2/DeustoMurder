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


public class GameInitializer {

    private final GameStartValidator startValidator;
    private final GameRandomizer randomizer;

    public GameInitializer() {
        startValidator = new GameStartValidator();
        randomizer = new GameRandomizer();
    }

    public GameStartResult initialize(Game game) {
        if (game == null) {
            return GameStartResult.failure("Game cannot be null");
        }

        List<Player> players = game.getPlayers();
        List<Room> rooms = GameSetup.createRooms();
        List<Actor> actors = GameSetup.createActors();
        List<Weapon> weapons = GameSetup.createWeapons();

        String error = startValidator.validate(
                players, rooms, actors, weapons, game.isInitialized()
        );
        if (error != null) {
            return GameStartResult.failure(error);
        }

        try {
            // LA SOLUCIÓN SE APARTA DE LA BARAJA, PERO NO DE LOS CATÁLOGOS.
            List<Card> solution = List.of(
                    randomizer.selectRandom(rooms),
                    randomizer.selectRandom(actors),
                    randomizer.selectRandom(weapons)
            );

            List<Card> cards = new ArrayList<>();
            cards.addAll(rooms);
            cards.addAll(actors);
            cards.addAll(weapons);
            cards.removeAll(solution);

            DeckManager deckManager = new DeckManager(cards);
            deckManager.shuffleDeck();

            // SNAPSHOT DEL ORDEN BARAJADO, ANTES DEL REPARTO.
            List<Card> shuffledDeck = deckManager.getDeck();
            deckManager.dealCards(players);
            List<Card> clues = deckManager.getRemainingCards();

            GameState gameState = new GameState(players);
            RoundManager roundManager = new RoundManager(players);
            AccusationValidator accusationValidator = new AccusationValidator();

            BoardLayout board = game.getBoard();
            MoveValidator moveValidator = board == null
                    ? null
                    : new MoveValidator(board, roundManager);

            gameState.start();
            roundManager.startTurns();

            game.configure(
                    rooms, actors, weapons, solution, shuffledDeck, clues,
                    gameState, roundManager, moveValidator, accusationValidator
            );

            game.getGameHistory().record(
                    GameEvent.Type.GAME_STARTED,
                    null,
                    null,
                    "The game has started"
            );

            return GameStartResult.success();
        } catch (RuntimeException exception) {
            return GameStartResult.failure(
                    "Game initialization failed: " + exception.getMessage()
            );
        }
    }
}

