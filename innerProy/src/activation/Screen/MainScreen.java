package activation.Screen;

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

    JPanel menuPanel = new JPanel(new GridLayout(7, 1,10,10));
    menuPanel.setPreferredSize(new Dimension(250,220));
    menuPanel.setOpaque(false);
    JLabel empty = new JLabel();
    menuPanel.add(empty);
    JLabel empty2 = new JLabel();
    menuPanel.add(empty2);
    JButton HostButton = new JButton("Host Game");
    HostButton.setPreferredSize(new Dimension(250,10));
    JButton JoinButton = new JButton("Join Game");
    JoinButton.setPreferredSize(new Dimension(250,10));
    JButton ExitButton = new JButton("Exit Game");
    ExitButton.setPreferredSize(new Dimension(250,10));

    menuPanel.add(HostButton);
    menuPanel.add(JoinButton);
    menuPanel.add(ExitButton);

    add(menuPanel, BorderLayout.WEST);

    // EAST MENU BUTTONS
    JPanel eastPanel = new JPanel();
    eastPanel.setPreferredSize(new Dimension(250, 220));
    eastPanel.setOpaque(false);
    add(eastPanel, BorderLayout.EAST);




        // Center MENU
    JPanel centerPanel = new JPanel(new BorderLayout());
    JLabel welcome = new JLabel("Welcome to Deusto Murder", SwingConstants.CENTER);
    JLabel welcome2 = new JLabel("A Deusto version of the famous game Cluedo", SwingConstants.CENTER);
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

        JScrollPane scrollPane = new JScrollPane(settingsTable);
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

        JScrollPane scrollPane = new JScrollPane(statsTable);
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