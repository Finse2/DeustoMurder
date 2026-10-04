package activation;

import model.Actor;
import model.Room;
import model.Weapon;
import ui.BoardLayout;
import ui.DetectiveCard;

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

        JButton detectiveCardButton = new JButton("Tarjeta del detective");

        detectiveCardButton.addActionListener(e -> {
            DetectiveCard.showCard(
                    actors,
                    weapons,
                    rooms
            );
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        topPanel.add(detectiveCardButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(board, BorderLayout.CENTER);

        setContentPane(mainPanel);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int side = (int) Math.min(screenSize.width * 0.8, screenSize.height * 0.8);

        setSize(new Dimension (side, side));
        setMinimumSize(new Dimension (650, 650));

        setLocationRelativeTo(null);
        setVisible(true);

    }
}