package activation;

import model.Room;
import ui.BoardLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Window extends JFrame {

    public Window(List<Room> rooms) {

        setTitle(
                "Deusto Murder - Board"
        );

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        BoardLayout board =
                new BoardLayout(rooms);

        setContentPane(board);

        Dimension screenSize =
                Toolkit
                        .getDefaultToolkit()
                        .getScreenSize();

        int side = (int) Math.min(
                screenSize.width * 0.80,
                screenSize.height * 0.80
        );

        setSize(
                side,
                side
        );

        setMinimumSize(
                new Dimension(
                        650,
                        650
                )
        );

        setLocationRelativeTo(null);

        setVisible(true);
    }
}