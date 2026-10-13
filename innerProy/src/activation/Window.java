package activation;

import model.Actor;
import model.Room;
import model.Weapon;
import ui.BoardLayout;
import ui.DetectiveCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class Window extends JFrame {

    Window(BoardLayout board, List<Actor> actors, List<Weapon> weapons, List<Room> rooms) {
        setTitle("Deusto Murder - Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JButton detectiveCardButton = new JButton("Tarjeta del detective");

        detectiveCardButton.addActionListener(e -> DetectiveCard.showCard(actors, weapons, rooms));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(detectiveCardButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(board, BorderLayout.CENTER);
        setContentPane(mainPanel);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int side = (int) Math.min(screenSize.width * 0.80, screenSize.height * 0.80);

        setSize(side, side);
        setMinimumSize(new Dimension(650, 650));
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
