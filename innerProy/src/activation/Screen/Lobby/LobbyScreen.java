package activation.Screen.Lobby;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import activation.Screen.*;
import model.Player;

public class LobbyScreen extends JFrame {
    private static final Path PROFILE_PATH = Path.of(System.getProperty("user.home"), ".deustoCluedo", "profile.png");
    private static final Path DEFAULT_PROFILE_PATH = Path.of("innerProy", "ressources", "DefaultProfile.jpg");
    private ProfileButton profileButton;
    private LoadingSpinner loadingSpinner;
    private LobbyRoster roster;
    private JPanel playerListPanel;
    private JLabel playerCountLabel;
    private String ServerIP = connectionValues.getIpAddress();
    private int port = connectionValues.getPort();
    private String HostNAME = connectionValues.getHostName();
    private BackGround lobbyBackGround;


    public LobbyScreen(LobbyRoster roster) throws Exception {
        this(roster, false,null);

    }
    public LobbyScreen(LobbyRoster roster,boolean isHost,Runnable onStartGame) throws Exception {
        this.roster = java.util.Objects.requireNonNull(roster, "roster required");
        setTitle("Lobby");
        setSize(1200, 900);



        //crear el panel principal
        lobbyBackGround = new BackGround("innerProy/ressources/LobbyClaustro.png");
        lobbyBackGround.setLayout(new BorderLayout());
        JPanel lobbyPanel = new JPanel(new BorderLayout());
        lobbyPanel.setOpaque(false);

        //Crear el panel del norte
        JPanel northPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        northPanel.setOpaque(false);
        String code = ServerCode.Encode(ServerIP,port,HostNAME);

        InlaidMenuButton infoServer= new InlaidMenuButton("Server Information:", this::infoServer);
        infoServer.setFont(new Font("Georgia", Font.BOLD, 18));
        infoServer.setForeground(new Color(235, 215, 100));
        infoServer.setOpaque(false);
        northPanel.add(infoServer);

        InlaidMenuButton copy = new InlaidMenuButton("Copy code : " + code, this::copy);

        northPanel.add(copy);
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
        northPanel.add(buttonPanel);

        // añadir el panel del norte al principal
        lobbyPanel.add(northPanel, BorderLayout.NORTH);

        //crear el panel de la izquierda(info de los jugadores)

        // PLAYER LIST
        JPanel westPanel = new JPanel(new BorderLayout(5, 5));
        westPanel.setPreferredSize(new Dimension(300, 0));

        westPanel.setOpaque(false);
        playerCountLabel = new JLabel("Connected Players (0)");
        playerCountLabel.setFont(
                new Font("Georgia", Font.BOLD, 18)
        );
        playerCountLabel.setForeground(new Color(235, 215, 100));
        playerCountLabel.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        westPanel.add(playerCountLabel, BorderLayout.NORTH);

        // Container that will hold the player cards
        playerListPanel = new JPanel();
        playerListPanel.setLayout(
                new BoxLayout(playerListPanel, BoxLayout.Y_AXIS)
        );
        playerListPanel.setOpaque(false);
        JScrollPane playerScroll = new JScrollPane(playerListPanel);
        playerScroll.setBorder(null);
        playerScroll.setOpaque(false);
        playerScroll.getVerticalScrollBar().setUnitIncrement(16);

        westPanel.add(playerScroll, BorderLayout.CENTER);

        lobbyPanel.add(westPanel, BorderLayout.WEST);

        //


        JPanel eastPanel = new JPanel(new BorderLayout(5,5));
        eastPanel.setPreferredSize(new Dimension(300, 0));
        eastPanel.setOpaque(false);

        lobbyPanel.add(eastPanel, BorderLayout.EAST);


        //crear el panel del sur
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        JLabel southVersion = new JLabel("Game Version: 1.0");
        southVersion.setFont(new Font("Arial", Font.BOLD, 20));
        southVersion.setForeground(new Color(235, 215, 100));
        southPanel.add(southVersion);
        //añadir el panel del sur al panel principal
        lobbyPanel.add(southPanel,BorderLayout.SOUTH);

        // crear el panel central
        JPanel centralPanel = new JPanel(new BorderLayout(5, 5));
        centralPanel.setOpaque(false);
        centralPanel.setPreferredSize(new Dimension(230, 210));

        JLabel vacio1 = new JLabel(" ");
        vacio1.setOpaque(false);
        centralPanel.add(vacio1, BorderLayout.EAST);

        JLabel vacio2 = new JLabel(" ");
        vacio2.setOpaque(false);
        centralPanel.add(vacio2, BorderLayout.WEST);

        // crear la loading animation
        JPanel loadingPanel = new JPanel(new GridLayout(5,3,5,15));
        loadingPanel.setOpaque(false);
        loadingPanel.setPreferredSize(new Dimension(230,210));
        JLabel empty = new JLabel();
        loadingPanel.add(empty);

       loadingSpinner = new LoadingSpinner();
        loadingPanel.add(loadingSpinner);
        JPanel spinnerContainer = new JPanel(new BorderLayout());
        spinnerContainer.setOpaque(false);

        JLabel empty2 = new JLabel();
        loadingPanel.add(empty2);

        //añadir la animacion al panel de carga
        spinnerContainer.add(loadingPanel, BorderLayout.CENTER);
        JPanel spinnerwestPanel = new JPanel();
        spinnerwestPanel.setOpaque(false);
        spinnerwestPanel.setPreferredSize(new Dimension(100, 210));
        JLabel empty3 = new JLabel();
        spinnerwestPanel.add(empty3);

        JPanel spinnereastPanel = new JPanel();
        spinnereastPanel.setPreferredSize(new Dimension(100, 210));
        spinnereastPanel.setOpaque(false);
        JLabel empty4 = new JLabel();
        spinnereastPanel.add(empty4);

        spinnerContainer.add(spinnerwestPanel, BorderLayout.WEST);
        spinnerContainer.add(spinnereastPanel, BorderLayout.EAST);

        //añadir el panl de carga al panel central
        centralPanel.add(spinnerContainer, BorderLayout.CENTER);

        // crear el panel de botones
        JPanel botonesPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        botonesPanel.setPreferredSize(new Dimension(230, 100));
        botonesPanel.setOpaque(false);

        //crear el boton de salida(disponible para todos)
        InlaidMenuButton exitButton = new InlaidMenuButton("Salir", this::Exit);
        exitButton.setSize(new Dimension(200,100));

        botonesPanel.add(exitButton);

        //crear el boton de inicio de partida (solo visible para el host)
        InlaidMenuButton inicioButton= new InlaidMenuButton("Iniciar juego", this::INICIAR);
        inicioButton.setSize(new Dimension(230,210));
        inicioButton.setOpaque(false);


        if (isHost){
                    inicioButton.setEnabled(true);
        }
        loadingPanel.add(inicioButton);

        //añadir el panel de botones al panel central
        centralPanel.add(botonesPanel, BorderLayout.SOUTH);

        //añadir el panel central al panel principal
        lobbyPanel.add(centralPanel, BorderLayout.CENTER);

        loadingSpinner.start();

        lobbyBackGround.add(lobbyPanel, BorderLayout.CENTER);
        setContentPane(lobbyBackGround);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        roster.setOnChanged(this::refreshPlayers);
        setVisible(true);

    }

