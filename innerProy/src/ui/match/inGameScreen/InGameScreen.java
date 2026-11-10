package ui.match.inGameScreen;

import activation.Screen.CluedoTableTheme;
import activation.Screen.Lobby.LobbyRoster;
import model.Player;
import model.Room;
import model.Weapon;
import ui.chat.Chat;
import game.history.GameHistory;
import ui.chat.ChatRenderer;
import model.Actor;
import ui.match.AccusationCard;
import ui.match.BoardLayout;
import activation.Screen.Lobby.LobbyRoster;

import activation.Screen.AnimatedBackGround.AnimatedBackGround;
import activation.Screen.ProfileButton;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contenedor principal de la partida. El tablero se muestra dentro de un
 * JScrollPane y permite zoom y desplazamiento horizontal y vertical.
 */
public final class InGameScreen extends JFrame {

    // ================================================================
    // SCREEN SEGMENTATION - a named panel for every visible area
    // ================================================================
    // rootPanel (BorderLayout)
    //   CENTER: gameRowPanel
    //     WEST:   playersColumnPanel -> logoPanel + playersPanel
    //     CENTER: boardScrollPane    -> boardCanvas -> board
    //     EAST:   rightColumnPanel   -> controlsPanel + historyChatPanel
    //                                       -> historyPanel + chatRenderer
    //   SOUTH:  footerPanel          -> handPanel + actionsPanel
    //
    // These fields are kept together so screen regions can be found at a glance.
    private JPanel rootPanel;
    private JPanel gameRowPanel;
    private JPanel playersColumnPanel;
    private JPanel logoPanel;
    private JPanel playersPanel;
    private JPanel rightColumnPanel;
    private JPanel controlsPanel;
    private JPanel historyChatPanel;
    private JPanel footerPanel;
    private JPanel handPanel;
    private JPanel panelJugadores;

    // Visible content of the named regions.
    private final BoardLayout board;
    private final List<Actor> actors;
    private final ChatRenderer chatRenderer;       // CHAT
    private final GameHistoryPanel historyPanel;   // HISTORY
    private final GameActionsPanel actionsPanel;   // ACTIONS
    private JScrollPane boardScrollPane;           // BOARD (scroll + zoom)
    private JPanel boardCanvas;                    // BOARD (inside viewport)

    private static final int GAP = 15;
    private static final int LEFT_COLUMN_WIDTH = 240;
    private static final int RIGHT_COLUMN_WIDTH = 345;
    private static final int HEADER_HEIGHT = 125;
    private static final int HAND_HEIGHT = 230;
    private static final Color SCREEN_BACKGROUND = Color.BLACK;
    private static final Color HEADER_BACKGROUND = new Color(16, 30, 38);
    private static final Color SECTION_BACKGROUND = new Color(25, 52, 67);
    private static final Color SECTION_BORDER = new Color(58, 36, 24);

    private double zoom = 1.0;
    private int baseTileSize = 25;

    private LobbyRoster roster;
    private JLabel playerCountLabel;
    public InGameScreen(BoardLayout board, List<Actor> actors, JComponent hand) {
        this(board, actors, hand, new Chat(
                actors.stream().map(Actor::getName).toList(),
                actors.isEmpty() ? "Jugador" : actors.getFirst().getName()));
    }

    public InGameScreen(BoardLayout board, List<Actor> actors, JComponent hand, Chat chat) {
        this(board, actors, hand, chat, new GameHistory());
    }

    /** Vincula la pantalla al historial real de una partida cuando está disponible. */
    public InGameScreen(BoardLayout board, List<Actor> actors, JComponent hand,
                        Chat chat, GameHistory history) {
        this.board = board;
        this.actors = actors;
        Map<String, Color> playerColors = actors.stream()
                .collect(Collectors.toMap(Actor::getName, Actor::getColor, (left, right) -> left));
        this.chatRenderer = new ChatRenderer(chat, playerColors);
        this.historyPanel = new GameHistoryPanel(history, actors);
        this.actionsPanel = new GameActionsPanel();

        configureWindow();
        defineScreenPanels(hand);     // 1. Create and configure each named region.
        assembleScreenPanels();       // 2. Place the regions in the screen layout.
        SwingUtilities.invokeLater(this::fitBoardToViewport);
    }

