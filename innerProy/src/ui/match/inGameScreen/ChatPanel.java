 package ui.match.inGameScreen;

import javax.swing.*;
import java.awt.*;

/** Contenedor del chat de partida. Su contenido se implementará posteriormente. */
public final class ChatPanel extends JPanel {

    public ChatPanel() {
        super(new BorderLayout());
        setBackground(new Color(25, 55, 75));

        JLabel title = new JLabel("Chat", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(title, BorderLayout.NORTH);
    }
}
