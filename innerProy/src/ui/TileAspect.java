package ui;

import model.Tile;

import javax.swing.*;
import java.awt.*;

public class TileAspect extends JPanel {

    public enum TileDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    public static final int SIZE = 30;

    private final Tile tile;

    private Color color =
            Color.WHITE;

    private int x;
    private int y;

    public TileAspect(Tile tile) {

        this(
                tile,
                0,
                0
        );
    }

    public TileAspect(
            Tile tile,
            int x,
            int y
    ) {

        this.tile = tile;

        this.x = x;
        this.y = y;

        setBounds(
                x,
                y,
                SIZE,
                SIZE
        );

        setBackground(color);

        setGridLineThickness(1);
    }

    public Tile getTile() {
        return tile;
    }

    public void setGridLineThickness(
            int thickness
    ) {

        setBorder(
                BorderFactory.createLineBorder(
                        new Color(
                                145,
                                145,
                                145
                        ),

                        Math.max(
                                1,
                                thickness
                        )
                )
        );
    }

    public void darker() {

        color = color.darker();

        setBackground(color);
    }

    public void concatenate(
            JPanel root,
            TileDirection direction
    ) {

        concatenate(
                root,
                direction,
                1
        );
    }

    public void concatenate(
            JPanel root,
            TileDirection direction,
            int numberOfTiles
    ) {

        if (numberOfTiles <= 0) {
            return;
        }

        int newX = x;
        int newY = y;

        switch (direction) {

            case NORTH ->
                    newY -= SIZE;

            case SOUTH ->
                    newY += SIZE;

            case EAST ->
                    newX += SIZE;

            case WEST ->
                    newX -= SIZE;
        }

        TileAspect newTile =
                new TileAspect(
                        new Tile(null),
                        newX,
                        newY
                );

        root.add(newTile);

        newTile.concatenate(
                root,
                direction,
                numberOfTiles - 1
        );

        root.revalidate();
        root.repaint();
    }
}