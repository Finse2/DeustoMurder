package game.core;

import game.accusations.AccusationValidator;
import game.cards.DeckManager;
import game.dice.Dice;
import game.dice.GameRandomizer;
import game.history.GameEvent;
import game.history.GameHistory;
import game.statistics.StatisticsManager;
import game.turns.MoveValidator;
import game.turns.RoundManager;

import model.Actor;
import model.Card;
import model.Piece;
import model.Player;
import model.Room;
import model.Weapon;

import ui.match.BoardLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {

    // INICIALIZACIÓN

    private final GameInitializer initializer;

    // ELEMENTOS DEL JUEGO

    private List<Room> rooms;
    private List<Actor> actors;
    private List<Weapon> weapons;

    // CARTAS

    private List<Card> solutionList;
    private List<Card> deck;
    private List<Card> clues;

    // JUGADORES

    private final List<Player> players;

    private Player currentPlayer;

    // COMPONENTES DE LA PARTIDA

    private GameState gameState;

    private RoundManager roundManager;

    private MoveValidator moveValidator;

    private AccusationValidator accusationValidator;

    private BoardLayout board;

    private DeckManager deckManager;

    private final Dice dice;

    // HISTORIAL Y ESTADÍSTICAS

    private final GameHistory gameHistory;

    private final StatisticsManager statisticsManager;

    // ESTADO GENERAL

    private boolean initialized;

    private boolean gameIsOver;


    // CONSTRUCTOR

    public Game() {

        initializer = new GameInitializer();

        dice = new Dice(new GameRandomizer());

        gameHistory = new GameHistory();

        statisticsManager = new StatisticsManager();

        players = new ArrayList<>();

        rooms = new ArrayList<>();
        actors = new ArrayList<>();
        weapons = new ArrayList<>();

        solutionList = new ArrayList<>();
        deck = new ArrayList<>();
        clues = new ArrayList<>();

        initialized = false;
        gameIsOver = false;
    }


    // INICIALIZAR PARTIDA

    public GameStartResult startGame() {

        return initializer.initialize(this);
    }


    // CONFIGURAR LOS COMPONENTES DESPUÉS DE INITIALIZE

    void configure(
            List<Room> rooms,
            List<Actor> actors,
            List<Weapon> weapons,
            List<Card> solution,
            List<Card> deck,
            List<Card> clues,
            GameState gameState,
            RoundManager roundManager,
            MoveValidator moveValidator,
            AccusationValidator accusationValidator
    ) {

        this.rooms = new ArrayList<>(rooms);

        this.actors = new ArrayList<>(actors);

        this.weapons = new ArrayList<>(weapons);

        this.solutionList = new ArrayList<>(solution);

        this.deck = new ArrayList<>(deck);

        this.clues = new ArrayList<>(clues);

        this.gameState = gameState;

        this.roundManager = roundManager;

        this.moveValidator = moveValidator;

        this.accusationValidator = accusationValidator;

        this.currentPlayer = roundManager.getCurrentPlayer();

        this.initialized = true;

        this.gameIsOver = false;
    }


    // AÑADIR JUGADOR

    public void addPlayer(Player player) {

        if (initialized) {
            throw new IllegalStateException(
                    "Players cannot be added after starting the game"
            );
        }

        if (player == null) {
            throw new IllegalArgumentException(
                    "Player cannot be null"
            );
        }

        if (players.contains(player)) {
            throw new IllegalArgumentException(
                    "Player already exists"
            );
        }

        players.add(player);
    }


    // CONFIGURAR TABLERO

    public void setBoard(BoardLayout board) {

        if (initialized) {
            throw new IllegalStateException(
                    "Board cannot be changed after initialization"
            );
        }

        this.board = board;
    }


    // LANZAR DADOS

    public int rollDice() {

        if (gameState == null || !gameState.canContinue()) {
            throw new IllegalStateException(
                    "The game is not running"
            );
        }

        return dice.roll();
    }


    // VALIDAR MOVIMIENTO

    public boolean isValidMove(
            Player player,
            Piece piece,
            int destinationX,
            int destinationY
    ) {

        if (moveValidator == null) {
            return false;
        }

        return moveValidator.isValidMove(
                player,
                piece,
                destinationX,
                destinationY
        );
    }


    // EJECUTAR MOVIMIENTO

    public boolean movePiece(
            Player player,
            int destinationX,
            int destinationY
    ) {

        if (board == null || player == null || player.getPiece() == null) {
            return false;
        }

        Piece piece = player.getPiece();

        if (!isValidMove(
                player,
                piece,
                destinationX,
                destinationY
        )) {
            return false;
        }

        int originX = piece.getX();
        int originY = piece.getY();

        board.movePiece(
                piece,
                originX,
                originY,
                destinationX,
                destinationY
        );

        gameHistory.record(
                GameEvent.Type.PLAYER_MOVED,
                player.getPlayerID(),
                player.getName(),
                "Player moved to (" +
                        destinationX + ", " + destinationY + ")"
        );

        return true;
    }


    // COMPROBAR ACUSACIÓN

    public boolean checkAccusation(
            Card room,
            Card actor,
            Card weapon
    ) {

        if (gameState == null || !gameState.canContinue()) {
            return false;
        }

        boolean correct = accusationValidator.isCorrectAccusation(
                room,
                actor,
                weapon,
                solutionList
        );

        Player player = currentPlayer;

        gameHistory.record(
                correct
                        ? GameEvent.Type.ACCUSATION_CORRECT
                        : GameEvent.Type.ACCUSATION_INCORRECT,
                player == null ? null : player.getPlayerID(),
                player == null ? null : player.getName(),
                "Final accusation: " + (correct ? "correct" : "incorrect")
        );

        if (correct && player != null) {

            gameState.setWinner(player);

            gameIsOver = true;

            currentPlayer = null;

            if (roundManager != null) {
                roundManager.stopTurns();
            }
        }

        return correct;
    }


    // PASAR AL SIGUIENTE TURNO

    public void nextTurn() {

        if (gameIsOver || roundManager == null) {
            return;
        }

        roundManager.nextTurn();

        currentPlayer = roundManager.getCurrentPlayer();

        if (currentPlayer != null) {

            gameHistory.record(
                    GameEvent.Type.TURN_STARTED,
                    currentPlayer.getPlayerID(),
                    currentPlayer.getName(),
                    "Turn started"
            );
        }
    }


    // FINALIZAR PARTIDA

    public void closeGame() {

        if (gameIsOver) {
            return;
        }

        gameIsOver = true;

        currentPlayer = null;

        if (gameState != null) {
            gameState.finish();
        }

        if (roundManager != null) {
            roundManager.stopTurns();
        }

        gameHistory.record(
                GameEvent.Type.GAME_FINISHED,
                null,
                null,
                "Game finished"
        );

        if (board != null) {
            board.repaint();
        }
    }


    // OBTENER PIEZAS

    public List<Piece> getPieces() {

        List<Piece> pieces = new ArrayList<>();

        for (Player player : players) {

            if (player.getPiece() != null) {
                pieces.add(player.getPiece());
            }
        }

        return pieces;
    }


    // GETTERS

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public List<Actor> getActors() {
        return Collections.unmodifiableList(actors);
    }

    public List<Weapon> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    public List<Card> getDeck() {
        return Collections.unmodifiableList(deck);
    }

    public List<Card> getClues() {
        return Collections.unmodifiableList(clues);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameIsOver;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public GameState getGameState() {
        return gameState;
    }

    public RoundManager getRoundManager() {
        return roundManager;
    }

    public MoveValidator getMoveValidator() {
        return moveValidator;
    }

    public BoardLayout getBoard() {
        return board;
    }

    public Dice getDice() {
        return dice;
    }

    public GameHistory getGameHistory() {
        return gameHistory;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
}