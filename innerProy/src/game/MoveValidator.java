package game;

import model.Piece;
import model.Player;
import ui.match.BoardLayout;

public class MoveValidator {

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

        if (!roundManager.isPlayerTurn(player)) {
            return false;
        }

        if (piece.getOwner() != player) {
            return false;
        }

        int dx = Math.abs(destinationX - piece.getX());
        int dy = Math.abs(destinationY - piece.getY());

        if (dx + dy != 1) {
            return false;
        }

        return board.canMovePiece(piece, destinationX, destinationY);
    }

}