    private void infoServer() {

        JFrame infoServer = new JFrame("Server Informatios");

            String[] colums = {"Information", "Value"};

            Object[][] data = {
                    {"Host Name:", HostNAME},
                    {"IP Address:", ServerIP},
                    {"Port:", port},
                    {"Session ID ", "1"}
            };
            JTable statsTable = new JTable(data, colums) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make all cells non-editable
                }
            };

            JScrollPane scrollPane = CluedoTableTheme.style(statsTable);
            infoServer.getContentPane().setBackground(new Color(20, 22, 27));
            infoServer.add(scrollPane);


            infoServer.setSize(450, 300);
            infoServer.setLocationRelativeTo(this);
            infoServer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            infoServer.setVisible(true);



    }


    private void INICIAR() {

    }

    private void Exit() {
        dispose();
    }

    @Override
    public void dispose() {
        roster.setOnChanged(null);
        if (loadingSpinner != null) {
            loadingSpinner.stop();
        }
        super.dispose();
    }

    private void refreshPlayers() {

        playerListPanel.removeAll();

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
                                    new Color(85, 65, 55)
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

            JLabel details = new JLabel(
                    "ID: " + player.playerID()
                            + " | " + player.computerName()
            );

            details.setFont(new Font("Arial", Font.PLAIN, 11));

            infoPanel.add(name);
            infoPanel.add(details);

            playerPanel.add(infoPanel, BorderLayout.CENTER);

            playerListPanel.add(playerPanel);
            playerListPanel.add(Box.createVerticalStrut(6));
        }

        playerListPanel.revalidate();
        playerListPanel.repaint();
    }


    private void openSettings() {
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

    private void openProfileStats() {

        JFrame statFrame = new JFrame("Profile Statistics");

        String[] colums = {"Statistic", "Value"};

        Object[][] data = {
                {"Games Played", 0},
                {"Games Won", 0},
                {"Games Lost", 0},
                {"Win Rate", "0%"},
                {"correct Accusations", 0}
        };
        JTable statsTable = new JTable(data, colums) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        JScrollPane scrollPane = CluedoTableTheme.style(statsTable);
        statFrame.getContentPane().setBackground(new Color(20, 22, 27));
        statFrame.add(scrollPane);

        JButton changePicture = createDialogButton(
                "Change Profile Picture"
        );

        changePicture.addActionListener(e -> changeProfilePicture());
        statFrame.add(changePicture, BorderLayout.SOUTH);

        statFrame.setSize(450, 300);
        statFrame.setLocationRelativeTo(this);
        statFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        statFrame.setVisible(true);


    }

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

    private BufferedImage loadProfilePicture() {
        BufferedImage saved = readImage(PROFILE_PATH);
        if (saved != null) {
            return saved;
        }
        return readImage(DEFAULT_PROFILE_PATH);
    }

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

    private void copy() {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ServerCode.Encode(this.ServerIP,this.port,this.HostNAME)),null);
                JOptionPane.showConfirmDialog(this, "Copied Code");
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, "Clipboard is unavailable", "Copy Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    private BufferedImage resizeIfNecessary(BufferedImage source, int maxSide) {
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();
        if (originalWidth <= maxSide && originalHeight <= maxSide) {
            return source;
        }

        double factor = Math.min((double) maxSide / originalWidth,
                (double) maxSide / originalHeight);
        int width = Math.max(1, (int) Math.round(originalWidth * factor));
        int height = Math.max(1, (int) Math.round(originalHeight * factor));
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(source, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }
}
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