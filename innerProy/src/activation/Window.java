package activation;

import model.Tile;
import ui.TileAspect;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    public Window() {

        setTitle("Cluedo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(500, 500);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(new Color(235, 235, 235));

        TileAspect tileAspect = new TileAspect(new Tile(null));

        content.add(tileAspect);

        add(content);

        setVisible(true);
    }
}