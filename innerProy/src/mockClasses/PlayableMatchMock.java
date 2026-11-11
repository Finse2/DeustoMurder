package mockClasses;

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
import ui.chat.Chat;
import ui.match.BoardLayout;
import ui.match.cardDisplayer.PlayersCardsRenderer;
import ui.match.cardDisplayer.PlayersHandsRendererHeader;
import ui.match.inGameScreen.GameActionsPanel;
import ui.match.inGameScreen.InGameScreen;
import utils.AllPaths.Resources.DefaultCardModels.ActorsData;
import utils.AllPaths.Resources.DefaultCardModels.RoomsData;
import utils.AllPaths.Resources.DefaultCardModels.WeaponsData;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Component;
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

    private enum Step { JOINING, WAIT_ROLL, ANIMATING, MOVING, FINISHED }

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
        screen = new InGameScreen(board, actors, hand, chat, game.getGameHistory());
        actions = screen.getActionsPanel();
        actions.showMockEndTurnButton(this::nextTurn);
        actions.getDiceButton().setEnabled(false);
        actions.setOnDiceRequested(this::requestRoll);
        actions.setOnDiceRolled(this::diceFinished);
        screen.updateMockPlayers(connected, null);
        installBoardClicks();
        screen.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent event) { game.closeGame(); }
            @Override public void windowClosing(WindowEvent event) { game.closeGame(); }
        });
        screen.setVisible(true);
        joinPlayersOneByOne();
    }

    /** Simula que cada jugador entra al lobby por separado, con aviso en chat. */
    private void joinPlayersOneByOne() {
        final int[] index = {0};
        Timer joinTimer = new Timer(650, event -> {
            Player player = players.get(index[0]);
            connected.add(player);
            chat.addParticipant(player.getName());
            chat.getHistory().add("Sistema", player.getName() + " se ha unido a la partida.");
            screen.updateMockPlayers(connected, null);
            index[0]++;
            if (index[0] == players.size()) {
                ((Timer) event.getSource()).stop();
                startGame();
            }
        });
        joinTimer.setInitialDelay(250);
        joinTimer.start();
    }

    private void startGame() {
        GameStartResult result = game.startGame();
        if (!result.isSuccessful()) {
            chat.getHistory().add("Sistema", "Error al iniciar: " + result.getMessage());
            throw new IllegalStateException(result.getMessage());
        }

        // El modelo Game y DeckManager reparten 4 cartas al azar por jugador,
        // reservando antes la solucion. Solo agregamos imagenes para el renderer.
        decorateDealtCards();
        for (Player player : players) {
            chat.getHistory().add("Sistema", "Se han repartido " + player.getCards().size()
                    + " cartas a " + player.getName() + ".");
        }
        screen.getHistoryPanel().bindTimer(game);
        beginTurn();
    }

    private void beginTurn() {
        Player current = game.getCurrentPlayer();
        if (current == null) return;
        rolledTotal = 0;
        step = Step.WAIT_ROLL;
        reachable = Map.of();
        board.setReachableTiles(reachable.keySet());
        hand.setCards(current.getCards().toArray());
        screen.updateMockPlayers(connected, current);
        screen.setTitle("DeustoMurder | Turno de " + current.getName());
        actions.getEndTurnButton().setEnabled(false);
        actions.getDiceButton().setEnabled(true);
        chat.getHistory().add("Sistema", "Turno de " + current.getName()
                + ": agita los dados. Si no haces nada, se lanzan automaticamente en 5 s.");

        // Entra en la animacion al comenzar el turno; el propio DiceRollPanel
        // fuerza el lanzamiento con rebotes tras 5 segundos sin raton.
        Timer autoOpen = new Timer(250, event -> {
            ((Timer) event.getSource()).stop();
            if (step == Step.WAIT_ROLL && !screen.getDiceRollPanel().isVisible()) {
                actions.showDiceOnTable();
            }
        });
        autoOpen.setRepeats(false);
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
        Player current = game.getCurrentPlayer();
        Piece piece = current.getPiece();
        reachable = DicePathfinder.distances(board, piece, rolledTotal);
        board.setReachableTiles(reachable.keySet());
        chat.getHistory().add("Sistema", current.getName() + " ha sacado " + first
                + " + " + second + " = " + rolledTotal + ". Haz clic en una casilla verde para moverte.");
        step = Step.MOVING;
        if (reachable.isEmpty()) {
            chat.getHistory().add("Sistema", "No hay casillas libres alcanzables. Puedes pasar turno.");
            step = Step.FINISHED;
            actions.getEndTurnButton().setEnabled(true);
        }
    }

    private void installBoardClicks() {
        MouseAdapter click = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (!SwingUtilities.isLeftMouseButton(event)) return;
                Point inBoard = SwingUtilities.convertPoint((Component) event.getSource(), event.getPoint(), board);
                Point destination = board.pixelToTile(inBoard);
                if (destination != null) selectDestination(destination);
            }
        };
        board.addMouseListener(click);
        for (Component component : board.getComponents()) component.addMouseListener(click);
    }

    private void selectDestination(Point destination) {
        if (step != Step.MOVING) return;
        Integer pathLength = reachable.get(destination);
        if (pathLength == null) {
            chat.getHistory().add("Sistema", "Movimiento rechazado: destino fuera de alcance, ocupado o bloqueado.");
            return;
        }

        Player current = game.getCurrentPlayer();
        Piece piece = current.getPiece();
        Point origin = new Point(piece.getX(), piece.getY());
        // El BFS verifica TODO el trayecto antes de actualizar el tablero.
        board.movePiece(piece, origin.x, origin.y, destination.x, destination.y);
        board.setReachableTiles(null);
        reachable = Map.of();
        step = Step.FINISHED;
        actions.getEndTurnButton().setEnabled(true);
        game.getGameHistory().record(GameEvent.Type.PLAYER_MOVED, current.getPlayerID(),
                current.getName(), "De (" + origin.x + "," + origin.y + ") a ("
                        + destination.x + "," + destination.y + ") usando " + pathLength
                        + " de " + rolledTotal + " pasos");
        chat.getHistory().add("Sistema", current.getName() + " se ha movido " + pathLength
                + " casillas. Ahora pulsa Pasar turno.");
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
