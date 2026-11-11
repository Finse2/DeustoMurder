package mockClasses.testing;

import game.core.Game;
import game.core.GameSetup;
import game.core.GameStartResult;
import game.turns.DicePathfinder;
import model.Actor;
import model.Piece;
import model.Player;
import model.board.BoardFactory;
import model.board.RawPiece;
import model.board.Tile;
import model.Room;
import java.util.concurrent.atomic.AtomicInteger;
import ui.match.BoardLayout;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Pruebas sin interfaz: reparto real y rutas <= dados sin Tile.disabled. */
public final class PlayableMatchMockTest {
    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        List<Actor> actors = GameSetup.createActors().subList(0, 4);
        List<Player> players = new ArrayList<>();
        RawPiece[] raw = new RawPiece[4];
        int[][] starts = {{0, 5}, {22, 5}, {22, 15}, {0, 16}};
        Game game = new Game();
        for (int i = 0; i < starts.length; i++) {
            Player p = new Player(actors.get(i), new ArrayList<>(), i);
            players.add(p);
            game.addPlayer(p);
            raw[i] = new RawPiece(p.getPiece(), starts[i][0], starts[i][1]);
        }
        BoardLayout board = new BoardLayout(BoardLayout.GRID_ROWS, BoardLayout.GRID_COLUMNS,
                BoardFactory.createRawRooms(), raw);
        game.setBoard(board);
        GameStartResult start = game.startGame();
        expect(start.isSuccessful(), start.getMessage());
        for (Player player : players) expect(player.getCards().size() == 4, "Reparto incorrecto");
        System.out.println("OK - 4 jugadores con 4 cartas aleatorias");

        Piece piece = players.getFirst().getPiece();
        Map<Point, Integer> reachable = DicePathfinder.distances(board, piece, 4);
        expect(reachable.containsKey(new Point(1, 5)), "Casilla adyacente no encontrada");
        for (var item : reachable.entrySet()) {
            Point p = item.getKey();
            expect(item.getValue() > 0 && item.getValue() <= 4, "Recorrido excede los dados");
            expect(!board.getTileAt(p.x, p.y).isDisabled(), "Se atraveso una habitacion");
            expect(board.getTileAt(p.x, p.y).getPieceOnThisTile() == null, "Se paso por otra ficha");
        }
        expect(!reachable.containsKey(new Point(1, 1)), "Un muro deberia bloquear");
        expect(DicePathfinder.shortestDistance(board, piece, 10, 10, 4) == -1, "Destino lejano valido");
        System.out.println("OK - BFS evita muros, piezas y limite de dados");

        int distance = reachable.get(new Point(1, 5));
        expect(distance == 1, "Distancia de casilla vecina incorrecta");
        board.movePiece(piece, piece.getX(), piece.getY(), 1, 5);
        expect(piece.getX() == 1 && piece.getY() == 5, "La ficha no se movio");
        expect(board.getTileAt(0, 5).getPieceOnThisTile() == null, "Casilla original aun ocupada");
        expect(board.getTileAt(1, 5).getPieceOnThisTile() == piece, "Casilla destino no ocupada");
        expect(DicePathfinder.shortestDistance(board, piece, 0, 5, 2) == 1, "No se recalculan rutas");
        System.out.println("OK - Movimiento actualiza modelo y rutas");
        // Entrada automática: debe disparar UN evento al llegar, e insertar
        // la ficha en una posición azul libre del cuarto.
        Player visitor = new Player(actors.getFirst(), new ArrayList<>(), 99);
        Piece visitorPiece = visitor.getPiece();
        BoardLayout entranceBoard = new BoardLayout(BoardLayout.GRID_ROWS, BoardLayout.GRID_COLUMNS,
                BoardFactory.createRawRooms(), new RawPiece[]{new RawPiece(visitorPiece, 8, 0)});
        AtomicInteger triggers = new AtomicInteger();
        entranceBoard.addEntranceListener(event -> {
            triggers.incrementAndGet();
            Tile entranceTile = (Tile) event.getSource();
            expect(entranceTile.givesAccessToRoom(), "Notifica tile no entrada");
            expect(entranceTile.getPieceOnThisTile() == visitorPiece, "Pieza ausente al notificar");
            Room room = entranceTile.getAccessibleRoom();
            entranceBoard.enterRoom(visitorPiece, room);
        });
        entranceBoard.movePiece(visitorPiece, 8, 0, 7, 0);
        expect(triggers.get() == 0, "Se notifico una casilla normal");
        entranceBoard.movePiece(visitorPiece, 7, 0, 7, 1);
        expect(triggers.get() == 1, "La entrada debe notificarse exactamente una vez");
        expect(entranceBoard.getTileAt(7, 1).getPieceOnThisTile() == null, "La ficha sigue en la entrada");
        Tile inside = entranceBoard.getTileAt(visitorPiece.getX(), visitorPiece.getY());
        expect(inside.isRoomPosition() && inside.getPieceOnThisTile() == visitorPiece,
                "La pieza no fue enviada a una posicion azul de la habitacion");
        System.out.println("OK - Entrada notificada una vez y teletransporte a sala correcto");

        // Un jugador dentro de una sala debe poder salir al turno siguiente.
        expect(DicePathfinder.shortestDistance(entranceBoard, visitorPiece, 7, 1, 2) == 1,
                "No se puede salir de la habitacion por la puerta");
        entranceBoard.movePiece(visitorPiece, visitorPiece.getX(), visitorPiece.getY(), 7, 1);
        expect(triggers.get() == 1, "Se reentro automaticamente al intentar salir");
        expect(visitorPiece.getX() == 7 && visitorPiece.getY() == 1,
                "La ficha no salio de la habitacion");
        entranceBoard.movePiece(visitorPiece, 7, 1, 7, 0);
        entranceBoard.movePiece(visitorPiece, 7, 0, 7, 1);
        expect(triggers.get() == 2, "El listener no se activo al entrar otra vez");
        expect(entranceBoard.getTileAt(visitorPiece.getX(), visitorPiece.getY()).isRoomPosition(),
                "No se pudo volver a entrar en la sala");
        System.out.println("OK - Salida de habitacion y reentrada funcionales");

        game.closeGame();
        System.out.println("TODAS LAS PRUEBAS DEL MOCK PASARON");
    }
}
