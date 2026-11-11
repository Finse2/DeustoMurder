package game.turns;

import model.Piece;
import model.Room;
import model.board.Tile;
import ui.match.BoardLayout;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Rutas ortogonales de movimiento en el tablero. No atraviesa habitaciones
 * (Tile.disabled) ni otras fichas; busca el camino mas corto mediante BFS.
 * El origen es valido aunque en el futuro una ficha comience dentro de una sala.
 */
public final class DicePathfinder {

    private static final int[][] DIRECTIONS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    private DicePathfinder() {}

    /** Casillas alcanzables en hasta maxSteps movimientos; la inicial no se incluye. */
    public static Map<Point, Integer> distances(BoardLayout board, Piece piece, int maxSteps) {
        if (board == null || piece == null || maxSteps <= 0) return Map.of();
        Point start = new Point(piece.getX(), piece.getY());
        if (!inside(start.x, start.y)) return Map.of();
        if (board.getTileAt(start.x, start.y).getPieceOnThisTile() != piece) return Map.of();

        Map<Point, Integer> distances = new HashMap<>();
        Set<Point> visited = new HashSet<>();
        ArrayDeque<Point> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.removeFirst();
            int steps = current.equals(start) ? 0 : distances.get(current);
            if (steps >= maxSteps) continue;

            // Desde una casilla azul de una habitacion, las puertas de ESA sala
            // cuentan como los primeros destinos, a coste de una casilla.
            // Esto permite salir en el siguiente turno despues del teletransporte.
            Tile currentTile = board.getTileAt(current.x, current.y);
            if (current.equals(start) && currentTile.isRoomPosition() && currentTile.getRoom() != null) {
                Room room = currentTile.getRoom();
                for (java.util.List<Room.Cell> entrance : room.getEntrances()) {
                    for (Room.Cell cell : entrance) {
                        Point next = new Point(cell.column(), cell.row());
                        if (!inside(next.x, next.y) || !visited.add(next)) continue;
                        Tile exit = board.getTileAt(next.x, next.y);
                        if (exit.isDisabled() || (exit.getPieceOnThisTile() != null
                                && exit.getPieceOnThisTile() != piece)) continue;
                        distances.put(next, steps + 1);
                        queue.addLast(next);
                    }
                }
                continue;
            }

            for (int[] direction : DIRECTIONS) {
                Point next = new Point(current.x + direction[0], current.y + direction[1]);
                if (!inside(next.x, next.y) || !visited.add(next)) continue;
                Tile tile = board.getTileAt(next.x, next.y);
                if (tile.isDisabled()) continue;
                if (tile.getPieceOnThisTile() != null && tile.getPieceOnThisTile() != piece) continue;
                distances.put(next, steps + 1);
                queue.addLast(next);
            }
        }
        return Collections.unmodifiableMap(distances);
    }

    public static int shortestDistance(BoardLayout board, Piece piece, int x, int y, int limit) {
        return distances(board, piece, limit).getOrDefault(new Point(x, y), -1);
    }

    private static boolean inside(int x, int y) {
        return x >= 0 && x < BoardLayout.GRID_COLUMNS && y >= 0 && y < BoardLayout.GRID_ROWS;
    }
}
