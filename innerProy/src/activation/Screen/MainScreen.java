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
    JLabel Title = new JLabel("Deusto Cluedo", SwingConstants.CENTER);
    Title.setFont(new Font("Arial", Font.BOLD, 32));
    Title.setForeground(Color.RED);
    add(Title, BorderLayout.NORTH);


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
    public static void main(String[] args) {
        MainScreen m = new MainScreen();
    }

}