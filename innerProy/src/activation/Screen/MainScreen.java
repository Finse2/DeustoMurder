package activation.Screen;

import Networking.Client;
import Networking.packets.GameOverPacket;
import game.statistics.ProfileMatchTracker;
import activation.Screen.Lobby.LobbyControl;
import activation.Screen.Lobby.LobbyRoster;
import activation.Screen.Lobby.LobbyScreen;
import activation.Screen.AnimatedBackGround.AnimatedBackGround;

import javax.swing.SwingWorker;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.sql.Time;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
/**
 * The MainScreen class represents the main user interface of the application.
 * It extends JFrame and provides functionality for hosting and joining games,
 * managing user profiles, and displaying various settings and statistics.
 */
public class MainScreen extends JFrame {
    private static final Path PROFILE_PATH = Path.of(System.getProperty("user.home"), ".deustoCluedo", "profile.png");
    private static final Path DEFAULT_PROFILE_PATH = Path.of("innerProy", "ressources", "DefaultProfile.jpg");
    private ProfileButton profileButton;
    private LobbyControl hostedLobby;
    private Client hostClient;
    private boolean hostingServer;
    private Client joinedClient;
    private boolean joiningServer = false;
    private LobbyRoster hostRoster;
    private LobbyRoster joinedRoster;
    private String HostUserName;
    private String ClientUserName;
    private final ProfileMatchTracker hostMatchTracker = new ProfileMatchTracker();
    private final ProfileMatchTracker joinedMatchTracker = new ProfileMatchTracker();
    private AnimatedBackGround animatedBackGround;
    private boolean animationFinished = false;
    private boolean connectionFinished = false;
    private JPanel[] menuPanels;

    private Runnable pendingLobbyAction;

    private static final Path NORMAL_BACKGROUND = Path.of("innerProy", "ressources", "MenuFrames1");
    private static final Path JOIN_BACKGROUND = Path.of("innerProy", "ressources", "MenuFrames2");

    private static final int FPS = 24;
/**
     * Constructor for the MainScreen class.
     * Initializes the main user interface, including the title, size, background image,
     * and various panels for hosting/joining games, settings, and profile management.
     */
    public MainScreen() {
        setTitle("Deusto Cluedo");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        animatedBackGround = new AnimatedBackGround();
        setContentPane(animatedBackGround);
        animatedBackGround.playLoop(NORMAL_BACKGROUND,FPS);


        // NORTH MAIN TITLE
        JPanel northPanel = new JPanel(new BorderLayout());

        JLabel Title = new JLabel("Deusto Cluedo", SwingConstants.CENTER);
        Title.setFont(new Font("Arial", Font.BOLD, 32));
        Title.setForeground(Color.RED);
        northPanel.add(Title, BorderLayout.CENTER);
        northPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        profileButton = new ProfileButton(loadProfilePicture());
        profileButton.addActionListener(e -> openProfileStats());
        ImageIcon icon1 = new ImageIcon("innerProy/ressources/settings.png");
        Image image1 = icon1.getImage();
        Image reseizedImage1 = image1.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        JButton settings = new JButton(new ImageIcon(reseizedImage1));
        settings.setOpaque(false);
        settings.setBorderPainted(false);
        settings.setContentAreaFilled(false);
        settings.setFocusPainted(false);
        buttonPanel.add(settings);
        buttonPanel.add(profileButton);
        northPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel westEqualizePanel = new JPanel();
        westEqualizePanel.setOpaque(false);
        Dimension eastSize = buttonPanel.getPreferredSize();
        westEqualizePanel.setPreferredSize(eastSize);
        northPanel.add(westEqualizePanel, BorderLayout.WEST);
        add(northPanel, BorderLayout.NORTH);

        // settings JTable
        settings.addActionListener(e -> {
            openSettings();
        });


        // WEST MENU BUTTONS
        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 0, 15));
        menuPanel.setOpaque(false);
        menuPanel.setPreferredSize(new Dimension(230, 210));

        InlaidMenuButton hostButton = new InlaidMenuButton("Host Game", this::HostGame);
        InlaidMenuButton joinButton = new InlaidMenuButton("Join Game", this::JoinGame);
        InlaidMenuButton exitButton = new InlaidMenuButton("Exit Game", this::Exit);

        menuPanel.add(hostButton);
        menuPanel.add(joinButton);
        menuPanel.add(exitButton);

