package ui;

import model.Tile;

import javax.swing.*;
import java.awt.*;

public class TileAspect extends JPanel {

    private final Tile tile;
    private Color color = Color.WHITE;

    public TileAspect(Tile tile) {
        this.tile = tile;

        setPreferredSize(new Dimension(190, 190));
        setBackground(color);
    }

    public Tile getTile() {
        return tile;
    }

    public void darker() {
        color = color.darker();
        setBackground(color);
    }
}