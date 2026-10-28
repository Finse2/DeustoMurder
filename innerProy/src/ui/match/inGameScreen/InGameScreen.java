package ui.match.inGameScreen;

import model.Actor;
import ui.match.BoardLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class InGameScreen extends JFrame {

    private final BoardLayout board;
    private final List<Actor> actors;

    public InGameScreen(BoardLayout board, List<Actor> actors) {
        this.board = board;
        this.actors = actors;

        setTitle("DeustoMurder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(Color.BLACK);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GameBlock header = new GameBlock("HEADER - Jugadores · Turno · Configuración", new Color(10, 35, 55));
        GameBlock history = new GameBlock("Historial", new Color(25, 55, 75));
        GameBlock chat = new GameBlock("Chat", new Color(25, 55, 75));
        GameBlock hand = new GameBlock("PlayersCardsRenderer - Sugerir · Descartar", new Color(25, 55, 75));

        JPanel players = createPlayersPanel();
        JPanel middle = new JPanel(new BorderLayout(10, 0));
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 10));

        middle.setBackground(Color.BLACK);
        right.setBackground(Color.BLACK);

        header.setPreferredSize(new Dimension(0, 125));
        hand.setPreferredSize(new Dimension(0, 145));
        players.setPreferredSize(new Dimension(240, 0));
        right.setPreferredSize(new Dimension(345, 0));

        right.add(history);
        right.add(chat);

        middle.add(players, BorderLayout.WEST);
        middle.add(board, BorderLayout.CENTER);
        middle.add(right, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(middle, BorderLayout.CENTER);
        root.add(hand, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 55, 75));

        JLabel title = new JLabel("Jugadores");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title);

        for (Actor actor : actors) {
            JLabel name = new JLabel(actor.getName());
            name.setFont(new Font("Arial", Font.PLAIN, 20));
            name.setForeground(actor.getColor());
            panel.add(name);
        }

        return panel;
    }

    public BoardLayout getBoard() {
        return board;
    }
}