// Container to position the menu
        JPanel westPanel = new JPanel(new GridBagLayout());
        westPanel.setOpaque(false);
        westPanel.setPreferredSize(new Dimension(280, 0));

        westPanel.add(menuPanel);

        add(westPanel, BorderLayout.WEST);

        // East Menu
        JPanel eastPanel = new JPanel(new GridBagLayout());
        eastPanel.setOpaque(false);
        eastPanel.setPreferredSize(new Dimension(280, 0));
        add(eastPanel, BorderLayout.EAST);

        // Center MENU
        JPanel centerPanel = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, Detective", SwingConstants.CENTER);
        welcome.setFont(new Font("Georgia", Font.BOLD, 24));
        welcome.setForeground(new Color(235, 215, 100));
        JLabel welcome2 = new JLabel("A Deusto version of the famous game Cluedo", SwingConstants.CENTER);
        welcome2.setFont(new Font("Georgia", Font.PLAIN, 14));
        welcome2.setForeground(new Color(220, 200, 175));
        centerPanel.add(welcome, BorderLayout.NORTH);
        centerPanel.add(welcome2, BorderLayout.SOUTH);
        centerPanel.setOpaque(false);
        add(centerPanel, BorderLayout.CENTER);


        // South Menu
        JPanel southPanel = new JPanel();
        JLabel status = new JLabel("Deusto Clue version 1.0", SwingConstants.CENTER);
        southPanel.add(status);
        southPanel.setOpaque(false);
        add(southPanel, BorderLayout.SOUTH);

        menuPanels = new JPanel[] {
                northPanel,
                westPanel,
                eastPanel,
                centerPanel,
                southPanel
        };

        setVisible(true);
    }
