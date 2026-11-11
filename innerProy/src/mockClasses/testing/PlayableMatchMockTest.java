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
        game.closeGame();
        System.out.println("TODAS LAS PRUEBAS DEL MOCK PASARON");
    }
}
