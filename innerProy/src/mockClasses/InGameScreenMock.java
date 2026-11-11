package mockClasses;

import ui.chat.Chat;
import game.core.Game;
import game.core.GameSetup;
import game.core.GameStartResult;
import game.history.GameHistory;
import model.Actor;
import model.Player;
import model.Room;
import model.Weapon;
import model.board.BoardFactory;
import model.board.RawPiece;
import ui.match.BoardLayout;
import ui.match.cardDisplayer.PlayersCardsRenderer;
import ui.match.cardDisplayer.PlayersHandsRendererHeader;
import ui.match.inGameScreen.InGameScreen;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class InGameScreenMock {

    public InGameScreenMock(BoardLayout board, List<Actor> actors) {
        this(board, actors, new GameHistory());
    }

    public InGameScreenMock(BoardLayout board, List<Actor> actors, GameHistory history) {
        PlayersHandsRendererHeader[] headers = {
                new PlayersHandsRendererHeader(Actor.class, null),
                new PlayersHandsRendererHeader(Weapon.class),
                new PlayersHandsRendererHeader(Room.class)
        };

        PlayersCardsRenderer hand = new PlayersCardsRenderer(headers, CardRendererMock.createHand());
        Chat chat = new Chat(actors.stream().map(Actor::getName).toList(), actors.getFirst().getName());
        InGameScreen screen = new InGameScreen(board, actors, hand, chat, history);
        // Pulsa "Tirar dados", manten el raton presionado y sueltalo sobre el tablero.
        screen.getActionsPanel().setOnDiceRolled((first, second) -> System.out.println("DADOS: " + first + " + " + second + " = " + (first + second)));
        screen.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<Actor> actors = GameSetup.createActors().subList(0, 4);
            int[][] positions = {{0, 5}, {22, 5}, {22, 15}, {0, 16}};
            RawPiece[] pieces = new RawPiece[4];
            Game game = new Game();

            for (int i = 0; i < pieces.length; i++) {
                Player player = new Player(actors.get(i), new ArrayList<>(), i);
                game.addPlayer(player);
                pieces[i] = new RawPiece(player.getPiece(), positions[i][0], positions[i][1]);
            }

            BoardLayout board = new BoardLayout(
                    BoardLayout.GRID_ROWS,
                    BoardLayout.GRID_COLUMNS,
                    BoardFactory.createRawRooms(),
                    pieces
            );
            game.setBoard(board);
            GameStartResult result = game.startGame();
            if (!result.isSuccessful()) {
                throw new IllegalStateException(result.getMessage());
            }

            // Mock visual: historial vacío, sin textos de inicio ni de turno ficticios.
            new InGameScreenMock(board, actors);
        });
    }
}
