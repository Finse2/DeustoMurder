package ui.menu;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Objects;

/** Menu window. Receives its navigation actions from the app layer. */
public final class MainScreen extends JFrame {

    public MainScreen(Runnable onHost, Runnable onJoin) {
        Objects.requireNonNull(onHost, "onHost");
        Objects.requireNonNull(onJoin, "onJoin");

        setTitle("Deusto Cluedo");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BackGround background = new BackGround("innerProy/ressources/mainMenuBackground.png");
        setContentPane(background);

        JLabel title = new JLabel("Deusto Cluedo", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(Color.RED);
        add(title, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        menuPanel.setPreferredSize(new Dimension(250, 220));
        menuPanel.setOpaque(false);

        JButton hostButton = new JButton("Host Game");
        JButton joinButton = new JButton("Join Game");
        JButton exitButton = new JButton("Exit Game");

        hostButton.addActionListener(event -> {
            onHost.run();
            dispose();
        });
        joinButton.addActionListener(event -> onJoin.run());
        exitButton.addActionListener(event -> dispose());

        menuPanel.add(hostButton);
        menuPanel.add(joinButton);
        menuPanel.add(exitButton);
        add(menuPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.add(new JLabel("Welcome to Deusto Murder"));
        centerPanel.add(new JLabel("A Deusto version of the famous game Cluedo"));
        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.add(new JLabel("Deusto Clue version 1.0", SwingConstants.CENTER));
        add(southPanel, BorderLayout.SOUTH);
    }
}
