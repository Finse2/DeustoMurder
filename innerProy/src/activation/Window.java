package activation;

import model.Tile;
import ui.TileAspect;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    public Window() {

        setTitle("Cluedo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1000, 800);
        setLocationRelativeTo(null);

        JPanel board = new JPanel();
        board.setLayout(null);
        board.setBackground(new Color(235, 235, 235));

        board.setPreferredSize(
                new Dimension(1600, 1200)
        );

        final int squareNumberOfElements = 21;
        for (int i = 0; i < squareNumberOfElements; i++) {

            TileAspect firstTile = new TileAspect(
                    new Tile(null),
                    400 + (i * TileAspect.SIZE),
                    300
            );

            board.add(firstTile);

            firstTile.concatenate(
                    board,
                    TileAspect.TileDirection.SOUTH,
                    squareNumberOfElements
            );
        }



        JScrollPane scrollPane = new JScrollPane(board);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        add(scrollPane);

        setVisible(true);
    }
}