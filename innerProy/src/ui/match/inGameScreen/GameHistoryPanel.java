package ui.match.inGameScreen;

import game.core.Game;
import game.history.GameEvent;
import game.history.GameHistory;
import model.Actor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicBoolean;

/** Vista del historial real: búsqueda, filtros, contador, CSV y turno actual. */
public final class GameHistoryPanel extends JPanel {

    private static final Color BACKGROUND = new Color(9, 31, 45);
    private static final Color GOLD = new Color(194, 150, 92);
    private static final Color TEXT = new Color(222, 229, 232);
    private static final Color BLUE = new Color(44, 151, 204);
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final GameHistory history;
    private final Map<String, Color> playerColors = new HashMap<>();
    private final JPanel entries = new JPanel();
    private final JLabel turnLabel = new JLabel("");
    private final JLabel timerLabel = new JLabel("");
    private final JLabel resultCount = new JLabel("0 eventos");
    private final JPanel turnBox = new JPanel(new BorderLayout(8, 0));
    private final JScrollPane scrollPane;
    private final JTextField searchField = new JTextField();
    private final JComboBox<Object> typeFilter = new JComboBox<>();
    private final JComboBox<String> playerFilter = new JComboBox<>();
    private final JComboBox<String> dateFilter = new JComboBox<>(new String[]{"Todo", "1 h", "24 h", "7 d"});
    private final JComboBox<String> limitFilter = new JComboBox<>(new String[]{"Últ. 200", "Todos"});
    private final AtomicBoolean refreshPending = new AtomicBoolean();
    private final Runnable historyChanged = this::refreshFromHistory;
    private final Consumer<Integer> timerChanged = this::updateRemainingTime;

    private Game boundGame;
    private boolean updatingPlayers;

    public GameHistoryPanel() {
        this(new GameHistory(), List.of());
    }

