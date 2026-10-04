package activation;

import model.Actor;
import model.Room;
import model.Weapon;
import ui.BoardLayout;
import ui.DetectiveCard;
import ui.DetectiveButton;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Window extends JFrame {

    public Window(List<Room> rooms, List<Actor> actors, List <Weapon> weapons) {

        setTitle(
                "Deusto Murder - Board"
        );

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        BoardLayout board = new BoardLayout(rooms);

        JPanel mainPanel = new JPanel(new BorderLayout());

        DetectiveButton detectiveCardButton = new DetectiveButton();

        detectiveCardButton.addActionListener(e -> {
            DetectiveCard.showCard(
                    actors,
                    weapons,
                    rooms
            );
        });

        // PANEL SUPERIOR

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        topPanel.setPreferredSize(new Dimension(0, 100));
        topPanel.add(detectiveCardButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(board, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // TAMAÑO DE LA VENTANA

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int side = (int) Math.min(screenSize.width * 0.8, screenSize.height * 0.8);

        setSize(new Dimension (side, side));
        setMinimumSize(new Dimension (650, 650));

        setLocationRelativeTo(null);
        setVisible(true);

    }
}