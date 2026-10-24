package activation.Screen;

import Networking.Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainScreen extends JFrame {
    private static final Path PROFILE_PATH = Path.of(System.getProperty("user.home"),".deustoCluedo","profile.png");
    private static final Path DEFAULT_PROFILE_PATH = Path.of("innerProy", "ressources", "DefaultProfile.jpg");
    private ProfileButton profileButton;

    public MainScreen() {
        setTitle("Deusto Cluedo");
        setSize(900,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackGround backGround = new BackGround("innerProy/ressources/mainMenuBackground.png");
        setContentPane(backGround);


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
    welcome.setForeground(new Color(235,215,100));
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

    setVisible(true);
    }

    private void HostGame() {
        ConnectionResult connection = openConnection();
        System.out.println(connection.toString());
    }

    private void JoinGame() {

        connectionValues clientConnect = ClientConnection();
        if (clientConnect == null) {
            return;
        }
        System.out.println(clientConnect.toString());
    }
    private void Exit() {
        dispose();
    }


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

        JTable settingsTable = new JTable(Settings.toArray(data),colums) {
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
                {"correct Accusations",0}
        };
        JTable statsTable = new JTable(data,colums) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainScreen::new);
    }
}

record ConnectionResult (
        connectionValues connection,
        String code
){}

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