    public GameHistoryPanel(GameHistory history, List<Actor> actors) {
        super(new BorderLayout(0, 4));
        this.history = Objects.requireNonNull(history);
        for (Actor actor : actors) playerColors.put(actor.getName(), actor.getColor());

        setOpaque(true);
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));

        JLabel title = new JLabel("Registro de partida", new RegistryIcon(), SwingConstants.LEFT);
        title.setIconTextGap(9);
        title.setForeground(new Color(232, 204, 157));
        title.setFont(new Font("Georgia", Font.PLAIN, 17));

        JMenuItem exportOption = new JMenuItem("Exportar CSV...");
        JMenuItem importOption = new JMenuItem("Importar CSV...");
        JMenuItem summaryOption = new JMenuItem("Resumen de eventos");
        exportOption.addActionListener(event -> exportHistory());
        importOption.addActionListener(event -> importHistory());
        summaryOption.addActionListener(event -> showSummary());

        JPopupMenu actions = new JPopupMenu();
        actions.add(importOption);
        actions.add(exportOption);
        actions.addSeparator();
        actions.add(summaryOption);
        JButton optionsButton = miniButton("Opciones ▾");
        optionsButton.addActionListener(event -> actions.show(optionsButton, 0, optionsButton.getHeight()));

        JPanel titleRow = new JPanel(new BorderLayout(4, 0));
        titleRow.setOpaque(false);
        titleRow.add(title, BorderLayout.CENTER);
        titleRow.add(optionsButton, BorderLayout.EAST);

        searchField.setToolTipText("Buscar por jugador, tipo o descripción");
        searchField.putClientProperty("JTextField.placeholderText", "Buscar eventos...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) { refreshFromHistory(); }
            public void removeUpdate(DocumentEvent event) { refreshFromHistory(); }
            public void changedUpdate(DocumentEvent event) { refreshFromHistory(); }
        });

        typeFilter.addItem("Tipo: todos");
        for (GameEvent.Type type : GameEvent.Type.values()) typeFilter.addItem(type);
        playerFilter.addItem("Jugador: todos");
        typeFilter.addActionListener(event -> refreshFromHistory());
        playerFilter.addActionListener(event -> { if (!updatingPlayers) refreshFromHistory(); });
        dateFilter.addActionListener(event -> refreshFromHistory());
        limitFilter.addActionListener(event -> refreshFromHistory());

        typeFilter.setToolTipText("Filtrar por tipo de evento");
        playerFilter.setToolTipText("Filtrar por jugador");
        dateFilter.setToolTipText("Filtrar por antigüedad del evento");
        limitFilter.setToolTipText("Número máximo de eventos visibles");
        for (JComboBox<?> combo : List.of(typeFilter, playerFilter, dateFilter, limitFilter)) {
            combo.setFont(new Font("SansSerif", Font.PLAIN, 10));
        }

        JPanel filters = new JPanel(new GridLayout(1, 4, 4, 0));
        filters.setOpaque(false);
        filters.add(typeFilter);
        filters.add(playerFilter);
        filters.add(dateFilter);
        filters.add(limitFilter);

        resultCount.setForeground(new Color(141, 168, 181));
        resultCount.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 23));
        searchField.setAlignmentX(LEFT_ALIGNMENT);
        filters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 23));
        filters.setAlignmentX(LEFT_ALIGNMENT);
        resultCount.setAlignmentX(LEFT_ALIGNMENT);
        heading.add(titleRow);
        heading.add(Box.createVerticalStrut(3));
        heading.add(searchField);
        heading.add(Box.createVerticalStrut(2));
        heading.add(filters);
        heading.add(Box.createVerticalStrut(2));
        heading.add(resultCount);
        heading.add(Box.createVerticalStrut(2));
        heading.add(new JSeparator());
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
        turnLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        turnLabel.setForeground(new Color(105, 182, 227));
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        timerLabel.setForeground(GOLD);

        turnBox.setBackground(new Color(8, 28, 41));
        turnBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 104, 119), 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        turnBox.add(turnIcon, BorderLayout.WEST);
        turnBox.add(turnLabel, BorderLayout.CENTER);
        turnBox.add(timerLabel, BorderLayout.EAST);
        add(turnBox, BorderLayout.SOUTH);

        history.addChangeListener(historyChanged);
        refreshFromHistory();
    }

    private static JButton miniButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.PLAIN, 10));
        button.setFocusable(false);
        button.setMargin(new Insets(2, 4, 2, 4));
        return button;
    }

    /** Vincular solo si este panel visualiza game.getGameHistory(). */
    public void bindTimer(Game game) {
        if (boundGame != null) boundGame.removeTurnTimeListener(timerChanged);
        if (game != null && game.getGameHistory() != history) {
            throw new IllegalArgumentException("The panel and game must share the same history");
        }
        boundGame = game;
        timerLabel.setText("");
        if (game != null) game.addTurnTimeListener(timerChanged);
    }

    private void updateRemainingTime(int seconds) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> updateRemainingTime(seconds));
            return;
        }
        timerLabel.setText(seconds > 0 ? String.format("%02d:%02d", seconds / 60, seconds % 60) : "00:00");
    }

    private void refreshFromHistory() {
        if (!SwingUtilities.isEventDispatchThread()) {
            if (refreshPending.compareAndSet(false, true)) {
                SwingUtilities.invokeLater(() -> {
                    refreshPending.set(false);
                    refreshFromHistory();
                });
            }
            return;
        }

        List<GameEvent> all = history.getAllEvents();
        updatePlayerFilter(all);

        GameEvent activeTurn = null;
        for (GameEvent event : all) {
            if (event.getType() == GameEvent.Type.TURN_STARTED) activeTurn = event;
            if (event.getType() == GameEvent.Type.TURN_ENDED ||
                    event.getType() == GameEvent.Type.GAME_FINISHED) activeTurn = null;
        }
        turnBox.setVisible(activeTurn != null);
        if (activeTurn != null) turnLabel.setText("Turno: " + activeTurn.getPlayerName());

        GameEvent.Type type = typeFilter.getSelectedItem() instanceof GameEvent.Type selected ? selected : null;
        String player = playerFilter.getSelectedIndex() <= 0 ? null : (String) playerFilter.getSelectedItem();
        Instant from = switch (dateFilter.getSelectedIndex()) {
            case 1 -> Instant.now().minus(Duration.ofHours(1));
            case 2 -> Instant.now().minus(Duration.ofDays(1));
            case 3 -> Instant.now().minus(Duration.ofDays(7));
            default -> null;
        };
        List<GameEvent> filtered = history.filterEvents(searchField.getText(), type, null, player, from, null);
        int matching = filtered.size();
        if (limitFilter.getSelectedIndex() == 0 && matching > 200) {
            filtered = filtered.subList(matching - 200, matching);
        }

        JScrollBar bar = scrollPane.getVerticalScrollBar();
        boolean atBottom = bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum() - 20;
        int previousScroll = bar.getValue();

        entries.removeAll();
        for (GameEvent event : filtered) entries.add(createEntry(event));
        resultCount.setText(filtered.size() + " visibles / " + matching + " filtrados / " + all.size() + " eventos");
        entries.revalidate();
        entries.repaint();
        SwingUtilities.invokeLater(() -> {
            if (atBottom) bar.setValue(bar.getMaximum());
            else bar.setValue(previousScroll);
        });
    }

    private void updatePlayerFilter(List<GameEvent> all) {
        String selected = (String) playerFilter.getSelectedItem();
        TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (GameEvent event : all) if (!event.getPlayerName().isBlank()) names.add(event.getPlayerName());
        if (playerFilter.getItemCount() == names.size() + 1) {
            boolean identical = true;
            int index = 1;
            for (String name : names) {
                if (!name.equals(playerFilter.getItemAt(index++))) { identical = false; break; }
            }
            if (identical) return;
        }
        updatingPlayers = true;
        try {
            playerFilter.removeAllItems();
            playerFilter.addItem("Jugador: todos");
            for (String name : names) playerFilter.addItem(name);
            if (selected != null) playerFilter.setSelectedItem(selected);
            if (playerFilter.getSelectedIndex() < 0) playerFilter.setSelectedIndex(0);
        } finally {
            updatingPlayers = false;
        }
    }

    private JPanel createEntry(GameEvent event) {
        JPanel row = new JPanel(new BorderLayout(7, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));

        Color color = playerColors.getOrDefault(event.getPlayerName(), BLUE);
        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        dot.setVerticalAlignment(SwingConstants.TOP);

        String who = event.getPlayerName();
        String narrative = eventNarrative(event);
        String message = who.isBlank() ? escape(narrative) :
                "<span style='color:" + hex(color) + "'>" + escape(who) + "</span> " + escape(narrative);
        String time = HOUR.format(event.getTimestamp());
        JLabel content = new JLabel("<html><div style='width:245px;color:" + hex(TEXT)
                + ";font-size:11px;font-family:serif'><span style='color:#8da8b5'>" + time
                + "</span> " + message + "</div></html>");
        content.setToolTipText("<html>" + escape(event.getType().name()) + " | "
                + escape(event.getTimestamp().toString()) + "<br>" + escape(event.getDescription()) + "</html>");
        row.add(dot, BorderLayout.WEST);
        row.add(content, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private void exportHistory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("game-history.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (!path.toString().toLowerCase().endsWith(".csv")) path = Path.of(path + ".csv");
        Path destination = path;
        runFileTask(() -> history.exportCSV(destination), "Historial exportado correctamente");
    }

    private void importHistory() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        int answer = JOptionPane.showConfirmDialog(this,
                "¿Sustituir el historial actual? (No = añadir eventos)",
                "Importar historial", JOptionPane.YES_NO_CANCEL_OPTION);
        if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) return;
        boolean append = answer == JOptionPane.NO_OPTION;
        Path source = chooser.getSelectedFile().toPath();
        runFileTask(() -> history.importCSV(source, append), "Historial importado correctamente");
    }

    private interface FileTask { void run() throws IOException; }

    private void runFileTask(FileTask task, String success) {
        new SwingWorker<Void, Void>() {
            private IOException failure;
            protected Void doInBackground() {
                try { task.run(); }
                catch (IOException error) { failure = error; }
                return null;
            }
            protected void done() {
                JOptionPane.showMessageDialog(GameHistoryPanel.this,
                        failure == null ? success : failure.getMessage(),
                        failure == null ? "Historial" : "Error", failure == null
                                ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            }
        }.execute();
    }

    private void showSummary() {
        StringBuilder summary = new StringBuilder("Total: " + history.size() + " eventos\n\n");
        for (Map.Entry<GameEvent.Type, Long> entry : history.countByType().entrySet()) {
            summary.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
        JOptionPane.showMessageDialog(this, summary.toString(), "Resumen del historial",
                JOptionPane.INFORMATION_MESSAGE);
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

    private static final class RegistryIcon implements Icon {
        public int getIconWidth() { return 19; }
        public int getIconHeight() { return 20; }
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

    public GameHistory getHistory() { return history; }
    public int getVisibleEventCount() { return entries.getComponentCount(); }
}
