package ui;

import model.Piece;
import model.Tile;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

class TileAspect extends JPanel {

    enum TileDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    static final int SIZE = 30;

    private final Tile tile;
    private Color color = Color.WHITE;
    private int x;
    private int y;

    TileAspect(Tile tile) {
        this(tile, 0, 0);
    }

    TileAspect(Tile tile, int x, int y) {
        this.tile = tile;
        this.x = x;
        this.y = y;
        setBounds(x, y, SIZE, SIZE);
        setBackground(color);
        setGridLineThickness(1);
    }

    Tile getTile() {
        return tile;
    }

    void setTileColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Tile color cannot be null");
        }

        this.color = color;
        setBackground(color);
        repaint();
    }

    void setGridLineThickness(int thickness) {
        setBorder(BorderFactory.createLineBorder(
                new Color(145, 145, 145),
                Math.max(1, thickness)
        ));
    }

    void darker() {
        color = color.darker();
        setBackground(color);
    }

    void concatenate(JPanel root, TileDirection direction) {
        concatenate(root, direction, 1);
    }

    void concatenate(JPanel root, TileDirection direction, int numberOfTiles) {
        if (numberOfTiles <= 0) {
            return;
        }

        int newX = x;
        int newY = y;

        switch (direction) {
            case NORTH -> newY -= SIZE;
            case SOUTH -> newY += SIZE;
            case EAST -> newX += SIZE;
            case WEST -> newX -= SIZE;
        }

        TileAspect newTile = new TileAspect(new Tile(null), newX, newY);
        root.add(newTile);
        newTile.concatenate(root, direction, numberOfTiles - 1);
        root.revalidate();
        root.repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Piece piece = tile.getPieceOnThisTile();

        if (piece == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) graphics.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margin = Math.max(3, Math.min(getWidth(), getHeight()) / 6);
            int width = Math.max(1, getWidth() - margin * 2);
            int height = Math.max(1, getHeight() - margin * 2);

            g2.setColor(piece.getColor());
            g2.fillOval(margin, margin, width, height);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(Math.max(1.0f, Math.min(getWidth(), getHeight()) / 18.0f)));
            g2.drawOval(margin, margin, width, height);
        } finally {
            g2.dispose();
        }
    }
}
