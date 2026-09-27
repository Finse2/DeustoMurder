package activation;

import javax.swing.*;
import java.awt.*;

public class MainScreen extends JFrame {

    public MainScreen() {
        setTitle("Deusto Cluedo");
        setSize(900,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // NORTH MAIN TITLE
    JLabel Title = new JLabel("Deusto Cluedo", SwingConstants.CENTER);
    Title.setFont(new Font("Arial", Font.BOLD, 32));
    add(Title, BorderLayout.NORTH);


        // WEST MENU BUTTONS
    JPanel menuPanel = new JPanel(new GridLayout(3, 1,10,10));

    JButton HostButton = new JButton("Host Game");
    JButton JoinButton = new JButton("Join Game");
    JButton ExitButton = new JButton("Exit Game");

    menuPanel.add(HostButton);
    menuPanel.add(JoinButton);
    menuPanel.add(ExitButton);

    add(menuPanel, BorderLayout.WEST);


        // Center MENU
    JPanel centerPanel = new JPanel();
    JLabel welcome = new JLabel("Welcome to Deusto Murder \n A Deusto version of the famous game Cluedo");
    centerPanel.add(welcome);
    add(centerPanel, BorderLayout.CENTER);


        // South Menu
    JPanel southPanel = new JPanel();
    JLabel status = new JLabel("Deusto Clue version 1.0", SwingConstants.CENTER);
    southPanel.add(status);
    add(southPanel, BorderLayout.SOUTH);

    setVisible(true);

    }
    public static void main(String[] args) {
        MainScreen m = new MainScreen();
    }

}