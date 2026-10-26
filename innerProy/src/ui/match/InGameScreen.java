package ui.match;

import game.GameSetup;
import model.Actor;
import model.Player;
import model.Room;
import model.Weapon;
import model.board.BoardFactory;
import model.board.RawPiece;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * game window <- composes the board and game-related Swing controls.
 * does not determine the answer cards, deal cards, or own turn rules.
 */
public final class InGameScreen extends JFrame {

    private final BoardLayout board;
    private final List<Actor> actors;
    private final List<Weapon> weapons;
    private final List<Room> rooms;
    public ArrayList<Player> players;
    /** Creates the current local/demo board, without starting turn logic. */
    public InGameScreen() {
        this(new BoardLayout(
                BoardLayout.GRID_ROWS,
                BoardLayout.GRID_COLUMNS,
                BoardFactory.createRawRooms(),
                new RawPiece[0] // No joined players yet.
        ), GameSetup.createActors(), GameSetup.createWeapons());
    }

    private InGameScreen(BoardLayout board, List<Actor> actors, List<Weapon> weapons) {
        this(board, actors, weapons, board.getRooms());
    }

    /** Can also be used with a game session prepared elsewhere. */
    public InGameScreen(BoardLayout board,
                        List<Actor> actors,
                        List<Weapon> weapons,
                        List<Room> rooms) {
        this.board = Objects.requireNonNull(board, "board");
        this.actors = List.copyOf(Objects.requireNonNull(actors, "actors"));
        this.weapons = List.copyOf(Objects.requireNonNull(weapons, "weapons"));
        this.rooms = List.copyOf(Objects.requireNonNull(rooms, "rooms"));

        setTitle("Deusto Murder - Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(650, 650));
        initializeComponents();
        configureWindowSize();
        setLocationRelativeTo(null);
    }

    /** Adds the game board and the visible controls. */
    private void initializeComponents() {
        JPanel root = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton detectiveCardButton = new JButton("Tarjeta del detective");
        detectiveCardButton.addActionListener(event -> showDetectiveCard());
        topPanel.add(detectiveCardButton);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(board, BorderLayout.CENTER);
        setContentPane(root);

        JPanel jugadoresPanel = new JPanel(new GridLayout(4,1,5,5));
        JPanel hostPanel = new JPanel(new GridLayout(1,2,5,5) );
        JLabel hostIcon = new JLabel("Imagen de perfil");
        JLabel hostName = new JLabel("Nombre del detective");
        hostPanel.add(hostIcon);
        hostPanel.add(hostName);
        jugadoresPanel.add(hostPanel);
        for (Player player : players){
            JPanel playerPanel = new JPanel(new  GridLayout(1,2,5,5));
            JLabel playerIcon = new JLabel("Imagen de perfil");
            JLabel playerName = new JLabel(player.getName());
            playerPanel.add(playerIcon);
            playerPanel.add(playerName);
            jugadoresPanel.add(playerPanel);
        }
        root.add(jugadoresPanel, BorderLayout.WEST);
    }

    /** opens the detective card in its own window. */
    private void showDetectiveCard() {
        DetectiveCard.showCard(actors, weapons, rooms);
    }

    private void configureWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int side = (int) Math.min(screenSize.width * 0.80, screenSize.height * 0.80);
        setSize(Math.max(650, side), Math.max(650, side));
    }

    public BoardLayout getBoard() {
        return board;
    }
}
