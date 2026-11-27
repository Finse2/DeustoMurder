package mockClasses;

import activation.Screen.lobby.LobbyRoster;
import game.core.Game;
import game.core.GameSetup;
import game.core.GameStartResult;
import game.history.GameEvent;
import game.turns.DicePathfinder;
import model.Actor;
import model.Card;
import model.Piece;
import model.Player;
import model.Room;
import model.Weapon;
import model.board.BoardFactory;
import model.board.RawPiece;
import model.board.Tile;
import ui.chat.Chat;
import ui.match.BoardLayout;
import ui.match.cardDisplayer.PlayersCardsRenderer;
import ui.match.cardDisplayer.PlayersHandsRendererHeader;
import ui.match.inGameScreen.GameActionsPanel;
import ui.match.inGameScreen.InGameScreen;
import ui.match.AccusationCard;
import ui.NoirToastManager;
import utils.AllPaths.DefaultCardModels.ActorsData;
import utils.AllPaths.DefaultCardModels.RoomsData;
import utils.AllPaths.DefaultCardModels.WeaponsData;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mock de una partida LOCAL. Los cuatro personajes son simulados y la misma
 * persona controla cada uno cuando le toca. No utiliza sockets ni servidores.
 *
 * Secuencia: jugadores se unen -> reparto -> tirada (5 s sin tocar = automatica)
 * -> clic en casilla verde -> boton Pasar turno -> siguiente jugador.
 */
public final class PlayableMatchMock {

    public long getLastInvalidMoveNotification() {
        return lastInvalidMoveNotification;
    }

    public void setLastInvalidMoveNotification(long lastInvalidMoveNotification) {
        this.lastInvalidMoveNotification = lastInvalidMoveNotification;
    }

    private enum Step {
        JOINING,
        WAIT_ROLL,
        ANIMATING,
        MOVING,
        MOVEMENT_ANIMATING,
        FINISHED
    }

    private final Game game = new Game();
    private final List<Player> players = new ArrayList<>();
    private final List<Player> connected = new ArrayList<>();
    private final BoardLayout board;
    private final Chat chat;
    private final PlayersCardsRenderer hand;
    private final InGameScreen screen;
    private final GameActionsPanel actions;
    private Step step = Step.JOINING;
    private int rolledTotal;
    private Map<Point, Integer> reachable = Map.of();
    private Room enteredRoomThisMove;
    private long lastInvalidMoveNotification = 0;
    private Point lastHoveredTile;


