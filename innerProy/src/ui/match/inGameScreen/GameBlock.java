package ui.match.inGameScreen;

import javax.swing.*;
import java.awt.*;

public class GameBlock extends JPanel {

    public GameBlock(String title, Color color) {
        setLayout(new BorderLayout());
        setBackground(color);

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 25));

        add(label, BorderLayout.CENTER);
    }
}