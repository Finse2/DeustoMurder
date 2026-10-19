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
    ImageIcon icon = new ImageIcon("innerProy/ressources/DefaultProfile.jpg");
    Image image = icon.getImage();
    Image reseizedImage = image.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
    JButton profile = new JButton(new ImageIcon(reseizedImage));
    profile.setOpaque(false);
    profile.setBorderPainted(false);
    profile.setContentAreaFilled(false);
    profile.setFocusPainted(false);
    northPanel.add(profile, BorderLayout.EAST);
    add(northPanel, BorderLayout.NORTH);

    // profile JTable
    profile.addActionListener(e -> {
        openProfileStats();
    });



        // WEST MENU BUTTONS

    JPanel menuPanel = new JPanel(new GridLayout(3, 1,10,10));
    menuPanel.setPreferredSize(new Dimension(250,220));
    menuPanel.setOpaque(false);

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


        // Center MENU
    JPanel centerPanel = new JPanel();
    JLabel welcome = new JLabel("Welcome to Deusto Murder");
    JLabel welcome2 = new JLabel("A Deusto version of the famous game Cluedo");
    centerPanel.add(welcome);
    centerPanel.add(welcome2);
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
        JTable statsTable = new JTable(data,colums);
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