    /** Window settings, independent of the screen's panel hierarchy. */
    private void configureWindow() {
        setTitle("DeustoMurder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    /**
     * STEP 1 - DEFINE AREAS.
     * Each field represents one specific section of the screen.
     * Nothing is positioned in the parent layout here.
     */
    private void defineScreenPanels(JComponent hand) {
        // Entire window.
        rootPanel = new WoodTablePanel(new BorderLayout(GAP, GAP));
        rootPanel.setName("area.root");
        rootPanel.setBackground(SCREEN_BACKGROUND);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));

        // Main horizontal row (players | board | sidebar).
        gameRowPanel = new JPanel(new BorderLayout(GAP, 0));
        gameRowPanel.setName("area.mainRow");
        gameRowPanel.setOpaque(false);

        // LEFT: logo above the player list.
        playersColumnPanel = new JPanel(new BorderLayout(0, GAP));
        playersColumnPanel.setName("area.leftColumn");
        playersColumnPanel.setOpaque(false);
        playersColumnPanel.setPreferredSize(new Dimension(LEFT_COLUMN_WIDTH, 0));

        logoPanel = createDeustoLogoPanel();
        logoPanel.setName("area.logo");
        logoPanel.setBackground(HEADER_BACKGROUND);
        logoPanel.setPreferredSize(new Dimension(0, HEADER_HEIGHT));

        playersPanel = createPlayersPanel();
        playersPanel.setName("area.players");

        // CENTER: the board and its own scrollable viewport.
        boardScrollPane = createBoardScrollPane();
        boardScrollPane.setName("area.board");
        boardCanvas.setName("area.boardCanvas");

        // RIGHT: top controls, with the history and chat below.
        rightColumnPanel = new JPanel(new BorderLayout(0, GAP));
        rightColumnPanel.setName("area.rightColumn");
        rightColumnPanel.setOpaque(false);
        rightColumnPanel.setPreferredSize(new Dimension(RIGHT_COLUMN_WIDTH, 0));

        controlsPanel = createHeaderControlsPanel();
        controlsPanel.setName("area.headerControls");
        controlsPanel.setBackground(HEADER_BACKGROUND);
        controlsPanel.setPreferredSize(new Dimension(0, HEADER_HEIGHT));

        historyChatPanel = new JPanel(new GridLayout(2, 1, 0, GAP));
        historyChatPanel.setName("area.historyAndChat");
        historyChatPanel.setOpaque(false);
        historyPanel.setName("area.history");
        chatRenderer.setName("area.chat");

        // BOTTOM: the player's cards on the left and game actions on the right.
        footerPanel = new JPanel(new BorderLayout(GAP, 0));
        footerPanel.setName("area.footer");
        footerPanel.setOpaque(false);

        handPanel = new JPanel(new BorderLayout());
        handPanel.setName("area.hand");
        handPanel.setOpaque(false);
        handPanel.setPreferredSize(new Dimension(0, HAND_HEIGHT));
        handPanel.add(hand, BorderLayout.CENTER);

        actionsPanel.setName("area.actions");
        actionsPanel.setPreferredSize(new Dimension(RIGHT_COLUMN_WIDTH, 0));

        styleSectionPanel(logoPanel);
        styleSectionPanel(playersPanel);
        styleSectionPanel(controlsPanel);
        styleSectionPanel(historyPanel);
        styleSectionPanel(chatRenderer);
        styleSectionPanel(actionsPanel);
    }

