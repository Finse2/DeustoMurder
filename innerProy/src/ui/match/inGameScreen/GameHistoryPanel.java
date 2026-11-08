package ui.match.inGameScreen;

import javax.swing.*;
import java.awt.*;

/** Contenedor del registro de la partida. Su contenido se implementará posteriormente. */
public final class GameHistoryPanel extends JPanel {

    public GameHistoryPanel() {
        super(new BorderLayout());
        setBackground(new Color(25, 55, 75));

        JLabel title = new JLabel("Registro de la partida", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(title, BorderLayout.NORTH);
    }
}
