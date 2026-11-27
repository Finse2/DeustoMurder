
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
import activation.Screen.ProfileButton;
import ui.NoirScrollBarUI;
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
import java.util.Objects;
import java.util.stream.Collectors;
import utils.AllThemes;
import utils.AllFonts;
import game.turns.RoundManager;

/**
 * Contenedor principal de la partida. El tablero se muestra dentro de un
 * JScrollPane y permite zoom y desplazamiento horizontal y vertical.
 */
public final class InGameScreen extends JFrame {


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
    private final ChatRenderer chatRenderer;
    private final GameHistoryPanel historyPanel;
    private final GameActionsPanel actionsPanel;
    private JScrollPane boardScrollPane;
    private JLayeredPane boardLayer;
    private final DiceRollPanel diceRollPanel;
    private JPanel boardCanvas;

    private static final int GAP = 10;
    private static final int LEFT_COLUMN_WIDTH = 240;
    private static final int RIGHT_COLUMN_WIDTH = 360;
    private static final int HEADER_HEIGHT = 125;
    private static final int CONTROL_HEIGHT = 70;
    private static final int CONTROL_WIDTH = 295;
    private static final int HAND_HEIGHT = 190;

    private double zoom = 1.0;
    private final JLabel zoomLabel = new JLabel("100%");
    private int baseTileSize = 25;

    private LobbyRoster roster;
    private JLabel playerCountLabel;
    private JLabel playersTitleLabel;
    private int connectedPlayers;
    private String activePlayerName;

    private JPanel playerListPanel;
    private final TurnHUD turnHUD = new TurnHUD();


    public InGameScreen(BoardLayout board, List<Actor> actors,
                        JComponent hand, LobbyRoster roster) {
        this(board, actors, hand, createDefaultChat(actors),
                new GameHistory(), roster);
    }

    public InGameScreen(BoardLayout board, List<Actor> actors,
                        JComponent hand) {
        this(board, actors, hand, new LobbyRoster());
    }

    public InGameScreen(BoardLayout board, List<Actor> actors,
                        JComponent hand, Chat chat, LobbyRoster roster) {
        this(board, actors, hand, chat, new GameHistory(), roster);
    }

    /** Vincula la pantalla al historial real de una partida cuando está disponible. */
    public InGameScreen(BoardLayout board, List<Actor> actors,
                        JComponent hand, Chat chat,
                        GameHistory history, LobbyRoster roster) {

        this.board = Objects.requireNonNull(board);
        this.actors = Objects.requireNonNull(actors);
        this.roster = Objects.requireNonNull(roster);

        Map<String, Color> playerColors = actors.stream()
                .collect(Collectors.toMap(
                        Actor::getName,
                        Actor::getColor,
                        (left, right) -> left
                ));

        this.chatRenderer = new ChatRenderer(chat, playerColors);
        this.historyPanel = new GameHistoryPanel(history, actors);
        this.historyPanel.bindTurnHUD(turnHUD, 3600);
        this.actionsPanel = new GameActionsPanel();

        this.diceRollPanel = actionsPanel.getDiceRollPanel();
        diceRollPanel.setVisible(false);
        actionsPanel.setOnDiceRequested(() -> diceRollPanel.prepareRoll());

        configureWindow();
        defineScreenPanels(hand);
        assembleScreenPanels();

        SwingUtilities.invokeLater(this::fitBoardToViewport);
    }

