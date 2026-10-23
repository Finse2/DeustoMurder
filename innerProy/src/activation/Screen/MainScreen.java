package activation.Screen;

import Networking.Client;

import javax.swing.*;
import java.awt.*;

public class MainScreen extends JFrame {

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
    ImageIcon icon = new ImageIcon("innerProy/ressources/DefaultProfile.jpg");
    Image image = icon.getImage();
    Image reseizedImage = image.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
    JButton profile = new JButton(new ImageIcon(reseizedImage));
    profile.setOpaque(false);
    profile.setBorderPainted(false);
    profile.setContentAreaFilled(false);
    profile.setFocusPainted(false);
    ImageIcon icon1 = new ImageIcon("innerProy/ressources/settings.png");
    Image image1 = icon1.getImage();
    Image reseizedImage1 = image1.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
    JButton settings = new JButton(new ImageIcon(reseizedImage1));
    settings.setOpaque(false);
    settings.setBorderPainted(false);
    settings.setContentAreaFilled(false);
    settings.setFocusPainted(false);
    buttonPanel.add(settings);
    buttonPanel.add(profile);
    northPanel.add(buttonPanel, BorderLayout.EAST);

    JPanel westEqualizePanel = new JPanel();
    westEqualizePanel.setOpaque(false);
    Dimension eastSize = buttonPanel.getPreferredSize();
    westEqualizePanel.setPreferredSize(eastSize);
    northPanel.add(westEqualizePanel, BorderLayout.WEST);
    add(northPanel, BorderLayout.NORTH);

    // profile JTable
    profile.addActionListener(e -> {
        openProfileStats();
    });

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
        connectionValues connection = openConnection();
        System.out.println(connection.toString());
    }

    private void JoinGame() {
        openConnection();
    }

    private void Exit() {
        dispose();
    }

    private connectionValues openConnection() {
        connectionValues connect = new connectionValues();

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

        JScrollPane scrollPane = new JScrollPane(connectionTable);
        connectionDialog.add(scrollPane, BorderLayout.CENTER);

        JButton confirmButton = new JButton("Confirm");

        confirmButton.addActionListener(e -> {
            try {
                // Make sure the current cell edit is saved
                if (connectionTable.isEditing()) {
                    connectionTable.getCellEditor().stopCellEditing();
                }

                String hostName = connectionTable.getValueAt(0, 1).toString();
                String ipAddress = connectionTable.getValueAt(1, 1).toString();
                int port = Integer.parseInt(
                        connectionTable.getValueAt(2, 1).toString()
                );

                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException("Invalid port");
                }

                connect.setHostName(hostName);
                connect.setIpAddress(ipAddress);
                connect.setPort(port);

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
            }
        });

        connectionDialog.add(confirmButton, BorderLayout.SOUTH);

        connectionDialog.setSize(450, 300);
        connectionDialog.setLocationRelativeTo(this);
        connectionDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        connectionDialog.setVisible(true);

        return connect;
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

        JScrollPane scrollPane = CluedoTableTheme.style(settingsTable);
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

        statFrame.setSize(450, 300);
        statFrame.setLocationRelativeTo(this);
        statFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        statFrame.setVisible(true);


    }
    public static void main(String[] args) {
        MainScreen m = new MainScreen();
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