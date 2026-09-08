package ui;

import model.Tile;

import javax.swing.*;
import java.awt.*;

public class TileAspect extends JPanel {

    private final Tile tile;
    private Color color = Color.GRAY;

    public TileAspect(Tile tile) {
        this.tile = tile;

        setPreferredSize(new Dimension(190, 190));
        setBackground(color);

        setBorder(BorderFactory.createLineBorder(Color.BLUE,5));

    }

    public Tile getTile() {
        return tile;
    }

    public void darker() {
        color = color.darker();
        setBackground(color);
    }
}