    private static Chat createDefaultChat(List<Actor> actors) {
        List<String> names = actors.stream()
                .map(Actor::getName)
                .collect(Collectors.toList());

        String currentPlayer = actors.isEmpty()
                ? "Jugador"
                : actors.get(0).getName();

        return new Chat(names, currentPlayer);
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
        rootPanel.setBackground(AllThemes.NegroDM);
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
        logoPanel.setBackground(AllThemes.AzulOscuroDM);
        logoPanel.setPreferredSize(new Dimension(0, HEADER_HEIGHT));

        playersPanel = createPlayersPanel();
        playersPanel.setName("area.players");

        // CENTER: the board and its own scrollable viewport.
        boardScrollPane = createBoardScrollPane();
        boardLayer = new JLayeredPane() {
            @Override
            public void doLayout() {
                boardScrollPane.setBounds(0, 0, getWidth(), getHeight());
                diceRollPanel.setBounds(0, 0, getWidth(), getHeight());
            }
        };
        boardLayer.setOpaque(false);
        boardLayer.setName("area.boardAndDice");
        boardLayer.add(boardScrollPane, JLayeredPane.DEFAULT_LAYER);
        boardLayer.add(diceRollPanel, JLayeredPane.PALETTE_LAYER);
        boardScrollPane.setName("area.board");
        boardCanvas.setName("area.boardCanvas");

        // RIGHT: top controls, with the history and chat below.
        rightColumnPanel = new JPanel(new BorderLayout(0, GAP));
        rightColumnPanel.setName("area.rightColumn");
        rightColumnPanel.setOpaque(false);
        rightColumnPanel.setPreferredSize(new Dimension(RIGHT_COLUMN_WIDTH, 0));

        controlsPanel = createHeaderControlsPanel();
        controlsPanel.setName("area.headerControls");
        controlsPanel.setBackground(AllThemes.AzulOscuroDM);
        controlsPanel.setPreferredSize(new Dimension(CONTROL_WIDTH, CONTROL_HEIGHT));

        historyChatPanel = new JPanel(
                new GridBagLayout()
        );
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
        actionsPanel.setPreferredSize(
                new Dimension(RIGHT_COLUMN_WIDTH, 235)
        );

        actionsPanel.setMinimumSize(
                new Dimension(280, 235)
        );

        styleSectionPanel(logoPanel);
        styleSectionPanel(playersPanel);
        styleSectionPanel(controlsPanel);
        styleSectionPanel(historyPanel);
        styleSectionPanel(chatRenderer);
        styleSectionPanel(actionsPanel);
    }

    /**
     * STEP 2 - POSITION AREAS.
     */
    private void assembleScreenPanels() {
        // LEFT COLUMN
        playersColumnPanel.add(logoPanel, BorderLayout.NORTH);
        playersColumnPanel.add(playersPanel, BorderLayout.CENTER);

        // RIGHT COLUMN
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        // CASE JOURNAL
        gbc.gridy = 0;
        gbc.weighty = 0.45;
        gbc.insets = new Insets(0, 0, 5, 0);

        historyChatPanel.add(historyPanel, gbc);

        // COMMUNICATIONS
        gbc.gridy = 1;
        gbc.weighty = 0.55;
        gbc.insets = new Insets(5, 0, 0, 0);

        historyChatPanel.add(chatRenderer, gbc);

        JPanel compactHeader = new JPanel(new BorderLayout());
        compactHeader.setOpaque(false);
        compactHeader.add(controlsPanel, BorderLayout.EAST);
        rightColumnPanel.add(compactHeader, BorderLayout.NORTH);
        rightColumnPanel.add(historyChatPanel, BorderLayout.CENTER);
        rightColumnPanel.add(actionsPanel, BorderLayout.SOUTH);

        // MAIN ROW
        gameRowPanel.add(playersColumnPanel, BorderLayout.WEST);
        JPanel boardSection = new JPanel(
                new BorderLayout(0, 4)
        );

        boardSection.setOpaque(false);

        boardSection.add(boardLayer, BorderLayout.CENTER);
        boardSection.add(createBoardControls(), BorderLayout.SOUTH);

        gameRowPanel.add(boardSection, BorderLayout.CENTER);

        // BOTTOM ROW
        footerPanel.add(handPanel, BorderLayout.CENTER);

        // WHOLE SCREEN
        rootPanel.add(gameRowPanel, BorderLayout.CENTER);
        rootPanel.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);

        JPanel mainContent = new JPanel(
                new BorderLayout(0, GAP)
        );

        mainContent.setOpaque(false);

        mainContent.add(gameRowPanel, BorderLayout.CENTER);
        mainContent.add(footerPanel, BorderLayout.SOUTH);

        rootPanel.add(mainContent, BorderLayout.CENTER);
        rootPanel.add(rightColumnPanel, BorderLayout.EAST);