    public PlayableMatchMock() {
        List<Actor> actors = GameSetup.createActors().subList(0, 4);
        int[][] startPositions = {{0, 5}, {22, 5}, {22, 15}, {0, 16}};
        RawPiece[] raw = new RawPiece[actors.size()];

        for (int i = 0; i < actors.size(); i++) {
            Player player = new Player(actors.get(i), new ArrayList<>(), i);
            players.add(player);
            game.addPlayer(player);
            raw[i] = new RawPiece(player.getPiece(), startPositions[i][0], startPositions[i][1]);
        }

        board = new BoardLayout(BoardLayout.GRID_ROWS, BoardLayout.GRID_COLUMNS,
                BoardFactory.createRawRooms(), raw);
        game.setBoard(board);
        game.setTurnDurationSeconds(3600); // evita que el temporizador de produccion adelante el mock.

        PlayersHandsRendererHeader[] headers = {
                new PlayersHandsRendererHeader(Actor.class, null),
                new PlayersHandsRendererHeader(Weapon.class),
                new PlayersHandsRendererHeader(Room.class)
        };
        hand = new PlayersCardsRenderer(headers, new Object[0]);
        chat = new Chat(List.of(), players.getFirst().getName());
        screen = new InGameScreen(board, actors, hand, chat, game.getGameHistory(),new LobbyRoster());
        actions = screen.getActionsPanel();
        actions.showMockEndTurnButton(this::nextTurn);
        actions.getDiceButton().setEnabled(false);
        actions.setOnDiceRequested(this::requestRoll);
        actions.setOnDiceRolled(this::diceFinished);
        actions.setOnAccusationConfirmed(this::handleAccusation);
        actions.getAccusationButton().setEnabled(false);
        screen.updateMockPlayers(connected, null);
        installEntranceListener();
        installBoardClicks();
        screen.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent event) { game.closeGame(); }
            @Override public void windowClosing(WindowEvent event) { game.closeGame(); }
        });
        screen.setVisible(true);
        joinPlayersOneByOne();
    }

    private void handleAccusation(
            AccusationCard.AccusationSelection selection
    ) {

        if (game.isGameOver()
                || step != Step.FINISHED) {

            screen.showNotification(
                    NoirToastManager.Type.WARNING,
                    "ACCUSATION UNAVAILABLE",
                    "Finish your movement phase first."
            );

            return;
        }

        Player current = game.getCurrentPlayer();

        if (current == null) return;

        String playerName = current.getName();

        // FIND ORIGINAL ACTOR

        Actor actor = null;

        for (Actor candidate : game.getActors()) {

            if (candidate.getName().equals(
                    selection.actor().getName())) {

                actor = candidate;
                break;
            }
        }

        // FIND ORIGINAL WEAPON

        Weapon weapon = null;

        for (Weapon candidate : game.getWeapons()) {

            if (candidate.getName().equals(
                    selection.weapon().getName())) {

                weapon = candidate;
                break;
            }
        }

        // FIND ORIGINAL ROOM

        Room room = null;

        for (Room candidate : game.getRooms()) {

            if (candidate.getName().equals(
                    selection.room().getName())) {

                room = candidate;
                break;
            }
        }

        // VALIDATE SELECTION

        if (actor == null || weapon == null || room == null
                || !game.getRoundManager().isPlayerTurn(current)) {

            screen.showNotification(
                    NoirToastManager.Type.ERROR,
                    "INVALID ACCUSATION",
                    "The accusation could not be processed."
            );

            return;
        }

        // NOTIFY ACCUSATION

        screen.notifyAccusationMade(playerName);

        // CHECK ACCUSATION

        boolean correct = game.checkAccusation(
                room,
                actor,
                weapon
        );

        // RESULT

        if (correct) {

            screen.notifyAccusationCorrect(playerName);

            chat.addSystemMessage(
                    playerName + " has solved the murder!"
            );

        } else {

            screen.notifyAccusationIncorrect(playerName);

            chat.addSystemMessage(
                    playerName + " made an incorrect accusation."
            );
        }

        // GAME FINISHED

        if (game.isGameOver()) {

            board.setReachableTiles(null);

            reachable = Map.of();

            step = Step.FINISHED;

            actions.getDiceButton().setEnabled(false);

            actions.getEndTurnButton().setEnabled(false);

            actions.getAccusationButton().setEnabled(false);

            return;
        }

        // NEXT PLAYER

        beginTurn();
    }

    /** Simula que cada jugador entra al lobby por separado, con aviso en chat. */
    private void joinPlayersOneByOne() {

        final int[] index = {0};

        Timer joinTimer = new Timer(650, event -> {

            if (!screen.isDisplayable()) {

                ((Timer) event.getSource()).stop();

                return;
            }

            Player player = players.get(index[0]);

            connected.add(player);

            chat.addParticipant(player.getName());

            chat.addSystemMessage(
                    player.getName()
                            + " has joined the investigation."
            );

            screen.updateMockPlayers(connected, null);

            // CONNECTION NOTIFICATION

            screen.notifyPlayerJoined(player.getName());

            index[0]++;

            // ALL PLAYERS CONNECTED

            if (index[0] == players.size()) {

                ((Timer) event.getSource()).stop();

                startGame();

                screen.notifyInvestigationReady(
                        connected.size()
                );
            }
        });

        joinTimer.setInitialDelay(250);

        joinTimer.start();
    }

    private void startGame() {
        GameStartResult result = game.startGame();
        if (!result.isSuccessful()) {
            chat.addSystemMessage("Error al iniciar: " + result.getMessage());
        }

        // El modelo Game y DeckManager reparten 4 cartas al azar por jugador,
        // reservando antes la solucion. Solo agregamos imagenes para el renderer.
        decorateDealtCards();
        for (Player player : players) {
            chat.addSystemMessage("Se han repartido " + player.getCards().size()
                    + " cartas a " + player.getName() + ".");
        }
        screen.getHistoryPanel().bindTimer(game);
        beginTurn();
    }

    private void beginTurn() {
        board.clearPreviewPath();
        lastHoveredTile = null;
        Player current = game.getCurrentPlayer();
        if (current == null) return;
        rolledTotal = 0;
        step = Step.WAIT_ROLL;
        reachable = Map.of();
        board.setReachableTiles(reachable.keySet());
        hand.setCards(current.getCards().toArray());
        screen.updateEvidenceCount(current.getCards().size());
        screen.updateMockPlayers(connected, current);
        screen.getTurnHUD().startTurn(
                current.getName()
        );
        screen.setTitle("DeustoMurder | " + current.getName() + "'s turn");
        actions.getEndTurnButton().setEnabled(false);
        actions.getDiceButton().setEnabled(true);
        actions.getAccusationButton().setEnabled(false);
        chat.addSystemMessage("Turno de " + current.getName()
                + ": agita los dados. Si no haces nada, "
                + "se lanzan automáticamente en 5 s.");

        // Entra en la animacion al comenzar el turno; el propio DiceRollPanel
        // fuerza el lanzamiento con rebotes tras 5 segundos sin raton.
        Timer autoOpen = new Timer(50, event -> {

            Timer timer = (Timer) event.getSource();

            if (!screen.isDisplayable()
                    || game.isGameOver()
                    || step != Step.WAIT_ROLL
                    || game.getCurrentPlayer() != current) {

                timer.stop();

                return;
            }

            if (screen.isTurnTransitionActive()) {

                return;
            }

            timer.stop();

            actions.getDiceButton().setEnabled(true);

            if (!screen.getDiceRollPanel().isVisible()) {

                actions.showDiceOnTable();
            }
        });

        autoOpen.setInitialDelay(250);

        autoOpen.start();
    }

    private void requestRoll() {
        if (step != Step.WAIT_ROLL) return;
        step = Step.ANIMATING;
        actions.getDiceButton().setEnabled(false);
        screen.getDiceRollPanel().prepareRoll();
    }

    private void diceFinished(int first, int second) {

        if (step != Step.ANIMATING) return;

        rolledTotal = first + second;

        screen.getTurnHUD().setDiceResult(first, second);

        Player current = game.getCurrentPlayer();

        Piece piece = current.getPiece();

        reachable = DicePathfinder.distances(
                board,
                piece,
                rolledTotal
        );

        board.setReachableTiles(reachable.keySet());

        // NOIR NOTIFICATION

        screen.showNotification(
                NoirToastManager.Type.INFO,
                "DICE ROLLED",
                current.getName() + " rolled "
                        + first + " + " + second
                        + " = " + rolledTotal + "."
        );

        chat.getHistory().add(
                "Sistema",
                current.getName() + " ha sacado "
                        + first + " + " + second
                        + " = " + rolledTotal
                        + ". Haz clic en una casilla verde para moverte."
        );

        step = Step.MOVING;

        if (reachable.isEmpty()) {

            screen.getTurnHUD().setRemainingMoves(0);

            chat.getHistory().add(
                    "Sistema",
                    "No hay casillas libres alcanzables. Puedes pasar turno."
            );

            screen.showNotification(
                    NoirToastManager.Type.WARNING,
                    "NO AVAILABLE MOVES",
                    "There are no reachable tiles. End your turn."
            );

            step = Step.FINISHED;

            actions.getEndTurnButton().setEnabled(true);
            actions.getAccusationButton().setEnabled(true);
        }
    }

    /** Listener del tablero: al pisar una entrada, salta automaticamente
     *  a la primera casilla azul disponible de la habitacion asociada.
     *  Si no queda espacio, el jugador permanece en la casilla de entrada.
     */
    private void installEntranceListener() {
        board.addEntranceListener(event -> {
            Tile tile = (Tile) event.getSource();
            Piece piece = tile.getPieceOnThisTile();
            Room room = tile.getAccessibleRoom();
            if (piece == null || room == null) return;

            try {
                board.enterRoom(piece, room);
                enteredRoomThisMove = room;
                chat.addSystemMessage(piece.getOwner().getName()
                        + " ha entrado automáticamente en " + room.getName() + ".");
            } catch (IllegalStateException fullRoom) {
                chat.addSystemMessage("No quedan posiciones libres en "
                        + room.getName() + "; la ficha permanece en la entrada.");
            }
        });
    }

    private void installBoardClicks() {

        MouseAdapter click = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {

                if (!SwingUtilities.isLeftMouseButton(event)
                        || event.isShiftDown()) {
                    return;
                }

                Point inBoard = SwingUtilities.convertPoint(
                        (Component) event.getSource(),
                        event.getPoint(),
                        board
                );

                Point destination = board.pixelToTile(inBoard);

                if (destination != null) {
                    selectDestination(destination);
                }
            }

            @Override
            public void mouseMoved(MouseEvent event) {

                Point inBoard = SwingUtilities.convertPoint(
                        (Component) event.getSource(),
                        event.getPoint(),
                        board
                );

                Point destination = board.pixelToTile(inBoard);

                previewDestination(destination);
            }

            @Override
            public void mouseExited(MouseEvent event) {

                Point inBoard = SwingUtilities.convertPoint(
                        (Component) event.getSource(),
                        event.getPoint(),
                        board
                );

                if (!board.contains(inBoard)) {

                    lastHoveredTile = null;

                    board.clearPreviewPath();
                }
            }
        };

        installClickRecursively(board, click);
    }

    private void installClickRecursively(
            Component component,
            MouseAdapter click
    ) {

        component.addMouseListener(click);

        component.addMouseMotionListener(click);

        if (component instanceof Container container) {

            for (Component child : container.getComponents()) {

                installClickRecursively(child, click);
            }
        }
    }

    private void previewDestination(Point destination) {

        if (step != Step.MOVING
                || board.isMovementAnimating()
                || destination == null
                || !reachable.containsKey(destination)) {

            lastHoveredTile = null;

            board.clearPreviewPath();

            return;
        }

        if (destination.equals(lastHoveredTile)) {
            return;
        }

        Player current = game.getCurrentPlayer();

        if (current == null) {

            lastHoveredTile = null;

            board.clearPreviewPath();

            return;
        }

        lastHoveredTile = new Point(destination);

        List<Point> path = DicePathfinder.findPath(
                board,
                current.getPiece(),
                destination,
                rolledTotal
        );

        board.setPreviewPath(path);
    }


    private void selectDestination(Point destination) {

        if (step != Step.MOVING
                || board.isMovementAnimating()) {

            return;
        }

        Integer pathLength = reachable.get(destination);

        // INVALID MOVEMENT

        if (pathLength == null) {

            chat.getHistory().add(
                    "Sistema",
                    "Movimiento rechazado: destino fuera de alcance, ocupado o bloqueado."
            );

            long now = System.currentTimeMillis();

            if (now - lastInvalidMoveNotification >= 2000) {

                screen.showNotification(
                        NoirToastManager.Type.WARNING,
                        "INVALID MOVE",
                        "Select an available highlighted tile."
                );

                lastInvalidMoveNotification = now;
            }

            return;
        }

        // CURRENT PLAYER

        Player current = game.getCurrentPlayer();

        if (current == null || game.isGameOver()) return;

        Piece piece = current.getPiece();

        Point origin = new Point(
                piece.getX(),
                piece.getY()
        );

        // FIND BFS PATH

        List<Point> path = DicePathfinder.findPath(
                board,
                piece,
                destination,
                rolledTotal
        );

        if (path.size() < 2) {

            screen.showNotification(
                    NoirToastManager.Type.ERROR,
                    "PATH NOT FOUND",
                    "The selected destination cannot be reached."
            );

            return;
        }

        // PREPARE MOVEMENT

        enteredRoomThisMove = null;

        step = Step.MOVEMENT_ANIMATING;

        board.clearPreviewPath();

        lastHoveredTile = null;

        actions.getEndTurnButton().setEnabled(false);

        actions.getAccusationButton().setEnabled(false);

        actions.getDiceButton().setEnabled(false);

        board.setReachableTiles(null);

        // START MOVEMENT ANIMATION

        board.animatePieceMovement(
                piece,
                path,
                () -> {

                    if (!screen.isDisplayable()
                            || game.isGameOver()
                            || game.getCurrentPlayer() != current) {

                        return;
                    }

                    finishMovement(
                            current,
                            piece,
                            origin,
                            destination,
                            path.size() - 1
                    );
                }
        );
    }


    private void finishMovement(
            Player current,
            Piece piece,
            Point origin,
            Point destination,
            int pathLength
    ) {

        // EXECUTE REAL MOVEMENT

        board.movePiece(
                piece,
                origin.x,
                origin.y,
                destination.x,
                destination.y
        );

        // CLEAR AVAILABLE MOVEMENTS

        board.setReachableTiles(null);

        reachable = Map.of();

        step = Step.FINISHED;

        // ENABLE GAME ACTIONS

        actions.getEndTurnButton().setEnabled(true);

        actions.getAccusationButton().setEnabled(true);

        screen.getTurnHUD().setRemainingMoves(0);

        // GAME HISTORY

        game.getGameHistory().record(
                GameEvent.Type.PLAYER_MOVED,
                current.getPlayerID(),
                current.getName(),
                "De (" + origin.x + "," + origin.y
                        + ") a (" + piece.getX()
                        + "," + piece.getY()
                        + ") usando " + pathLength
                        + " de " + rolledTotal + " pasos"
        );

        // CHAT HISTORY

        chat.getHistory().add(
                "Sistema",
                current.getName() + " se ha movido "
                        + pathLength + " casillas"
                        + (enteredRoomThisMove != null
                        ? " y esta dentro de "
                        + enteredRoomThisMove.getName()
                        : "")
                        + ". Ahora pulsa Pasar turno."
        );

        // MOVEMENT NOTIFICATIONS

        if (enteredRoomThisMove != null) {

            screen.showNotification(
                    NoirToastManager.Type.SUCCESS,
                    "LOCATION ENTERED",
                    current.getName() + " entered "
                            + enteredRoomThisMove.getName() + "."
            );

        } else {

            screen.showNotification(
                    NoirToastManager.Type.SUCCESS,
                    "MOVEMENT COMPLETED",
                    current.getName() + " moved "
                            + pathLength + " tiles."
            );
        }

        screen.showNotification(
                NoirToastManager.Type.INFO,
                "MOVEMENT PHASE COMPLETE",
                "You can now end the turn."
        );
    }



    private void nextTurn() {
        if (step != Step.FINISHED) return;
        actions.getEndTurnButton().setEnabled(false);
        game.nextTurn();
        beginTurn();
    }

    private void decorateDealtCards() {
        List<Room> rooms = GameSetup.createRooms();
        for (Player player : players) {
            for (Card card : player.getCards()) {
                if (card instanceof Actor actor) {
                    for (ActorsData data : ActorsData.values()) {
                        if (!data.getName().equalsIgnoreCase(actor.getName())) continue;
                        card.setNameToRender(data.getName());
                        card.setRenderImages(data.getMainBackgroundImage(), data.getFrontCircleImage());
                        card.setCardBackGroundColor(data.getColor().darker().darker());
                    }
                } else if (card instanceof Weapon weapon) {
                    for (WeaponsData data : WeaponsData.values()) {
                        if (!data.getName().equalsIgnoreCase(weapon.getName())) continue;
                        card.setNameToRender(data.getName());
                        card.setRenderImages(data.getMainBackgroundImage(), data.getFrontCircleImage());
                        card.setCardBackGroundColor(new Color(60, 35, 25));
                    }
                } else if (card instanceof Room room) {
                    int index = 0;
                    for (int i = 0; i < rooms.size(); i++) {
                        if (rooms.get(i).getName().equals(room.getName())) { index = i; break; }
                    }
                    RoomsData[] images = RoomsData.values();
                    RoomsData data = images[index % images.length];
                    card.setNameToRender(room.getName());
                    card.setRenderImages(data.getMainBackgroundImage(), data.getFrontCircleImage());
                    card.setCardBackGroundColor(new Color(20, 50, 70));
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PlayableMatchMock::new);
    }
}