/**
     * Hosts a game by establishing a server and connecting the host client.
     * This method checks if the user is already hosting a game, opens a connection dialog,
     * and starts a background task to host the game and connect the host client.
     */
    private void HostGame() {

        if (isConnectionBusy()) {
            JOptionPane.showMessageDialog(
                    this,
                    "A connection is already active or in progress."
            );
            return;
        }

        if (hostingServer || (hostedLobby != null && hostedLobby.isRunning())) {

            JOptionPane.showMessageDialog(
                    this,
                    "You are already hosting a game."
            );
            return;
        }

        ConnectionResult connection = openConnection();

        if (connection == null) {
            return;
        }

        String username = JOptionPane.showInputDialog(
                this,
                "What is your username?"
        );

        if (username == null || username.isBlank()) {
            return;
        }

        HostUserName = username.trim();

        int port = connectionValues.getPort();
        hostingServer = true;

        startTransition();

        LobbyRoster roster = new LobbyRoster();

        new SwingWorker<HostedSession, Void>() {

            @Override
            protected HostedSession doInBackground() throws Exception {

                LobbyControl lobby = LobbyControl.host(port);

                CompletableFuture<Integer> accepted = new CompletableFuture<>();

                Client client = new Client("127.0.0.1", port, new Client.Listener() {
                    @Override
                    public void onConnected(int playerID) {
                        hostMatchTracker.onConnected(playerID, HostUserName);
                        roster.upsert(
                                playerID,
                                HostUserName,
                                System.getenv().getOrDefault("COMPUTERNAME", "Host-PC")
                        );
                        accepted.complete(playerID);
                    }
                    @Override
                    public void onPlayerJoined(int playerID, String name, String computerName) {
                        roster.upsert(playerID, name, computerName);
                    }

                    @Override
                    public void onGameStarted() {
                        hostMatchTracker.onGameStarted();
                    }

                    @Override
                    public void onGameOverPacket(GameOverPacket result) {
                        hostMatchTracker.onGameOver(result);
                    }

                    @Override
                    public void onDisconnected() {
                        accepted.completeExceptionally(new IOException("Disconnected before joining."));
                    }

                    @Override
                    public void onError(Exception e) {
                        accepted.completeExceptionally(e);
                    }
                });

                try {
                    String computerName = System.getenv().getOrDefault("COMPUTERNAME", "Host-PC");
                    client.connect(HostUserName, computerName);

                    int playerID = accepted.get(5, TimeUnit.SECONDS);

                    return new HostedSession(lobby, client, playerID);

                } catch (Exception e) {
                    client.disconnect();
                    lobby.close();
                    throw e;
                }
            }
            /**
             * This method is called when the background task is completed.
             * It retrieves the hosted session, updates the UI, and opens the host lobby.
             * If an exception occurs during hosting, it displays an error message.
             */
            @Override
            protected void done() {

                try {
                    HostedSession session = get();

                    hostedLobby = session.lobby();
                    hostClient = session.client();
                    hostRoster = roster;

                    System.out.println("Server started successfully.");
                    System.out.println("Host connected successfully.");
                    System.out.println("Player ID: " + session.playerId());
                    System.out.println("Connected players: " + hostedLobby.getClientCount());

                    System.out.println("Host connected. Waiting for animation...");

                    connectionReady(() -> {

                        if (hostClient == null || !hostClient.isConnected()) {
                            transitionFailed(
                                    "The host connection was lost before opening the lobby."
                            );
                            return;
                        }

                        try {
                            openHostLobby();
                            hostingServer = false;

                        } catch (Exception ex) {
                            hostingServer = false;

                            if (hostClient != null) {
                                hostClient.disconnect();
                                hostClient = null;
                            }

                            if (hostedLobby != null) {
                                hostedLobby.close();
                                hostedLobby = null;
                            }

                            resetTransition();

                            JOptionPane.showMessageDialog(
                                    MainScreen.this,
                                    ex.getMessage(),
                                    "Lobby Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });


                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    transitionFailed("Hosting interrupted.");

                } catch (ExecutionException e) {
                    transitionFailed(
                            "Failed to host: " + e.getCause().getMessage()
                    );

                } catch (Exception e) {
                    transitionFailed(e.getMessage());
                }
            }

        }.execute();
    }


    /**
     * Joins a game by connecting the client to an existing server.
     * This method checks if the user is already joining or connected to a game,
     * opens a connection dialog, and starts a background task to join the game.
     */
    private void JoinGame() {
        if (isConnectionBusy()) {
            JOptionPane.showMessageDialog(
                    this,
                    "A connection is already active or in progress."
            );
            return;
        }
        if (joiningServer || (joinedClient != null && joinedClient.isConnected())) {

            JOptionPane.showMessageDialog(
                    this,
                    "You are already joining or connected to a game."
            );
            return;
        }

        // Open the existing connection dialog
        connectionValues connection = ClientConnection();

        if (connection == null) {
            return;
        }

        String username = JOptionPane.showInputDialog(this,"What is your username");

        if (username == null || username.isBlank()) {
            return;
        }

        ClientUserName = username.trim();
        startTransition();

        String ipAddress = connectionValues.getIpAddress();
        int port = connectionValues.getPort();

        joiningServer = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        LobbyRoster roster = new LobbyRoster();

        new SwingWorker<Client, Void>() {

            @Override
            protected Client doInBackground() throws Exception {

                CompletableFuture<Integer> accepted =
                        new CompletableFuture<>();

                Client client = new Client(
                        ipAddress,
                        port,
                        new Client.Listener() {

                            @Override
                            public void onConnected(int playerID) {
                                joinedMatchTracker.onConnected(playerID, ClientUserName);
                                roster.upsert(
                                        playerID,
                                        ClientUserName,
                                        System.getenv().getOrDefault("COMPUTERNAME", "Unknown-PC")
                                );
                                accepted.complete(playerID);
                            }
                            @Override
                            public void onPlayerJoined(
                                    int playerID,
                                    String playerName,
                                    String computerName
                            ) {
                                roster.upsert(
                                        playerID,
                                        playerName,
                                        computerName
                                );
                            }

                            @Override
                            public void onGameStarted() {
                                joinedMatchTracker.onGameStarted();
                            }

                            @Override
                            public void onGameOverPacket(GameOverPacket result) {
                                joinedMatchTracker.onGameOver(result);
                            }

                            @Override
                            public void onDisconnected() {
                                accepted.completeExceptionally(
                                        new IOException("Disconnected before joining.")
                                );
                            }

                            @Override
                            public void onError(Exception e) {
                                accepted.completeExceptionally(e);
                            }
                        }
                );

                try {

                    String computerName = System.getenv().getOrDefault("COMPUTERNAME", "Unknown-PC");


                    client.connect(ClientUserName, computerName);

                    // Wait for the server to assign our player ID
                    int playerID = accepted.get(5, TimeUnit.SECONDS);

                    if (!client.isConnected()) {
                        throw new IOException(
                                "Connection lost while joining."
                        );
                    }

                    System.out.println("Joined server successfully.");
                    System.out.println("Assigned Player ID: " + playerID);

                    return client;

                } catch (Exception e) {
                    client.disconnect();
                    throw e;
                }
            }

            @Override
            protected void done() {

                setCursor(Cursor.getDefaultCursor());

                try {
                    Client client = get();

                    if (!client.isConnected()) {
                        throw new IOException(
                                "The server disconnected."
                        );
                    }

                    joinedClient = client;
                    joinedRoster = roster;

                    connectionReady(() -> {

                        if (joinedClient == null || !joinedClient.isConnected()) {
                            transitionFailed(
                                    "The connection was lost before entering the lobby."
                            );
                            return;
                        }

                        try {
                            openJoinedLobby();
                            joiningServer = false;
                        } catch (Exception ex) {
                            transitionFailed(
                                    "Unable to open the lobby: " + ex.getMessage()
                            );
                        }
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    transitionFailed("Joining interrupted.");

                } catch (ExecutionException e) {
                    transitionFailed(
                            "Failed to join: " + e.getCause().getMessage()
                    );

                } catch (IOException e) {
                    transitionFailed(e.getMessage());

                } catch (Exception e) {
                    transitionFailed(e.getMessage());
                }
            }

        }.execute();
    }
/**
     * Exits the application by disposing of the main window.
     */
    private void Exit() {
        animatedBackGround.disposePlayback();
        dispose();
    }

/**
     * Opens a dialog for the user to input connection parameters, either through a server code or manual entry.
     * The method validates the input and returns a connectionValues object containing the host name, IP address, and port.
     *
     * @return A connectionValues object with the specified connection parameters, or null if the dialog was canceled.
     */
    private connectionValues ClientConnection() {

        connectionValues connect = new connectionValues();
        final connectionValues[] result = {null};

        JDialog connectionDialog = new JDialog(
                this, "Connection Parameters", true
        );

        connectionDialog.setLayout(new BorderLayout(10, 10));

        // SERVER CODE INPUT
        JPanel codePanel = new JPanel(new BorderLayout(10, 0));

        JLabel codeLabel = new JLabel("Server Code:");
        JTextField codeHolder = new JTextField();

        codePanel.add(codeLabel, BorderLayout.WEST);
        codePanel.add(codeHolder, BorderLayout.CENTER);

        connectionDialog.add(codePanel, BorderLayout.NORTH);

        // MANUAL CONNECTION TABLE
        String[] columns = {"Parameter", "Value"};

        Settings[] data = {
                new Settings("Host name", connectionValues.hostName),
                new Settings("IP Address", connectionValues.ipAddress),
                new Settings("Port", connectionValues.port)
        };

        JTable connectionTable = new JTable(Settings.toArray(data), columns
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };

        JScrollPane scrollPane = CluedoTableTheme.style(connectionTable);

        connectionDialog.add(scrollPane, BorderLayout.CENTER);

        // CONFIRM BUTTON
        JButton confirmButton = new JButton("Confirm");

        confirmButton.addActionListener(e -> {
            try {
                String hostName;
                String ipAddress;
                int port;

                String code = codeHolder.getText().trim();

                if (!code.isEmpty()) {

                    // CONNECTION USING SERVER CODE
                    ServerCode.ServerInfo info =
                            ServerCode.decode(code);

                    hostName = info.hostname();
                    ipAddress = info.ip();
                    port = info.port();

                } else {

                    // MANUAL CONNECTION
                    if (connectionTable.isEditing()) {
                        if (!connectionTable.getCellEditor()
                                .stopCellEditing()) {
                            return;
                        }
                    }

                    Object nameValue = connectionTable.getValueAt(0, 1);
                    Object ipValue = connectionTable.getValueAt(1, 1);
                    Object portValue = connectionTable.getValueAt(2, 1);

                    hostName = nameValue == null ? ""
                            : nameValue.toString().trim();

                    ipAddress = ipValue == null ? ""
                            : ipValue.toString().trim();

                    port = Integer.parseInt(
                            portValue == null ? ""
                                    : portValue.toString().trim()
                    );
                }

                // VALIDATE PARAMETERS
                if (hostName.isBlank() || ipAddress.isBlank()) {
                    throw new IllegalArgumentException(
                            "Hostname and IP cannot be empty."
                    );
                }

                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException(
                            "Invalid port."
                    );
                }

                // SAVE CONNECTION VALUES
                connect.setHostName(hostName);
                connect.setIpAddress(ipAddress);
                connect.setPort(port);

                result[0] = connect;

                connectionDialog.dispose();

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        connectionDialog,
                        "Please enter a valid numeric port.",
                        "Invalid Port",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        connectionDialog,
                        "Connection parameters error: "
                                + ex.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        connectionDialog.add(confirmButton, BorderLayout.SOUTH);

        connectionDialog.setSize(450, 300);
        connectionDialog.setLocationRelativeTo(this);
        connectionDialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        connectionDialog.setVisible(true);
        return result[0];
    }

/**
     * Opens a dialog for the user to input connection parameters, either through a server code or manual entry.
     * The method validates the input and returns a connectionValues object containing the host name, IP address, and port.
     *
     * @return A connectionValues object with the specified connection parameters, or null if the dialog was canceled.
     */
    private ConnectionResult openConnection() {

        connectionValues connect = new connectionValues();

        // Stores the result after confirmation
        final ConnectionResult[] result = {null};

        JDialog connectionDialog = new JDialog(
                this, "Connection Parameters", true
        );

        String[] columns = {"Parameter", "Value"};

        Settings[] data = {
                new Settings("Host name", connectionValues.hostName),
                new Settings("IP Address", connectionValues.ipAddress),
                new Settings("Port", connectionValues.port)
        };

        JTable connectionTable = new JTable(Settings.toArray(data), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };

        JScrollPane scrollPane = CluedoTableTheme.styleConnection(connectionTable);
        connectionDialog.add(scrollPane, BorderLayout.CENTER);

        JButton confirmButton = new JButton("Confirm");

        confirmButton.addActionListener(e -> {
            try {
                if (connectionTable.isEditing()) {
                    connectionTable.getCellEditor().stopCellEditing();
                }

                String hostName = connectionTable.getValueAt(0, 1).toString().trim();
                String ipAddress = connectionTable.getValueAt(1, 1).toString().trim();
                int port = Integer.parseInt(
                        connectionTable.getValueAt(2, 1).toString().trim()
                );

                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException("Invalid port");
                }

                // Generate the server code
                String code = ServerCode.Encode(ipAddress, port, hostName);

                // Store the connection parameters
                connect.setHostName(hostName);
                connect.setIpAddress(ipAddress);
                connect.setPort(port);

                // Save both values together
                result[0] = new ConnectionResult(connect, code);

                connectionDialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        connectionDialog,
                        "Please enter a valid numeric port.",
                        "Invalid Port",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                        connectionDialog,
                        ex.getMessage(),
                        "Invalid Connection Parameters",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        connectionDialog,
                        "Unable to generate server code: " + ex.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        connectionDialog.add(confirmButton, BorderLayout.SOUTH);

        connectionDialog.setSize(450, 300);
        connectionDialog.setLocationRelativeTo(this);
        connectionDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        connectionDialog.setVisible(true);

        return result[0];
    }

/**
     * Opens a settings dialog that allows the user to adjust various application settings.
     * The settings are displayed in a table format, and the user can modify values as needed.
     */
    public void openSettings() {
        JFrame settingsFrame = new JFrame("Settings");
        String[] colums = {"Settings", "Value"};

        Settings[] data = {
                new Settings("Music Volume", 0),
                new Settings("Sound Effects", 0),
                new Settings("Mute Sound", false),
                new Settings("Brightness", 0),
                new Settings("FullScreen", false),
                new Settings("Highlight Possible Moves", true),
                new Settings("Player Name", "Finse"),
                new Settings("Default Server IP", "127.0.0.1"),
                new Settings("Default Server Port", "5000")
        };

        JTable settingsTable = new JTable(Settings.toArray(data), colums) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Make all cells oof the first column non-editable
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

/**
     * Opens a profile statistics dialog that displays the user's game statistics and allows them to change their profile picture.
     * The statistics are displayed in a table format, and the user can modify their profile picture as needed.
     */
    public void openProfileStats() {
        ProfileStatisticsDialog.open(this, this::changeProfilePicture);
    }
/**
     * Creates a custom-styled JButton for use in dialogs.
     *
     * @param text The text to display on the button.
     * @return A JButton with the specified text and custom styling.
     */
    private JButton createDialogButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(75, 38, 42));
        button.setForeground(new Color(225, 190, 140));
        button.setFont(new Font("Georgia", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setOpaque(true);
        return button;
    }
/**
     * Loads the user's profile picture from the specified path.
     * If the profile picture is not found, it loads a default profile picture.
     *
     * @return A BufferedImage representing the user's profile picture or the default picture.
     */
    private BufferedImage loadProfilePicture() {
        BufferedImage saved = readImage(PROFILE_PATH);
        if (saved != null) {
            return saved;
        }
        return readImage(DEFAULT_PROFILE_PATH);
    }
/**
     * Reads an image from the specified path.
     *
     * @param path The path to the image file.
     * @return A BufferedImage representing the image, or null if the file is not found or cannot be read.
     */
    private BufferedImage readImage(Path path) {
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try {
            return ImageIO.read(path.toFile());
        } catch (IOException ex) {
            System.err.println("Could not read image " + path + ": " + ex.getMessage());
            return null;
        }
    }
/**
     * Opens a file chooser dialog to allow the user to select a new profile picture.
     * The selected image is resized if necessary and saved to the specified profile path.
     * If an error occurs during the process, an error message is displayed.
     */
    private void changeProfilePicture() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Profile Picture");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Image files (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"
        ));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            BufferedImage selected = ImageIO.read(chooser.getSelectedFile());
            if (selected == null) {
                throw new IOException("Unsupported or damaged image file.");
            }

            // Resize very large pictures before saving, preserving their aspect ratio.
            BufferedImage picture = resizeIfNecessary(selected, 512);
            Files.createDirectories(PROFILE_PATH.getParent());
            if (!ImageIO.write(picture, "png", PROFILE_PATH.toFile())) {
                throw new IOException("Could not save the profile image as PNG.");
            }

            profileButton.setPicture(picture);
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not update profile picture: " + ex.getMessage(),
                    "Image Error", JOptionPane.ERROR_MESSAGE);
        }
    }
/**
     * Resizes the given image if its width or height exceeds the specified maximum side length.
     * The aspect ratio of the image is preserved during resizing.
     *
     * @param source  The original BufferedImage to be resized.
     * @param maxSide The maximum allowed length for the width or height of the image.
     * @return A new BufferedImage that is resized if necessary, or the original image if no resizing is needed.
     */
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
/**
     * Opens the host lobby screen, allowing the host to manage the game and start it.
     * This method creates a new LobbyScreen instance and sets up a window listener to handle cleanup when the lobby is closed.
     *
     * @throws Exception If an error occurs while opening the lobby.
     */
    private void openHostLobby() throws Exception {
        LobbyScreen lobbyScreen = new LobbyScreen(hostRoster,true,() -> {
            if (hostClient == null || !hostClient.isConnected()) {
            throw new IllegalStateException("Host is Disconected");
            }
            hostClient.startGame();
        });
        lobbyScreen.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (hostClient != null) {
                    hostClient.disconnect();
                    hostClient = null;
                }
                if (hostedLobby != null) {
                    hostedLobby.close();
                    hostedLobby = null;
                }
                resetTransition();
                MainScreen.this.setVisible(true);
            }
        });
        MainScreen.this.setVisible(false);
    }
/**
     * Opens the joined lobby screen, allowing the client to view the game lobby and wait for the host to start the game.
     * This method creates a new LobbyScreen instance and sets up a window listener to handle cleanup when the lobby is closed.
     *
     * @throws Exception If an error occurs while opening the lobby.
     */
    private void openJoinedLobby() throws Exception {

        LobbyScreen lobbyScreen = new LobbyScreen(joinedRoster);

        lobbyScreen.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {

                if (joinedClient != null) {
                    joinedClient.disconnect();
                    joinedClient = null;
                }

                resetTransition();
                MainScreen.this.setVisible(true);
            }
        });

        MainScreen.this.setVisible(false);
    }

    private void startTransition() {

        animationFinished = false;
        connectionFinished = false;
        pendingLobbyAction = null;

        setMenuVisible(false);

        animatedBackGround.playOnce(
                JOIN_BACKGROUND,
                FPS,
                () -> {
                    animationFinished = true;
                    checkTransition();
                }
        );
    }
    private void connectionReady(Runnable openLobby){
        connectionFinished = true;
        pendingLobbyAction = openLobby;

        checkTransition();
    }

    private void checkTransition(){
        if (!animationFinished || !connectionFinished) {
            return;
        }
        if (pendingLobbyAction == null) {
            return;
        }
        Runnable action = pendingLobbyAction;
        pendingLobbyAction = null;
        action.run();
    }

    private void resetTransition() {

        animationFinished = false;
        connectionFinished = false;
        pendingLobbyAction = null;

        animatedBackGround.playLoop(NORMAL_BACKGROUND, FPS);

        setMenuVisible(true);
    }


    private void transitionFailed(String message) {

        joiningServer = false;
        hostingServer = false;

        if (joinedClient != null) {
            joinedClient.disconnect();
            joinedClient = null;
        }

        if (hostClient != null) {
            hostClient.disconnect();
            hostClient = null;
        }

        if (hostedLobby != null) {
            hostedLobby.close();
            hostedLobby = null;
        }

        setCursor(Cursor.getDefaultCursor());
        resetTransition();

        JOptionPane.showMessageDialog(
                this,
                message,
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private boolean isConnectionBusy() {
        return hostingServer
                || joiningServer
                || (hostedLobby != null && hostedLobby.isRunning())
                || (hostClient != null && hostClient.isConnected())
                || (joinedClient != null && joinedClient.isConnected());
    }
    private void setMenuVisible(boolean visible) {

        for (JPanel panel : menuPanels) {
            panel.setVisible(visible);
        }

        animatedBackGround.revalidate();
        animatedBackGround.repaint();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainScreen::new);
    }

}
/**
 * A record to encapsulate the result of a connection attempt, including the connection values and the generated server code.
 *
 * @param connection The connection values containing host name, IP address, and port.
 * @param code       The generated server code for the connection.
 */
record ConnectionResult (
        connectionValues connection,
        String code
){}
/**
 * A record to represent a setting option and its corresponding value.
 *
 * @param option The name of the setting option.
 * @param value  The value associated with the setting option.
 */
record Settings(String option, Object value) {
    public static Object[][] toArray(Settings[] settings) {
        Object[][] result = new Object[settings.length][2];
        for (int i = 0; i < settings.length; i++) {
            result[i][0] = settings[i].option();
            result[i][1] = settings[i].value();
        }
        return result;
    }
}
/**
 * A record to encapsulate the details of a hosted session, including the lobby control, client, and player ID.
 *
 * @param lobby    The LobbyControl instance managing the hosted game.
 * @param client   The Client instance representing the host's connection.
 * @param playerId The unique identifier for the host player.
 */
record HostedSession(LobbyControl lobby, Client client, int playerId){};
