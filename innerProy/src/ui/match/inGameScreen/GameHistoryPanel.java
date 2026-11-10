package ui.match.inGameScreen;

import game.history.GameEvent;
import game.history.GameHistory;
import model.Actor;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Registro visual de eventos de GameHistory; no almacena una copia de los eventos. */
public final class GameHistoryPanel extends JPanel {

    private static final Color BACKGROUND = new Color(9, 31, 45);
    private static final Color GOLD = new Color(194, 150, 92);
    private static final Color TEXT = new Color(222, 229, 232);
    private static final Color BLUE = new Color(44, 151, 204);

    private final GameHistory history;
    private final Map<String, Color> playerColors = new HashMap<>();
    private final JPanel entries = new JPanel();
    private final JLabel turnLabel = new JLabel("");
    private final JPanel turnBox = new JPanel(new BorderLayout(8, 0));
    private final JScrollPane scrollPane;
    private final Runnable historyChanged = this::refreshFromHistory;

    public GameHistoryPanel() {
        this(new GameHistory(), List.of());
    }

    public GameHistoryPanel(GameHistory history, List<Actor> actors) {
        super(new BorderLayout(0, 8));
        this.history = Objects.requireNonNull(history);
        for (Actor actor : actors) playerColors.put(actor.getName(), actor.getColor());

        setOpaque(true);
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel title = new JLabel("Registro de partida", new RegistryIcon(), SwingConstants.LEFT);
        title.setIconTextGap(9);
        title.setForeground(new Color(232, 204, 157));
        title.setFont(new Font("Georgia", Font.PLAIN, 18));
        title.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(title, BorderLayout.CENTER);
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(62, 92, 109));
        heading.add(separator, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);

        entries.setLayout(new BoxLayout(entries, BoxLayout.Y_AXIS));
        entries.setBackground(BACKGROUND);
        scrollPane = new JScrollPane(entries,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        JLabel turnIcon = new JLabel("◉");
        turnIcon.setForeground(BLUE);
        turnIcon.setFont(new Font("SansSerif", Font.BOLD, 22));
        turnLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        turnLabel.setForeground(new Color(105, 182, 227));
        JLabel hourglass = new JLabel("⌛");
        hourglass.setFont(new Font("SansSerif", Font.PLAIN, 19));
        hourglass.setForeground(GOLD);

        turnBox.setOpaque(true);
        turnBox.setBackground(new Color(8, 28, 41));
        turnBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 104, 119), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        turnBox.add(turnIcon, BorderLayout.WEST);
        turnBox.add(turnLabel, BorderLayout.CENTER);
        turnBox.add(hourglass, BorderLayout.EAST);
        add(turnBox, BorderLayout.SOUTH);

        history.addChangeListener(historyChanged);
        refreshFromHistory();
    }

    /** Actualiza la vista también si los eventos llegan desde un hilo de red. */
    private void refreshFromHistory() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshFromHistory);
            return;
        }

        entries.removeAll();
        GameEvent activeTurn = null;
        for (GameEvent event : history.getAllEvents()) {
            if (event.getType() == GameEvent.Type.TURN_STARTED) {
                activeTurn = event;
                continue;
            }
            if (event.getType() == GameEvent.Type.GAME_FINISHED) activeTurn = null;
            entries.add(createEntry(event));
        }

        turnBox.setVisible(activeTurn != null);
        if (activeTurn != null) {
            turnLabel.setText("Ahora es el turno de " + activeTurn.getPlayerName() + ".");
        }

        entries.revalidate();
        entries.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private JPanel createEntry(GameEvent event) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(7, 2, 7, 2));
        

        Color color = playerColors.getOrDefault(event.getPlayerName(), BLUE);
        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        dot.setFont(new Font("SansSerif", Font.PLAIN, 15));
        dot.setVerticalAlignment(SwingConstants.TOP);

        String who = event.getPlayerName();
        String narrative = eventNarrative(event);
        String body = who.isBlank() ? escape(narrative)
                : "<span style='color:" + hex(color) + "'>" + escape(who)
                + "</span> " + escape(narrative);
        JLabel message = new JLabel("<html><div style='width:220px;color:" + hex(TEXT)
                + ";font-size:12px;font-family:serif'>" + body + "</div></html>");
        message.setVerticalAlignment(SwingConstants.TOP);
        row.add(dot, BorderLayout.WEST);
        row.add(message, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private static String eventNarrative(GameEvent event) {
        String description = event.getDescription().trim();
        return switch (event.getType()) {
            case GAME_STARTED -> "La partida ha comenzado.";
            case GAME_FINISHED -> "La partida ha terminado.";
            case PLAYER_JOINED -> "se ha unido a la partida.";
            case PLAYER_LEFT -> "ha abandonado la partida.";
            case PLAYER_DISCONNECTED -> "ha perdido la conexión.";
            case PLAYER_MOVED -> description.startsWith("Player moved to ")
                    ? "se ha movido a " + description.substring("Player moved to ".length()) + "."
                    : (description.isEmpty() ? "se ha movido." : description);
            case TURN_ENDED -> "ha terminado su turno.";
            case TURN_STARTED -> "ha comenzado su turno.";
            case SUGGESTION_MADE -> "ha formulado una sospecha.";
            case SUGGESTION_REFUTED -> "ha refutado una sospecha.";
            case ACCUSATION_MADE -> "ha realizado una acusación.";
            case ACCUSATION_CORRECT -> "ha acertado la acusación.";
            case ACCUSATION_INCORRECT -> "ha fallado la acusación.";
            case CUSTOM -> description;
        };
    }

    private static String hex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Icono de registro dibujado en Swing, sin necesitar un recurso externo. */
    private static final class RegistryIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 19;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(1.3f));
            g.drawRoundRect(3, 1, 13, 18, 2, 2);
            g.drawLine(6, 1, 6, 19);
            g.drawLine(9, 6, 13, 6);
            g.drawLine(9, 10, 13, 10);
            g.drawLine(9, 14, 13, 14);
            g.dispose();
        }
    }

    public GameHistory getHistory() {
        return history;
    }

    public int getVisibleEventCount() {
        return entries.getComponentCount();
    }
}