        setContentPane(rootPanel);

    }

    private JButton createImageButton(
            String path,
            int buttonWidth,
            int buttonHeight,
            int imageWidth,
            int imageHeight
    ) {

        JButton button = new JButton();

        try {

            BufferedImage original = ImageIO.read(
                    Path.of(path).toFile()
            );

            if (original == null) {
                throw new IOException("Invalid image: " + path);
            }

            double scale = Math.min(
                    (double) imageWidth / original.getWidth(),
                    (double) imageHeight / original.getHeight()
            );

            int width = Math.max(1,
                    (int) Math.round(original.getWidth() * scale));

            int height = Math.max(1,
                    (int) Math.round(original.getHeight() * scale));

            BufferedImage resized = new BufferedImage(
                    width,
                    height,
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g2 = resized.createGraphics();

            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            g2.drawImage(original, 0, 0, width, height, null);
            g2.dispose();

            button.setIcon(new ImageIcon(resized));

        } catch (IOException e) {
            e.printStackTrace();
            button.setText("?");
        }

        button.setPreferredSize(
                new Dimension(buttonWidth, buttonHeight)
        );

        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder());

        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);

        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    /** The top-right control area: profile, settings and quit. */
    private JPanel createHeaderControlsPanel() {

        JPanel header = new JPanel(new GridBagLayout());

        JPanel topButtons = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 14, 0)
        );

        topButtons.setOpaque(false);

        // PROFILE BUTTON
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

        profileButton.setPreferredSize(new Dimension(54, 54));
        profileButton.addActionListener(e -> openProfileStats());