    /**
     * STEP 2 - POSITION AREAS.
     * This is the complete screen map: every add() states exactly where a
     * named region belongs. Adjust the layout here without touching controls.
     */
    private void assembleScreenPanels() {
        // LEFT COLUMN
        playersColumnPanel.add(logoPanel, BorderLayout.NORTH);
        playersColumnPanel.add(playersPanel, BorderLayout.CENTER);

        // RIGHT COLUMN
        historyChatPanel.add(historyPanel);    // top half
        historyChatPanel.add(chatRenderer);    // bottom half
        rightColumnPanel.add(controlsPanel, BorderLayout.NORTH);
        rightColumnPanel.add(historyChatPanel, BorderLayout.CENTER);

        // MAIN ROW
        gameRowPanel.add(playersColumnPanel, BorderLayout.WEST);
        gameRowPanel.add(boardScrollPane, BorderLayout.CENTER);
        gameRowPanel.add(rightColumnPanel, BorderLayout.EAST);

        // BOTTOM ROW
        footerPanel.add(handPanel, BorderLayout.CENTER);
        footerPanel.add(actionsPanel, BorderLayout.EAST);

        // WHOLE SCREEN
        rootPanel.add(gameRowPanel, BorderLayout.CENTER);
        rootPanel.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    /** The top-right control area: profile, settings and quit. */
    private JPanel createHeaderControlsPanel() {
        JPanel header = new JPanel(new BorderLayout());
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        topButtons.setOpaque(false);

        // Profile button
        BufferedImage profilePicture = null;
        try {
            profilePicture = ImageIO.read(
                    Path.of("innerProy/ressources/DefaultProfile.jpg").toFile()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (profilePicture != null) {
            profilePicture = resizeIfNecessary(profilePicture, 512);
        }
        ProfileButton profileButton = new ProfileButton(profilePicture);
        profileButton.addActionListener(e -> openProfileStats());

        // Settings button
        ImageIcon settingsIcon = new ImageIcon("innerProy/ressources/settings.png");
        Image settingsImage = settingsIcon.getImage().getScaledInstance(
                40, 40, Image.SCALE_SMOOTH
        );
        JButton settingsButton = new JButton(new ImageIcon(settingsImage));
        settingsButton.setOpaque(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settingsButton.addActionListener(e -> openSettings());

        // Quit button
        JButton exitButton = new HeaderButton("Quit", 108, true);
        exitButton.addActionListener(e -> exitGame());

        topButtons.add(profileButton);
        topButtons.add(settingsButton);
        topButtons.add(exitButton);
        header.add(topButtons, BorderLayout.NORTH);
        return header;
    }

    /** Logo de Deusto en la esquina superior izquierda, adaptado al tamaño del panel. */
    private JPanel createDeustoLogoPanel() {
        Image logo = loadDeustoLogo();
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (logo == null) return;

                int imageWidth = logo.getWidth(null);
                int imageHeight = logo.getHeight(null);
                if (imageWidth <= 0 || imageHeight <= 0) return;

                int availableWidth = Math.max(1, getWidth() - 20);
                int availableHeight = Math.max(1, getHeight() - 20);
                double scale = Math.min(availableWidth / (double) imageWidth,
                        availableHeight / (double) imageHeight);
                int width = (int) Math.round(imageWidth * scale);
                int height = (int) Math.round(imageHeight * scale);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(logo, 10, (getHeight() - height) / 2, width, height, this);
                g.dispose();
            }
        };
    }

    private static Image loadDeustoLogo() {
        String resource = "/ui/match/inGameScreen/deusto_logo.png";
        URL url = InGameScreen.class.getResource(resource);
        if (url != null) return new ImageIcon(url).getImage();

        // También funciona cuando IntelliJ no copia los PNG del directorio src.
        for (Path path : List.of(
                Path.of("src/ui/match/inGameScreen/deusto_logo.png"),
                Path.of("innerProy/src/ui/match/inGameScreen/deusto_logo.png"),
                Path.of("DeustoMurder/innerProy/src/ui/match/inGameScreen/deusto_logo.png"))) {
            if (Files.isRegularFile(path)) return new ImageIcon(path.toString()).getImage();
        }
        return null;
    }

    /** Botones de cabecera: esquinas redondeadas y contorno contrastado. */
    private static final class HeaderButton extends JButton {
        private static final Color NORMAL = new Color(6, 25, 39);
        private static final Color HOVER = new Color(16, 45, 62);
        private static final Color OUTLINE = new Color(112, 146, 163);
        private final boolean exit;
        private boolean hovered;

        private HeaderButton(String label, int width, boolean exit) {
            super(label);
            this.exit = exit;
            setPreferredSize(new Dimension(width, 50));
            setFont(new Font("Georgia", Font.BOLD, 18));
            setForeground(new Color(243, 246, 247));
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent event) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent event) { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(hovered ? HOVER : NORMAL);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.fillRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 16, 16);
            g.setColor(OUTLINE);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 16, 16);
            g.dispose();

            // Swing centra el texto; la X se dibuja aparte, sin desplazar «Salir».
            super.paintComponent(graphics);
            if (exit) {
                Graphics2D icon = (Graphics2D) graphics.create();
                icon.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                icon.setColor(getForeground());
                icon.setFont(new Font("SansSerif", Font.BOLD, 23));
                FontMetrics metrics = icon.getFontMetrics();
                int baseline = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                icon.drawString("X", 13, baseline);
                icon.dispose();
            }
        }
    }

    private JScrollPane createBoardScrollPane() {
        // La mesa y el tablero forman un único componente dentro del viewport.
        boardCanvas = new WoodTablePanel(new GridBagLayout());
        boardCanvas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        boardCanvas.add(board);

        boardScrollPane = new JScrollPane(
                boardCanvas,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        boardScrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 24, 16), 6));
        boardScrollPane.getViewport().setBackground(new Color(32, 17, 11));
        boardScrollPane.setWheelScrollingEnabled(false);
        boardScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        boardScrollPane.getHorizontalScrollBar().setUnitIncrement(30);

        // Rueda: zoom; Mayús+rueda: scroll horizontal; Alt+rueda: scroll vertical.
        MouseAdapter wheelAdapter = new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent event) {
                if (event.isShiftDown()) {
                    scrollWithWheel(boardScrollPane.getHorizontalScrollBar(), event);
                } else if (event.isAltDown()) {
                    scrollWithWheel(boardScrollPane.getVerticalScrollBar(), event);
                } else {
                    zoomAt(event);
                }
            }
        };

        // Botón central / derecho, o Mayús + botón izquierdo: arrastrar en ambos ejes.
        MouseAdapter panAdapter = new MouseAdapter() {
            private Point lastScreenPoint;

            @Override
            public void mousePressed(MouseEvent event) {
                boolean middleOrRight = SwingUtilities.isMiddleMouseButton(event)
                        || SwingUtilities.isRightMouseButton(event);
                boolean shiftLeft = SwingUtilities.isLeftMouseButton(event) && event.isShiftDown();
                if (!middleOrRight && !shiftLeft) return;

                lastScreenPoint = event.getLocationOnScreen();
                boardCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                event.consume();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (lastScreenPoint == null) return;

                boolean held = (event.getModifiersEx() & (InputEvent.BUTTON1_DOWN_MASK
                        | InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK)) != 0;
                if (!held) return;

                Point point = event.getLocationOnScreen();
                int dx = point.x - lastScreenPoint.x;
                int dy = point.y - lastScreenPoint.y;
                JViewport viewport = boardScrollPane.getViewport();
                Point position = viewport.getViewPosition();
                moveViewport(position.x - dx, position.y - dy);
                lastScreenPoint = point;
                event.consume();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (lastScreenPoint == null) return;
                lastScreenPoint = null;
                boardCanvas.setCursor(Cursor.getDefaultCursor());
                event.consume();
            }
        };

        // Registrar también en las casillas y habitaciones, que son componentes hijos.
        installBoardMouseControls(boardCanvas, wheelAdapter, panAdapter);
        boardScrollPane.addMouseWheelListener(wheelAdapter);
        boardScrollPane.getViewport().addMouseWheelListener(wheelAdapter);

        boardScrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                fitBoardToViewport();
            }
        });

        return boardScrollPane;
    }

    private void installBoardMouseControls(Component component, MouseAdapter wheel, MouseAdapter pan) {
        component.addMouseWheelListener(wheel);
        component.addMouseListener(pan);
        component.addMouseMotionListener(pan);
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                installBoardMouseControls(child, wheel, pan);
            }
        }
    }

    private void fitBoardToViewport() {
        if (boardScrollPane.getWidth() <= 0 || boardScrollPane.getHeight() <= 0) return;

        int width = boardScrollPane.getWidth() - BoardLayout.FRAME_MARGIN * 2 - 25;
        int height = boardScrollPane.getHeight() - BoardLayout.FRAME_MARGIN * 2 - 25;
        if (width <= 0 || height <= 0) return;

        baseTileSize = Math.max(3, Math.min(
                width / BoardLayout.GRID_COLUMNS,
                height / BoardLayout.GRID_ROWS
        ));
        board.setTileSize(Math.max(3, (int) Math.round(baseTileSize * zoom)));
        boardCanvas.revalidate();
    }

    private void scrollWithWheel(JScrollBar scrollBar, MouseWheelEvent event) {
        event.consume();
        int offset = (int) Math.round(event.getPreciseWheelRotation() * scrollBar.getUnitIncrement() * 3);
        scrollBar.setValue(scrollBar.getValue() + offset);
    }

    private void moveViewport(int x, int y) {
        JViewport viewport = boardScrollPane.getViewport();
        Dimension view = viewport.getViewSize();
        Dimension extent = viewport.getExtentSize();
        viewport.setViewPosition(new Point(
                Math.max(0, Math.min(x, Math.max(0, view.width - extent.width))),
                Math.max(0, Math.min(y, Math.max(0, view.height - extent.height)))
        ));
    }

    private void zoomAt(MouseWheelEvent event) {
        event.consume();
        double nextZoom = Math.max(0.50, Math.min(3.00,
                zoom * Math.pow(1.12, -event.getPreciseWheelRotation())));
        if (Math.abs(nextZoom - zoom) < 0.001) return;

        JViewport viewport = boardScrollPane.getViewport();
        Point mouseInViewport = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), viewport);
        Point mouseInBoard = SwingUtilities.convertPoint(viewport, mouseInViewport, board);

        double fractionX = Math.max(0, Math.min(1, mouseInBoard.x / (double) Math.max(1, board.getWidth())));
        double fractionY = Math.max(0, Math.min(1, mouseInBoard.y / (double) Math.max(1, board.getHeight())));

        zoom = nextZoom;
        board.setTileSize(Math.max(3, (int) Math.round(baseTileSize * zoom)));
        boardCanvas.revalidate();
        boardScrollPane.revalidate();

        SwingUtilities.invokeLater(() -> {
            boardScrollPane.doLayout();
            viewport.doLayout();
            boardCanvas.doLayout();

            int anchorX = board.getX() + (int) Math.round(board.getWidth() * fractionX);
            int anchorY = board.getY() + (int) Math.round(board.getHeight() * fractionY);
            moveViewport(anchorX - mouseInViewport.x, anchorY - mouseInViewport.y);
        });
    }

    private JPanel createPlayersPanel() {
        panelJugadores = new JPanel();
        panelJugadores.setLayout(new BoxLayout(panelJugadores, BoxLayout.Y_AXIS));
        panelJugadores.setBackground(SECTION_BACKGROUND);

        playerCountLabel = new JLabel("Connected Players (0)");
        playerCountLabel.setFont(
                new Font("Georgia", Font.BOLD, 18)
        );
        playerCountLabel.setForeground(new Color(235, 215, 100));
        playerCountLabel.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
        panelJugadores.add(playerCountLabel);

        JLabel title = new JLabel("Jugadores:");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        panelJugadores.add(title);

        return panelJugadores;
    }

    private void styleSectionPanel(JComponent component) {
        component.setOpaque(true);
        component.setBackground(SECTION_BACKGROUND);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECTION_BORDER, 3),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
        ));
    }

    public GameHistoryPanel getHistoryPanel() {
        return historyPanel;
    }

    public GameActionsPanel getActionsPanel() {
        return actionsPanel;
    }

    public Chat getChat() {
        return chatRenderer.getChat();
    }

    public ChatRenderer getChatRenderer() {
        return chatRenderer;
    }

    public BoardLayout getBoard() {
        return board;
    }

    public JScrollPane getBoardScrollPane() {
        return boardScrollPane;
    }

    public void openSettings() {

        JFrame settingsFrame = new JFrame("Settings");

        String[] columns = {"Settings", "Value"};

        Object[][] data = {
                {"Music Volume", 0},
                {"Sound Effects", 0},
                {"Mute Sound", false},
                {"Brightness", 0},
                {"FullScreen", false},
                {"Highlight Possible Moves", true},
                {"Player Name", "Finse"},
                {"Default Server IP", "127.0.0.1"},
                {"Default Server Port", "5000"}
        };

        JTable settingsTable = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };

        JScrollPane scrollPane = CluedoTableTheme.styleConnection(settingsTable);

        settingsFrame.getContentPane().setBackground(new Color(20, 22, 27));
        settingsFrame.add(scrollPane);

        settingsFrame.setSize(450, 300);
        settingsFrame.setLocationRelativeTo(this);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setVisible(true);
    }

    public void openProfileStats() {

        JFrame statsFrame = new JFrame("Player Statistics");

        String[] columns = {
                "Statistic",
                "Value"
        };

        Object[][] data = {
                {"Games Played", 0},
                {"Games Won", 0},
                {"Games Lost", 0},
                {"Win Rate", "0%"},
                {"Total Points", 0}
        };

        JTable statsTable = new JTable(data, columns);

        JScrollPane scrollPane = CluedoTableTheme.styleConnection(statsTable);

        statsFrame.getContentPane().setBackground(new Color(20, 22, 27));
        statsFrame.add(scrollPane);

        statsFrame.setSize(450, 300);
        statsFrame.setLocationRelativeTo(this);
        statsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        statsFrame.setVisible(true);
    }

    private void exitGame() {

        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the game?",
                "Quit Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    private BufferedImage resizeIfNecessary(BufferedImage source, int maxSide) {
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();
        if (originalWidth <= maxSide && originalHeight <= maxSide) {
            return source;
        }

        double factor = Math.min((double) maxSide / originalWidth, (double) maxSide / originalHeight);
        int width = Math.max(1, (int) Math.round(originalWidth * factor));
        int height = Math.max(1, (int) Math.round(originalHeight * factor));
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(source, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    public void openAccusationCard(List<Weapon> weapons, List<Room> rooms) {

        AccusationCard.showCard(
                actors,
                weapons,
                rooms,
                selection -> {

                    Actor accused = selection.actor();
                    Weapon weapon = selection.weapon();
                    Room room = selection.room();

                    System.out.println("ACUSACIÓN CONFIRMADA");
                    System.out.println("Sospechoso: " + accused.getName());
                    System.out.println("Arma: " + weapon.getName());
                    System.out.println("Ubicación: " + room.getName());

                }
        );
    }
    private void refreshPlayers() {

        panelJugadores.removeAll();

        var connectedPlayers = roster.snapshot();

        playerCountLabel.setText(
                "Connected Players (" + connectedPlayers.size() + ")"
        );

        for (LobbyRoster.Entry player : connectedPlayers) {

            JPanel playerPanel = new JPanel(
                    new BorderLayout(10, 5)
            );

            playerPanel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    new Color(74, 49, 35)
                            ),
                            BorderFactory.createEmptyBorder(
                                    10, 12, 10, 12
                            )
                    )
            );

            playerPanel.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 70)
            );
            playerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            infoPanel.setOpaque(false);

            JLabel name = new JLabel(player.name());
            name.setFont(new Font("Georgia", Font.BOLD, 15));
            name.setForeground(new Color(235, 229, 212));
            infoPanel.add(name);

            playerPanel.setOpaque(true);
            playerPanel.setBackground(new Color(30, 55, 70));
            playerPanel.add(infoPanel, BorderLayout.CENTER);

            panelJugadores.add(playerPanel);
            panelJugadores.add(Box.createVerticalStrut(6));
        }

        panelJugadores.revalidate();
        panelJugadores.repaint();
    }
}


