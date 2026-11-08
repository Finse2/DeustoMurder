
package ui.match.inGameScreen;

import model.Actor;
import ui.match.BoardLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * Clase contenedora principal, donde se añaden los diferentes elementos gráficos visibles para el usuario durante la partida
 * En el centro muestra un BoardLayout, alrededor del cual pone los demás elementos en posiciones predefinidas
 */
public final class InGameScreen extends JFrame {

    private final BoardLayout board;
    private final List<Actor> actors;

    public InGameScreen(BoardLayout board, List<Actor> actors, JComponent hand) {
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
        GameHistoryPanel history = new GameHistoryPanel();
        ChatPanel chat = new ChatPanel();

        JPanel handPanel = new JPanel(new BorderLayout());
        handPanel.setBackground(new Color(25, 55, 75));
        handPanel.add(hand, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(Color.BLACK);
        GameActionsPanel actions = new GameActionsPanel();

        JPanel players = createPlayersPanel();
        JPanel middle = new JPanel(new BorderLayout(10, 0));
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 10));

        middle.setBackground(Color.BLACK);
        right.setBackground(Color.BLACK);

        header.setPreferredSize(new Dimension(0, 125));
        handPanel.setPreferredSize(new Dimension(0, 230));
        players.setPreferredSize(new Dimension(240, 0));
        right.setPreferredSize(new Dimension(345, 0));
        actions.setPreferredSize(new Dimension(345, 0));

        right.add(history);
        right.add(chat);

        middle.add(players, BorderLayout.WEST);
        middle.add(board, BorderLayout.CENTER);
        middle.add(right, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(middle, BorderLayout.CENTER);
        bottom.add(handPanel, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

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