// SETTINGS BUTTON
        JButton settingsButton = new JButton() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY
                );

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                g2.translate(centerX, centerY);

                // Gear dimensions
                int teeth = 8;
                double outerRadius = 18;
                double innerRadius = 14;

                Polygon gear = new Polygon();

                double step = 2 * Math.PI / teeth;

                // Draw gear teeth
                for (int i = 0; i < teeth; i++) {

                    double angle = i * step;

                    double[] angles = {
                            angle - step * 0.30,
                            angle - step * 0.18,
                            angle + step * 0.18,
                            angle + step * 0.30
                    };

                    double[] radii = {
                            innerRadius,
                            outerRadius,
                            outerRadius,
                            innerRadius
                    };

                    for (int j = 0; j < 4; j++) {

                        gear.addPoint(
                                (int) Math.round(
                                        Math.cos(angles[j]) * radii[j]
                                ),
                                (int) Math.round(
                                        Math.sin(angles[j]) * radii[j]
                                )
                        );
                    }
                }

                // Gear with a transparent central hole
                java.awt.geom.Area shape =
                        new java.awt.geom.Area(gear);

                shape.subtract(
                        new java.awt.geom.Area(
                                new java.awt.geom.Ellipse2D.Double(
                                        -6, -6, 12, 12
                                )
                        )
                );

                // Hover effect
                if (getModel().isPressed()) {

                    g2.setColor(AllThemes.AzulClaroDM);

                } else if (getModel().isRollover()) {

                    g2.setColor(AllThemes.DoradorDM);

                } else {

                    g2.setColor(AllThemes.CremaDM);
                }

                g2.fill(shape);
                g2.dispose();
            }
        };

        settingsButton.setPreferredSize(new Dimension(54, 54));
        settingsButton.setOpaque(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setRolloverEnabled(true);

        settingsButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        settingsButton.setToolTipText("Settings");
        settingsButton.addActionListener(e -> openSettings());

        // QUIT BUTTON
        JButton exitButton = new JButton("Quit") {

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

                g2.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY
                );

                // Dimensions
                int width = getWidth() - 4;
                int height = 42;
                int x = 2;
                int y = (getHeight() - height) / 2;

                // Background
                if (getModel().isPressed()) {
                    g2.setColor(AllThemes.NegroDM);
                } else if (getModel().isRollover()) {
                    g2.setColor(AllThemes.AzulVerdosoDM);
                } else {
                    g2.setColor(AllThemes.AzulOscuro2DM);
                }

                g2.fillRoundRect(x, y, width, height, 16, 16);

                // Border
                g2.setColor(AllThemes.AzulClaroDM);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, width, height, 16, 16);

                // Icon and text
                g2.setColor(AllThemes.CremaDM);
                g2.setFont(AllFonts.GEORGIA_BOLD_18);

                FontMetrics fm = g2.getFontMetrics();

                int iconSize = 18;
                int iconGap = 9;
                int textWidth = fm.stringWidth(getText());

                int totalWidth = iconSize + iconGap + textWidth;
                int startX = (getWidth() - totalWidth) / 2;

                int iconX = startX + iconSize / 2;
                int iconY = getHeight() / 2;

                // Circle
                g2.drawOval(
                        iconX - 9,
                        iconY - 9,
                        iconSize,
                        iconSize
                );

                // X icon
                g2.setStroke(new BasicStroke(
                        2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                g2.drawLine(
                        iconX - 4, iconY - 4,
                        iconX + 4, iconY + 4
                );

                g2.drawLine(
                        iconX + 4, iconY - 4,
                        iconX - 4, iconY + 4
                );

                // Quit text
                int textX = startX + iconSize + iconGap;

                int textY = (getHeight() - fm.getHeight()) / 2
                        + fm.getAscent();

                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };

        exitButton.setPreferredSize(new Dimension(114, 54));
        exitButton.setMargin(new Insets(0, 0, 0, 0));
        exitButton.setOpaque(false);
        exitButton.setBorderPainted(false);
        exitButton.setContentAreaFilled(false);
        exitButton.setFocusPainted(false);
        exitButton.setRolloverEnabled(true);

        exitButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        exitButton.setToolTipText("Quit Game");
        exitButton.addActionListener(e -> exitGame());

        // ADD COMPONENTS
        topButtons.add(profileButton);
        topButtons.add(settingsButton);
        topButtons.add(exitButton);

        header.add(topButtons);

        return header;
    }

    /** Logo de Deusto en la esquina superior izquierda, adaptado al tamaño del panel. */
    private JPanel createDeustoLogoPanel() {

        Image logo = loadDeustoLogo();

        return new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                if (logo == null) {
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();

                // Rendering quality
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC
                );

                g2.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY
                );

                // Original image dimensions
                int imageWidth = logo.getWidth(null);
                int imageHeight = logo.getHeight(null);

                if (imageWidth <= 0 || imageHeight <= 0) {
                    g2.dispose();
                    return;
                }

                // Available space
                Insets insets = getInsets();

                int availableWidth = Math.max(1,
                        getWidth() - insets.left - insets.right - 8);

                int availableHeight = Math.max(1,
                        getHeight() - insets.top - insets.bottom - 8);

                // Preserve aspect ratio
                double scale = Math.min(
                        (double) availableWidth / imageWidth,
                        (double) availableHeight / imageHeight
                );

                int width = Math.max(1,
                        (int) Math.round(imageWidth * scale));

                int height = Math.max(1,
                        (int) Math.round(imageHeight * scale));

                // Center image
                int x = (getWidth() - width) / 2;
                int y = (getHeight() - height) / 2;

                // Draw logo
                g2.drawImage(logo, x, y, width, height, this);

                g2.dispose();
            }
        };
    }

    private static Image loadDeustoLogo() {

        try {
            return ImageIO.read(
                    Path.of("innerProy/ressources/deustomurder_logo.png").toFile()
            );

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /** Botones de cabecera: esquinas redondeadas y contorno contrastado. */
    private static final class HeaderButton extends JButton {

        private final boolean exit;
        private boolean hovered;

        private HeaderButton(String label, int width, boolean exit) {
            super(label);
            this.exit = exit;
            setPreferredSize(new Dimension(width, 50));
            setFont(AllFonts.GEORGIA_BOLD_18);
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
            g.setColor(hovered ? AllThemes.AzulVerdosoDM : AllThemes.AzulOscuro2DM);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.fillRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 16, 16);
            g.setColor(AllThemes.AzulClaroDM);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 16, 16);
            g.dispose();

            super.paintComponent(graphics);
            if (exit) {
                Graphics2D icon = (Graphics2D) graphics.create();
                icon.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                icon.setColor(getForeground());
                icon.setFont(AllFonts.SANS_SERIF_BOLD_23);
                FontMetrics metrics = icon.getFontMetrics();
                int baseline = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                icon.drawString("X", 13, baseline);
                icon.dispose();
            }
        }
    }

    private JButton createBoardControl(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(new Color(214, 179, 113));
        button.setBackground(new Color(12, 34, 46));

        button.setFocusPainted(false);

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(85, 112, 122)
                ),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    private JPanel createBoardControls() {

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 8, 3)
        );

        controls.setOpaque(false);

        JButton zoomOut = createBoardControl("-");
        JButton zoomIn = createBoardControl("+");
        JButton resetZoom = createBoardControl("Fit Board");

        zoomLabel.setForeground(new Color(214, 179, 113));
        zoomLabel.setFont(new Font("Arial", Font.BOLD, 12));
        zoomLabel.setHorizontalAlignment(SwingConstants.CENTER);
        zoomLabel.setPreferredSize(new Dimension(55, 28));

        zoomOut.addActionListener(e -> changeBoardZoom(zoom / 1.15));

        zoomIn.addActionListener(e -> changeBoardZoom(zoom * 1.15));

        resetZoom.addActionListener(e -> changeBoardZoom(1.0));

        controls.add(zoomOut);
        controls.add(zoomLabel);
        controls.add(zoomIn);
        controls.add(resetZoom);

        return controls;
    }

    private void changeBoardZoom(double newZoom) {

        zoom = Math.max(0.50, Math.min(3.00, newZoom));

        zoomLabel.setText(
                Math.round(zoom * 100) + "%"
        );

        fitBoardToViewport();

        boardScrollPane.revalidate();

        SwingUtilities.invokeLater(() -> {

            boardScrollPane.doLayout();

            JViewport viewport = boardScrollPane.getViewport();

            viewport.doLayout();
            boardCanvas.doLayout();

            Dimension view = viewport.getViewSize();
            Dimension extent = viewport.getExtentSize();

            moveViewport(
                    (view.width - extent.width) / 2,
                    (view.height - extent.height) / 2
            );
        });
    }

    private JScrollPane createBoardScrollPane() {
        boardCanvas = new WoodTablePanel(new GridBagLayout());
        boardCanvas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        boardCanvas.add(board);

        boardScrollPane = new JScrollPane(
                boardCanvas,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        boardScrollPane.setBorder(BorderFactory.createLineBorder(AllThemes.MarronOscuro3DM, 6));
        boardScrollPane.getViewport().setBackground(AllThemes.Granate2DM);
        boardScrollPane.setWheelScrollingEnabled(false);
        boardScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        boardScrollPane.getHorizontalScrollBar().setUnitIncrement(30);

        // NOIR SCROLLBARS

        boardScrollPane.getVerticalScrollBar().setUI(
                new NoirScrollBarUI()
        );

        boardScrollPane.getVerticalScrollBar().setPreferredSize(
                new Dimension(12, 12)
        );

        boardScrollPane.getHorizontalScrollBar().setUI(
                new NoirScrollBarUI()
        );

        boardScrollPane.getHorizontalScrollBar().setPreferredSize(
                new Dimension(12, 12)
        );

        boardScrollPane.setViewportBorder(null);

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
        zoomLabel.setText(
                Math.round(zoom * 100) + "%"
        );
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

        panelJugadores = new JPanel(new BorderLayout(0, 10));

        panelJugadores.setBackground(AllThemes.AzulVerdosoDM);

        // HEADER
        JPanel header = new JPanel();

        header.setLayout(
                new BoxLayout(header, BoxLayout.Y_AXIS)
        );

        header.setOpaque(false);
        header.setBorder(
                BorderFactory.createEmptyBorder(12, 8, 8, 8)
        );

        // TITLE
        playersTitleLabel = new JLabel("INVESTIGATORS");

        playersTitleLabel.setFont(AllFonts.GEORGIA_BOLD_18);
        playersTitleLabel.setForeground(AllThemes.DoradorDM);
        playersTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // CONNECTED PLAYERS
        playerCountLabel = new JLabel("\u25CF  0 Investigators");

        playerCountLabel.setFont(AllFonts.ARIAL_BOLD_12);
        playerCountLabel.setForeground(new Color(101, 187, 135));
        playerCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(playersTitleLabel);
        header.add(Box.createVerticalStrut(8));
        header.add(playerCountLabel);
        header.add(Box.createVerticalStrut(12));

        JSeparator separator = new JSeparator();
        separator.setForeground(AllThemes.DoradorDM);
        separator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 2)
        );

        header.add(separator);

        // PLAYER LIST
        playerListPanel = new JPanel();

        playerListPanel.setLayout(
                new BoxLayout(playerListPanel, BoxLayout.Y_AXIS)
        );

        playerListPanel.setOpaque(false);

        playerListPanel.setBorder(
                BorderFactory.createEmptyBorder(0, 8, 0, 8)
        );

        // SCROLL
        JScrollPane scrollPane = new JScrollPane(playerListPanel);

        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.getVerticalScrollBar().setUI(
                new NoirScrollBarUI()
        );

        scrollPane.getVerticalScrollBar().setPreferredSize(
                new Dimension(10, 10)
        );

        // FOOTER
        JLabel footer = new JLabel(
                "Everyone is a suspect.",
                SwingConstants.CENTER
        );

        footer.setFont(new Font("Georgia", Font.ITALIC, 12));
        footer.setForeground(AllThemes.DoradorDM);

        footer.setBorder(
                BorderFactory.createEmptyBorder(8, 0, 12, 0)
        );

        // ADD COMPONENTS
        panelJugadores.add(header, BorderLayout.NORTH);
        panelJugadores.add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));

        bottomPanel.setOpaque(false);

        bottomPanel.add(turnHUD, BorderLayout.CENTER);
        bottomPanel.add(footer, BorderLayout.SOUTH);

        panelJugadores.add(bottomPanel, BorderLayout.SOUTH);

        roster.setOnChanged(this::refreshPlayers);

        refreshPlayers();

        return panelJugadores;
    }

    public TurnHUD getTurnHUD() {
        return turnHUD;
    }

    private JComponent createPlayerAvatar(
            String name,
            Color playerColor
    ) {

        JComponent avatar = new JComponent() {

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

                int size = Math.min(getWidth(), getHeight()) - 4;

                if (size <= 0) {
                    g2.dispose();
                    return;
                }

                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // BACKGROUND
                g2.setColor(AllThemes.AzulOscuroDM);
                g2.fillOval(x, y, size, size);

                // BORDER
                g2.setColor(playerColor);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x, y, size, size);

                // INITIAL
                String initial = name == null || name.isBlank()
                        ? "?"
                        : name.substring(0, 1).toUpperCase();

                g2.setFont(new Font("Georgia", Font.BOLD, 19));
                g2.setColor(AllThemes.CremaDM);

                FontMetrics fm = g2.getFontMetrics();

                int textX = (getWidth()
                        - fm.stringWidth(initial)) / 2;

                int textY = (getHeight()
                        - fm.getHeight()) / 2 + fm.getAscent();

                g2.drawString(initial, textX, textY);

                g2.dispose();
            }
        };

        avatar.setPreferredSize(new Dimension(42, 42));

        return avatar;
    }

    private JPanel createPlayerCard(
            String name,
            Color playerColor,
            boolean active
    ) {

        JPanel card = new JPanel(new BorderLayout(10, 0)) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // BACKGROUND
                g2.setColor(active
                        ? new Color(45, 65, 60)
                        : AllThemes.AzulOscuro2DM
                );

                g2.fillRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        14, 14
                );

                // BORDER
                g2.setColor(active
                        ? AllThemes.DoradorDM
                        : new Color(55, 80, 90)
                );

                g2.setStroke(
                        new BasicStroke(active ? 2f : 1f)
                );

                g2.drawRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        14, 14
                );

                // PLAYER COLOR INDICATOR
                g2.setColor(playerColor);

                g2.fillRoundRect(
                        5, 10,
                        4, getHeight() - 20,
                        4, 4
                );

                g2.dispose();
            }
        };

        card.setOpaque(false);

        card.setPreferredSize(new Dimension(200, 74));
        card.setMinimumSize(new Dimension(160, 74));

        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 74)
        );

        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.setBorder(
                BorderFactory.createEmptyBorder(8, 15, 8, 10)
        );

        // AVATAR
        JComponent avatar = createPlayerAvatar(
                name, playerColor
        );

        // PLAYER INFORMATION
        JPanel information = new JPanel();

        information.setLayout(
                new BoxLayout(information, BoxLayout.Y_AXIS)
        );

        information.setOpaque(false);

        JLabel nameLabel = new JLabel(name);

        nameLabel.setFont(AllFonts.GEORGIA_BOLD_15);
        nameLabel.setForeground(AllThemes.CremaDM);

        JLabel statusLabel = new JLabel(
                active ? "Current turn" : "Connected"
        );

        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        statusLabel.setForeground(active
                ? AllThemes.DoradorDM
                : new Color(145, 180, 175)
        );

        information.add(Box.createVerticalGlue());
        information.add(nameLabel);
        information.add(Box.createVerticalStrut(4));
        information.add(statusLabel);
        information.add(Box.createVerticalGlue());

        // STATUS INDICATOR
        JLabel statusIndicator = new JLabel(
                active ? "\u25B6" : "\u25CF"
        );

        statusIndicator.setFont(new Font("Arial", Font.BOLD, 14));

        statusIndicator.setForeground(active
                ? AllThemes.DoradorDM
                : new Color(101, 187, 135)
        );

        // ADD COMPONENTS
        card.add(avatar, BorderLayout.WEST);
        card.add(information, BorderLayout.CENTER);
        card.add(statusIndicator, BorderLayout.EAST);

        card.setToolTipText(name);

        return card;
    }



    private void styleSectionPanel(JComponent component) {
        component.setOpaque(true);
        component.setBackground(AllThemes.AzulVerdosoDM);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AllThemes.MarronOscuro2DM, 3),
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

    public DiceRollPanel getDiceRollPanel() {
        return diceRollPanel;
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

        statsFrame.getContentPane().setBackground(AllThemes.MarinoDM);
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

    /** Vista del roster local para mocks: 4 jugadores, resaltando el turno activo. */
    public void updateMockPlayers(
            List<Player> joined,
            Player active
    ) {

        if (!SwingUtilities.isEventDispatchThread()) {

            SwingUtilities.invokeLater(
                    () -> updateMockPlayers(joined, active)
            );

            return;
        }

        playerListPanel.removeAll();

        connectedPlayers = joined.size();

        playerCountLabel.setText(
                connectedPlayers + " Connected Players"
        );

        for (Player player : joined) {

            boolean current = player == active;

            JPanel card = createPlayerCard(
                    player.getName(),
                    player.getPiece().getColor(),
                    current
            );

            playerListPanel.add(card);
            playerListPanel.add(Box.createVerticalStrut(8));
        }

        playerListPanel.revalidate();
        playerListPanel.repaint();
    }

    public void setActivePlayer(String playerName) {

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(
                    () -> setActivePlayer(playerName)
            );
            return;
        }

        activePlayerName = playerName;

        refreshPlayers();
    }

    public void bindRoundManager(RoundManager roundManager) {

        Objects.requireNonNull(roundManager);

        roundManager.setOnTurnChanged(player -> {

            setActivePlayer(
                    player != null ? player.getName() : null
            );
        });
    }

    private void refreshPlayers() {

        if (!SwingUtilities.isEventDispatchThread()) {

            SwingUtilities.invokeLater(this::refreshPlayers);
            return;
        }

        playerListPanel.removeAll();

        List<LobbyRoster.Entry> players = roster.snapshot();

        connectedPlayers = players.size();

        playerCountLabel.setText(
                "\u25CF  " + connectedPlayers + " Investigators"
        );

        for (LobbyRoster.Entry player : players) {

            JPanel card = createPlayerCard(
                    player.name(),
                    AllThemes.DoradorDM,
                    Objects.equals(player.name(), activePlayerName)
            );

            playerListPanel.add(card);
            playerListPanel.add(Box.createVerticalStrut(8));
        }

        playerListPanel.revalidate();
        playerListPanel.repaint();
    }
}
