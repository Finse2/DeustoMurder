package ui.match.mockClasses;

import game.core.GameSetup;
import model.Actor;
import model.Player;
import model.board.BoardFactory;
import model.board.RawPiece;
import ui.match.BoardLayout;
import ui.match.inGameScreen.InGameScreen;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class InGameScreenMock {

    public InGameScreenMock(BoardLayout board, List<Actor> actors) {
        new InGameScreen(board, actors).setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<Actor> actors = GameSetup.createActors().subList(0, 4);

            int[][] positions = {
                    {0, 5}, {22, 5}, {22, 15}, {0, 16}
            };

            RawPiece[] pieces = new RawPiece[4];

            for (int i = 0; i < pieces.length; i++) {
                Player player = new Player(actors.get(i), new ArrayList<>(), i);
                pieces[i] = new RawPiece(player.getPiece(), positions[i][0], positions[i][1]);
            }

            BoardLayout board = new BoardLayout(BoardLayout.GRID_ROWS, BoardLayout.GRID_COLUMNS, BoardFactory.createRawRooms(), pieces);
            new InGameScreenMock(board, actors);
        });
    }
}