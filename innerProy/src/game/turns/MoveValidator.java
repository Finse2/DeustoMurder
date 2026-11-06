package game.turns;

import model.Piece;
import model.Player;
import ui.match.BoardLayout;

public class MoveValidator {

    // IMPLEMENTACIONES:

    /*
    Comprobar que las coordenadas están dentro de los límites.
    Distinguir entre movimiento válido, pieza incorrecta, turno incorrecto y destino bloqueado.
    Evitar movimientos a casillas ocupadas cuando corresponda.
    Validar entradas a habitaciones y salidas de ellas.
    Detectar movimientos imposibles a través de paredes.
    Comprobar restricciones especiales de cada pieza.
    Registrar los movimientos rechazados en el historial.
    Devolver un motivo de error, no solamente true or false.
     */

    private final BoardLayout board;
    private final RoundManager roundManager;

    public MoveValidator (BoardLayout board, RoundManager roundManager) {

        if (board == null || roundManager == null) {
            throw new IllegalArgumentException("Board and round manager cannot be null");
        }

        this.board = board;
        this.roundManager = roundManager;

    }

    // COMPREUBA SI EL MOVIMIENTO ES VÁLIDO

    public boolean isValidMove (Player player, Piece piece, int destinationX, int destinationY) {
        if (player == null || piece == null) {
            return false;
        }

        // COMPROBAR TURNO

        if (!roundManager.isPlayerTurn(player)) {
            return false;
        }

        // COMPROBAR PROPIETARIO

        if (piece.getOwner() != player) {
            return false;
        }

        // COMPROBAR LÍMITES

        if (destinationX < 0 ||
                destinationX >= BoardLayout.GRID_COLUMNS ||
                destinationY < 0 ||
                destinationY >= BoardLayout.GRID_ROWS) {

            return false;
        }

        // CALCULAR DISTANCIA

        int dx = Math.abs(destinationX - piece.getX());
        int dy = Math.abs(destinationY - piece.getY());

        // PERMITIR MOVIMIENTOS DE UNA CASILLA

        if (dx + dy != 1) {
            return false;
        }

        // COMPROBAR TABLERO

        return board.canMovePiece(piece, destinationX, destinationY);
    }

}
