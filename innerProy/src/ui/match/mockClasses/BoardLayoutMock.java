package ui.match.mockClasses;

import game.GameSetup;
import model.Actor;
import model.Player;
import model.board.BoardFactory;
import model.board.RawPiece;
import ui.match.BoardLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public final class BoardLayoutMock {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<Actor> actors = GameSetup.createActors();

            // Mock player positions
            int[][] positions = {
                    {7, 0}, {8, 0}, {14, 0},
                    {15, 0}, {7, 5}, {8, 5}
            };

            RawPiece[] mockPieces = new RawPiece[actors.size()];

            for (int i = 0; i < actors.size(); i++) {
                Player player = new Player(
                        actors.get(i), new ArrayList<>(), i
                );

                mockPieces[i] = new RawPiece(
                        player.getPiece(),
                        positions[i][0],
                        positions[i][1]
                );
            }

            BoardLayout board = new BoardLayout(
                    BoardLayout.GRID_ROWS,
                    BoardLayout.GRID_COLUMNS,
                    BoardFactory.createRawRooms(),
                    mockPieces
            );

            JFrame frame = new JFrame("BoardLayout Mock Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(board);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
