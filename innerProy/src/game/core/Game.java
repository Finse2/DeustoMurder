package game.core;

import game.accusations.AccusationValidator;
import game.cards.DeckManager;
import game.dice.Dice;
import game.dice.GameRandomizer;
import game.history.GameEvent;
import game.history.GameEventProcessorThread;
import game.history.GameHistory;
import game.history.GameHistoryWriterThread;
import game.statistics.StatisticsManager;
import game.turns.MoveValidator;
import game.turns.RoundManager;
import game.turns.TurnTimerThread;

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
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

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

    private GameHistoryWriterThread historyWriter;

    // TEMPORIZADOR: un hilo independiente por turno, configurable antes de iniciar.
    private static final int DEFAULT_TURN_SECONDS = 60;
    private int turnDurationSeconds = DEFAULT_TURN_SECONDS;
    private TurnTimerThread turnTimer;
    private final List<Consumer<Integer>> turnTimeListeners = new CopyOnWriteArrayList<>();

    // HISTORIAL Y ESTADÍSTICAS

    private final GameHistory gameHistory;

    private final StatisticsManager statisticsManager;

    private GameEventProcessorThread eventProcessor;

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

    public synchronized GameStartResult startGame() {
        GameStartResult result = initializer.initialize(this);
        if (result.isSuccessful() && currentPlayer != null) {
            recordTurnEvent(GameEvent.Type.TURN_STARTED, currentPlayer);
            beginTurnTimer();
        }
        return result;
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

        if (eventProcessor != null) {
            eventProcessor.addEvent(new GameEvent(GameEvent.Type.PLAYER_MOVED,
                    player.getPlayerID(), player.getName(), "Player moved to (" + destinationX + ", " + destinationY + ")"));
        }


        return true;
    }


    // COMPROBAR ACUSACIÓN

    public synchronized boolean checkAccusation(
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
            stopTurnTimer();
            gameHistory.record(GameEvent.Type.GAME_FINISHED, null, null, "Game finished");
            if (historyWriter != null) historyWriter.stopWriter();

            currentPlayer = null;

            if (roundManager != null) {
                roundManager.stopTurns();
            }
        }

        return correct;
    }


    // TEMPORIZADOR DE TURNOS

    /** Duración aplicable a cada turno; configurar antes de iniciar la partida. */
    public synchronized void setTurnDurationSeconds(int seconds) {
        if (seconds <= 0) throw new IllegalArgumentException("Seconds must be positive");
        if (initialized) throw new IllegalStateException("Configure turn time before starting the game");
        turnDurationSeconds = seconds;
    }

    public synchronized int getTurnDurationSeconds() {
        return turnDurationSeconds;
    }

    public synchronized int getTurnRemainingSeconds() {
        return turnTimer == null ? 0 : turnTimer.getRemainingSeconds();
    }

    public void addTurnTimeListener(Consumer<Integer> listener) {
        turnTimeListeners.add(Objects.requireNonNull(listener));
        listener.accept(getTurnRemainingSeconds());
    }

    public void removeTurnTimeListener(Consumer<Integer> listener) {
        turnTimeListeners.remove(listener);
    }

    private void notifyTurnTime(int seconds) {
        for (Consumer<Integer> listener : turnTimeListeners) listener.accept(seconds);
    }

    private synchronized void beginTurnTimer() {
        stopTurnTimer();
        TurnTimerThread timer = new TurnTimerThread(turnDurationSeconds,
                this::notifyTurnTime, () -> timerExpired((TurnTimerThread) Thread.currentThread()));
        turnTimer = timer;
        timer.start();
    }

    private synchronized void stopTurnTimer() {
        if (turnTimer != null) {
            turnTimer.stopTimer();
            turnTimer = null;
        }
    }

    /** El hilo antiguo no puede avanzar un turno después de que otro lo haya sustituido. */
    private synchronized void timerExpired(TurnTimerThread expiredTimer) {
        if (turnTimer == expiredTimer && !gameIsOver) nextTurn();
    }

    public synchronized void pauseTurnTimer() {
        if (turnTimer != null) turnTimer.pauseTimer();
    }

    public synchronized void resumeTurnTimer() {
        if (turnTimer != null) turnTimer.resumeTimer();
    }

    private void recordTurnEvent(GameEvent.Type type, Player player) {
        gameHistory.record(type, player.getPlayerID(), player.getName(),
                type == GameEvent.Type.TURN_STARTED ? "Turn started" : "Turn ended");
    }

    // PASAR AL SIGUIENTE TURNO (manual o por timeout)

    public synchronized void nextTurn() {
        if (gameIsOver || roundManager == null || !roundManager.areTurnsActive()) return;

        stopTurnTimer();
        if (currentPlayer != null) recordTurnEvent(GameEvent.Type.TURN_ENDED, currentPlayer);
        roundManager.nextTurn();
        currentPlayer = roundManager.getCurrentPlayer();

        if (currentPlayer != null) {
            recordTurnEvent(GameEvent.Type.TURN_STARTED, currentPlayer);
            beginTurnTimer();
        }
    }


    // FINALIZAR PARTIDA

    public synchronized void closeGame() {

        if (gameIsOver) {
            return;
        }

        gameIsOver = true;
        stopTurnTimer();

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

        if (historyWriter != null) historyWriter.stopWriter();

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

    public synchronized void startHistoryWriter() {
        if (historyWriter != null && historyWriter.isAlive()) return;
        historyWriter = new GameHistoryWriterThread(gameHistory,
                java.nio.file.Path.of("data/history/game-history.csv"));
        historyWriter.setDaemon(true);
        historyWriter.start();
    }

    public void startEventProcessor() {

        eventProcessor = new GameEventProcessorThread();

        eventProcessor.start();
    }

    public void stopEventProcessor() {

        if (eventProcessor != null) {

            eventProcessor.stopProcessor();

            eventProcessor = null;
        }
    }
}