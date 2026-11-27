package ui.match.inGameScreen;

import javax.swing.plaf.basic.BasicScrollBarUI;
import game.core.Game;
import game.history.GameEvent;
import game.history.GameHistory;
import model.Actor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
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

    private static final Color BACKGROUND = new Color(12, 34, 46);
    private static final Color GOLD = new Color(214, 179, 113);
    private static final Color TEXT = new Color(232, 214, 181);
    private static final Color BLUE = new Color(44, 151, 204);

    private static final Color SECONDARY_TEXT = new Color(145, 169, 176);
    private static final Color BUTTON_BACKGROUND = new Color(16, 38, 49);
    private static final Color HOVER_BACKGROUND = new Color(41, 67, 75);
    private static final Color BORDER_COLOR = new Color(85, 112, 122);

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
    private final JTextField searchField = createSearchField();
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

        // JOURNAL TITLE
        JLabel title = new JLabel("CASE JOURNAL");

        title.setFont(new Font("Georgia", Font.BOLD, 18));
        title.setForeground(GOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // JOURNAL SUBTITLE
        JLabel subtitle = new JLabel("Investigation records");

        subtitle.setFont(new Font("Arial", Font.PLAIN, 11));
        subtitle.setForeground(SECONDARY_TEXT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        JButton optionsButton = miniButton("...");

        optionsButton.setToolTipText("Journal options");

        optionsButton.addActionListener(event ->
                actions.show(
                        optionsButton,
                        optionsButton.getWidth() - actions.getPreferredSize().width,
                        optionsButton.getHeight()
                )
        );

        // TITLE AND SUBTITLE
        JPanel titleContent = new JPanel();

        titleContent.setLayout(
                new BoxLayout(titleContent, BoxLayout.Y_AXIS)
        );

        titleContent.setOpaque(false);

        titleContent.add(Box.createVerticalGlue());
        titleContent.add(title);
        titleContent.add(Box.createVerticalStrut(3));
        titleContent.add(subtitle);
        titleContent.add(Box.createVerticalGlue());

        // JOURNAL ICON
        JLabel journalIcon = new JLabel(new RegistryIcon());

        journalIcon.setPreferredSize(new Dimension(30, 36));
        journalIcon.setHorizontalAlignment(SwingConstants.CENTER);

        // TITLE ROW
        JPanel titleRow = new JPanel(new BorderLayout(8, 0));

        titleRow.setOpaque(false);

        titleRow.setBorder(
                BorderFactory.createEmptyBorder(5, 3, 5, 3)
        );

        titleRow.add(journalIcon, BorderLayout.WEST);
        titleRow.add(titleContent, BorderLayout.CENTER);
        titleRow.add(optionsButton, BorderLayout.EAST);

        searchField.setToolTipText("Buscar por jugador, tipo o descripción");
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
        for (JComboBox<?> combo : List.of(
                typeFilter, playerFilter, dateFilter, limitFilter)) {
            styleComboBox(combo);
        }

        JPanel filters = new JPanel(new GridLayout(2, 2, 6, 5));

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
        searchField.setAlignmentX(LEFT_ALIGNMENT);
        filters.setPreferredSize(new Dimension(280, 59));
        filters.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 59)
        );

        filters.setAlignmentX(LEFT_ALIGNMENT);
        resultCount.setAlignmentX(LEFT_ALIGNMENT);
        // HEADER
        heading.add(titleRow);
        heading.add(Box.createVerticalStrut(5));

        // GOLD SEPARATOR
        JPanel goldSeparator = new JPanel();

        goldSeparator.setBackground(GOLD);
        goldSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);
        goldSeparator.setPreferredSize(new Dimension(1, 1));
        goldSeparator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1)
        );

        heading.add(goldSeparator);
        heading.add(Box.createVerticalStrut(8));

        // SEARCH FIELD
        heading.add(searchField);
        heading.add(Box.createVerticalStrut(4));

        // FILTERS
        heading.add(filters);
        heading.add(Box.createVerticalStrut(4));

        // EVENT COUNT
        heading.add(resultCount);
        heading.add(Box.createVerticalStrut(4));

        // BOTTOM SEPARATOR
        JSeparator bottomSeparator = new JSeparator();

        bottomSeparator.setForeground(BORDER_COLOR);
        bottomSeparator.setBackground(BORDER_COLOR);
        bottomSeparator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 2)
        );

        bottomSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(bottomSeparator);

        add(heading, BorderLayout.NORTH);

        entries.setLayout(new BoxLayout(entries, BoxLayout.Y_AXIS));
        entries.setBackground(BACKGROUND);
        entries.setBorder(
                BorderFactory.createEmptyBorder(5, 3, 5, 3)
        );
        scrollPane = new JScrollPane(
                entries,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(null);
        scrollPane.setBackground(BACKGROUND);
        scrollPane.getViewport().setBackground(BACKGROUND);

        // CUSTOM SCROLLBAR
        styleScrollBar(scrollPane.getVerticalScrollBar());

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

        JButton button = new JButton(label) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // BACKGROUND
                if (getModel().isPressed()) {
                    g2.setColor(BACKGROUND);
                } else if (getModel().isRollover()) {
                    g2.setColor(HOVER_BACKGROUND);
                } else {
                    g2.setColor(BUTTON_BACKGROUND);
                }

                g2.fillRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        10, 10
                );

                // BORDER
                g2.setColor(
                        getModel().isRollover()
                                ? GOLD
                                : BORDER_COLOR
                );

                g2.setStroke(new BasicStroke(1f));

                g2.drawRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        10, 10
                );

                // THREE DOTS ICON
                g2.setColor(GOLD);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                for (int i = -1; i <= 1; i++) {

                    g2.fillOval(
                            centerX + i * 7 - 2,
                            centerY - 2,
                            4, 4
                    );
                }

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(36, 34));
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    private void styleComboBox(JComboBox<?> combo) {

        // GENERAL APPEARANCE
        combo.setFont(new Font("Arial", Font.PLAIN, 11));
        combo.setBackground(BUTTON_BACKGROUND);
        combo.setForeground(TEXT);
        combo.setOpaque(true);

        combo.setBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1)
        );

        combo.setPreferredSize(new Dimension(140, 27));

        combo.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        combo.setMaximumRowCount(8);

        // CUSTOM COMBOBOX UI
        combo.setUI(new BasicComboBoxUI() {

            @Override
            protected JButton createArrowButton() {

                JButton arrowButton = new JButton() {

                    @Override
                    protected void paintComponent(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                        );

                        // Background
                        g2.setColor(getModel().isRollover()
                                ? HOVER_BACKGROUND
                                : BUTTON_BACKGROUND);

                        g2.fillRect(
                                0, 0,
                                getWidth(), getHeight()
                        );

                        // Arrow
                        g2.setColor(GOLD);

                        g2.setStroke(new BasicStroke(
                                1.7f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        ));

                        int x = getWidth() / 2;
                        int y = getHeight() / 2;

                        g2.drawLine(x - 4, y - 2, x, y + 2);
                        g2.drawLine(x, y + 2, x + 4, y - 2);

                        g2.dispose();
                    }
                };

                arrowButton.setPreferredSize(
                        new Dimension(24, 27)
                );

                arrowButton.setOpaque(false);
                arrowButton.setBorderPainted(false);
                arrowButton.setContentAreaFilled(false);
                arrowButton.setFocusPainted(false);
                arrowButton.setRolloverEnabled(true);

                arrowButton.setCursor(
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                );

                return arrowButton;
            }

            // SELECTED ITEM BACKGROUND
            @Override
            public void paintCurrentValueBackground(
                    Graphics g,
                    Rectangle bounds,
                    boolean hasFocus
            ) {

                g.setColor(BUTTON_BACKGROUND);

                g.fillRect(
                        bounds.x,
                        bounds.y,
                        bounds.width,
                        bounds.height
                );
            }

            // POPUP MENU
            @Override
            protected ComboPopup createPopup() {

                BasicComboPopup popup =
                        (BasicComboPopup) super.createPopup();

                popup.setBackground(BUTTON_BACKGROUND);

                popup.setBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR)
                );

                popup.getList().setBackground(BUTTON_BACKGROUND);
                popup.getList().setForeground(TEXT);

                popup.getList().setSelectionBackground(
                        HOVER_BACKGROUND
                );

                popup.getList().setSelectionForeground(GOLD);

                return popup;
            }
        });

        // CUSTOM ITEM RENDERER
        combo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {

                JLabel label = (JLabel)
                        super.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                cellHasFocus
                        );

                label.setFont(new Font("Arial", Font.PLAIN, 11));

                label.setOpaque(true);

                label.setBorder(
                        BorderFactory.createEmptyBorder(3, 7, 3, 4)
                );

                if (index >= 0 && isSelected) {

                    label.setBackground(HOVER_BACKGROUND);
                    label.setForeground(GOLD);

                } else {

                    label.setBackground(BUTTON_BACKGROUND);
                    label.setForeground(TEXT);
                }

                return label;
            }
        });
    }

    private JTextField createSearchField() {

        JTextField field = new JTextField() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                );

                // BACKGROUND
                g2.setColor(BACKGROUND);

                g2.fillRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        12, 12
                );

                // BORDER
                g2.setColor(
                        hasFocus() ? GOLD : BORDER_COLOR
                );

                g2.setStroke(
                        new BasicStroke(hasFocus() ? 1.5f : 1f)
                );

                g2.drawRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        12, 12
                );

                // SEARCH ICON
                g2.setColor(
                        hasFocus() ? GOLD : SECONDARY_TEXT
                );

                g2.setStroke(new BasicStroke(
                        1.7f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                int iconX = 12;
                int iconY = getHeight() / 2 - 7;

                g2.drawOval(iconX, iconY, 11, 11);

                g2.drawLine(
                        iconX + 9,
                        iconY + 9,
                        iconX + 15,
                        iconY + 15
                );

                g2.dispose();

                // DRAW USER TEXT
                super.paintComponent(g);

                // PLACEHOLDER
                if (getText().isEmpty()) {

                    Graphics2D placeholder =
                            (Graphics2D) g.create();

                    placeholder.setRenderingHint(
                            RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                    );

                    placeholder.setColor(SECONDARY_TEXT);
                    placeholder.setFont(getFont());

                    FontMetrics fm = placeholder.getFontMetrics();

                    int textY = (getHeight() - fm.getHeight()) / 2
                            + fm.getAscent();

                    placeholder.drawString(
                            "Search evidence...",
                            getInsets().left,
                            textY
                    );

                    placeholder.dispose();
                }
            }
        };

        // TEXT STYLE
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(TEXT);
        field.setCaretColor(GOLD);

        // TEXT SELECTION
        field.setSelectionColor(HOVER_BACKGROUND);
        field.setSelectedTextColor(TEXT);

        // TRANSPARENCY
        field.setOpaque(false);

        // INTERNAL PADDING
        field.setBorder(
                BorderFactory.createEmptyBorder(5, 36, 5, 10)
        );

        // DIMENSIONS
        field.setPreferredSize(new Dimension(280, 34));

        field.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 34)
        );

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        // FOCUS EFFECT
        field.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.repaint();
            }
        });

        return field;
    }

    private void styleScrollBar(JScrollBar scrollBar) {

        scrollBar.setPreferredSize(new Dimension(9, 9));
        scrollBar.setOpaque(false);
        scrollBar.setUnitIncrement(16);

        scrollBar.setUI(new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {

                this.thumbColor = GOLD;
                this.trackColor = BUTTON_BACKGROUND;
            }

            // REMOVE UPPER ARROW
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }

            // REMOVE LOWER ARROW
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }

            // INVISIBLE BUTTON
            private JButton createInvisibleButton() {

                JButton button = new JButton();

                Dimension size = new Dimension(0, 0);

                button.setPreferredSize(size);
                button.setMinimumSize(size);
                button.setMaximumSize(size);

                button.setOpaque(false);
                button.setBorderPainted(false);
                button.setContentAreaFilled(false);

                return button;
            }

            // SCROLLBAR TRACK
            @Override
            protected void paintTrack(
                    Graphics g,
                    JComponent c,
                    Rectangle trackBounds
            ) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(BUTTON_BACKGROUND);

                g2.fillRoundRect(
                        trackBounds.x,
                        trackBounds.y,
                        trackBounds.width,
                        trackBounds.height,
                        8, 8
                );

                g2.dispose();
            }

            // SCROLLBAR THUMB
            @Override
            protected void paintThumb(
                    Graphics g,
                    JComponent c,
                    Rectangle thumbBounds
            ) {

                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // HOVER AND DRAG EFFECTS
                if (isDragging) {

                    g2.setColor(GOLD);

                } else if (isThumbRollover()) {

                    g2.setColor(new Color(225, 195, 135));

                } else {

                    g2.setColor(new Color(160, 133, 87));
                }

                int padding = 2;

                g2.fillRoundRect(
                        thumbBounds.x + padding,
                        thumbBounds.y + padding,
                        Math.max(1, thumbBounds.width - padding * 2),
                        Math.max(1, thumbBounds.height - padding * 2),
                        8, 8
                );

                g2.dispose();
            }

            // MINIMUM THUMB SIZE
            @Override
            protected Dimension getMinimumThumbSize() {
                return new Dimension(9, 30);
            }
        });
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
        for (GameEvent event : filtered) {

            JPanel card = createEntry(event);

            JPanel wrapper = new JPanel(new BorderLayout());

            wrapper.setOpaque(false);

            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

            wrapper.setBorder(
                    BorderFactory.createEmptyBorder(0, 0, 6, 0)
            );

            wrapper.add(card, BorderLayout.CENTER);

            wrapper.setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            card.getMaximumSize().height + 6
                    )
            );

            entries.add(wrapper);
        }
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

        Color eventColor = getEventColor(event.getType());

        JPanel row = new JPanel(new BorderLayout(8, 0)) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // CARD BACKGROUND
                g2.setColor(BUTTON_BACKGROUND);

                g2.fillRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        12, 12
                );

                // CARD BORDER
                g2.setColor(new Color(48, 73, 85));
                g2.setStroke(new BasicStroke(1f));

                g2.drawRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        12, 12
                );

                // EVENT COLOR INDICATOR
                g2.setColor(eventColor);

                g2.fillRoundRect(
                        5, 8,
                        3, getHeight() - 16,
                        3, 3
                );

                g2.dispose();
            }
        };

        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.setBorder(
                BorderFactory.createEmptyBorder(9, 14, 9, 9)
        );

        // EVENT ICON
        JComponent eventIcon = createEventIcon(
                event.getType()
        );

        // EVENT INFORMATION
        JPanel information = new JPanel(
                new BorderLayout(0, 5)
        );

        information.setOpaque(false);

        // EVENT HEADER
        JPanel eventHeader = new JPanel(
                new BorderLayout(5, 0)
        );

        eventHeader.setOpaque(false);

        JLabel typeLabel = new JLabel(
                event.getType().name().replace('_', ' ')
        );

        typeLabel.setFont(new Font("Arial", Font.BOLD, 10));
        typeLabel.setForeground(eventColor);

        JLabel timeLabel = new JLabel(
                HOUR.format(event.getTimestamp())
        );

        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(SECONDARY_TEXT);

        eventHeader.add(typeLabel, BorderLayout.CENTER);
        eventHeader.add(timeLabel, BorderLayout.EAST);

        // EVENT DESCRIPTION
        String who = event.getPlayerName();
        String narrative = eventNarrative(event);

        Color playerColor = playerColors.getOrDefault(
                who, BLUE
        );

        String message = who.isBlank()
                ? escape(narrative)
                : "<span style='color:" + hex(playerColor)
                + "'><b>" + escape(who) + "</b></span> "
                + escape(narrative);

        JLabel description = new JLabel(
                "<html><div style='width:155px;"
                        + "color:" + hex(TEXT) + ";"
                        + "font-family:Arial;"
                        + "font-size:10px;'>"
                        + message
                        + "</div></html>"
        );

        information.add(eventHeader, BorderLayout.NORTH);
        information.add(description, BorderLayout.CENTER);

        // ADD COMPONENTS
        row.add(eventIcon, BorderLayout.WEST);
        row.add(information, BorderLayout.CENTER);

        row.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        Math.max(58, row.getPreferredSize().height)
                )
        );

        // EVENT DETAILS
        row.setToolTipText(
                "<html>"
                        + escape(event.getType().name())
                        + "<br>"
                        + escape(event.getTimestamp().toString())
                        + "<br>"
                        + escape(event.getDescription())
                        + "</html>"
        );

        return row;
    }

    private Color getEventColor(GameEvent.Type type) {

        return switch (type) {

            case GAME_STARTED, GAME_FINISHED ->
                    GOLD;

            case PLAYER_JOINED, SUGGESTION_REFUTED,
                 ACCUSATION_CORRECT ->
                    new Color(101, 187, 135);

            case PLAYER_LEFT, PLAYER_DISCONNECTED,
                 ACCUSATION_INCORRECT ->
                    new Color(215, 123, 112);

            case PLAYER_MOVED ->
                    new Color(181, 138, 209);

            case TURN_STARTED, TURN_ENDED ->
                    new Color(131, 182, 206);

            case SUGGESTION_MADE ->
                    new Color(217, 162, 92);

            case ACCUSATION_MADE ->
                    new Color(224, 108, 101);

            case CUSTOM ->
                    SECONDARY_TEXT;
        };
    }

    private JComponent createEventIcon(GameEvent.Type type) {

        JComponent icon = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                int x = (getWidth() - 32) / 2;
                int y = (getHeight() - 32) / 2;

                g2.translate(x, y);

                // ICON BACKGROUND
                g2.setColor(HOVER_BACKGROUND);
                g2.fillRoundRect(0, 0, 32, 32, 9, 9);

                // ICON COLOR
                g2.setColor(getEventColor(type));

                g2.setStroke(new BasicStroke(
                        1.8f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                switch (type) {

                    case GAME_STARTED, GAME_FINISHED -> {

                        // Flag
                        g2.drawLine(9, 7, 9, 25);
                        g2.drawRect(9, 7, 14, 10);
                    }

                    case PLAYER_JOINED -> {

                        // Plus
                        g2.drawLine(16, 8, 16, 24);
                        g2.drawLine(8, 16, 24, 16);
                    }

                    case PLAYER_LEFT, PLAYER_DISCONNECTED,
                         ACCUSATION_INCORRECT -> {

                        // Cross
                        g2.drawOval(6, 6, 20, 20);
                        g2.drawLine(12, 12, 20, 20);
                        g2.drawLine(20, 12, 12, 20);
                    }

                    case PLAYER_MOVED -> {

                        // Direction arrow
                        g2.drawLine(7, 16, 24, 16);
                        g2.drawLine(18, 10, 24, 16);
                        g2.drawLine(18, 22, 24, 16);
                    }

                    case TURN_STARTED, TURN_ENDED -> {

                        // Turn clock
                        g2.drawOval(6, 6, 20, 20);
                        g2.drawLine(16, 10, 16, 16);
                        g2.drawLine(16, 16, 21, 19);
                    }

                    case SUGGESTION_MADE -> {

                        // Magnifying glass
                        g2.drawOval(7, 7, 13, 13);
                        g2.drawLine(18, 18, 25, 25);
                    }

                    case SUGGESTION_REFUTED,
                         ACCUSATION_CORRECT -> {

                        // Check
                        g2.drawOval(6, 6, 20, 20);
                        g2.drawLine(10, 16, 14, 20);
                        g2.drawLine(14, 20, 23, 11);
                    }

                    case ACCUSATION_MADE -> {

                        // Warning triangle
                        Polygon triangle = new Polygon();

                        triangle.addPoint(16, 5);
                        triangle.addPoint(28, 26);
                        triangle.addPoint(4, 26);

                        g2.drawPolygon(triangle);

                        g2.drawLine(16, 12, 16, 18);
                        g2.fillOval(15, 21, 2, 2);
                    }

                    case CUSTOM -> {

                        // Information
                        g2.drawOval(6, 6, 20, 20);
                        g2.fillOval(15, 10, 2, 2);
                        g2.drawLine(16, 14, 16, 22);
                    }
                }

                g2.dispose();
            }
        };

        icon.setPreferredSize(new Dimension(32, 32));

        return icon;
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

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 26;
        }

        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {

            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.translate(x, y);

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(GOLD);

            g2.setStroke(new BasicStroke(
                    1.6f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            // NOTEBOOK
            g2.drawRoundRect(4, 2, 17, 22, 4, 4);

            // BINDING
            g2.drawLine(8, 3, 8, 23);

            // TEXT LINES
            g2.drawLine(12, 8, 17, 8);
            g2.drawLine(12, 12, 17, 12);
            g2.drawLine(12, 16, 17, 16);

            g2.dispose();
        }
    }

    public GameHistory getHistory() { return history; }
    public int getVisibleEventCount() { return entries.getComponentCount(); }
}
