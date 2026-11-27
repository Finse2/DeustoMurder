package ui.match;

import model.Piece;
import model.board.Tile;
import utils.AllPaths.DefaultCardModels.ActorsData;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.Objects;

class TileAspect extends JPanel {

    enum TileDirection {NORTH, SOUTH, EAST, WEST}

    static final int SIZE = 30;

    private final Tile tile;
    private Color color = Color.WHITE;
    private boolean reachable;
    private boolean tokenHidden;

    void setTokenHidden(boolean hidden) {
        tokenHidden = hidden;
        repaint();
    }
    private String activePlayerName;
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
        setOpaque(false);
        repaint();
    }

    void setGridLineThickness(int thickness) {
        setBorder(BorderFactory.createLineBorder(
                new Color(145, 145, 145), Math.max(1, thickness)));
    }

    void setReachable(boolean value) {
        reachable = value;
        repaint();
    }

    void setActivePlayer(String playerName) {
        activePlayerName = playerName;
        repaint();
    }

    void darker() {
        color = color.darker();
        setBackground(color);
    }

    // Recursive tile creation
    void concatenate(JPanel root, TileDirection direction) {
        concatenate(root, direction, 1);
    }

    void concatenate(JPanel root, TileDirection direction, int numberOfTiles) {
        if (numberOfTiles <= 0) return;

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

    static Image getPlayerPortrait(Piece piece) {
        if (piece == null || piece.getOwner() == null) return null;

        String name = piece.getOwner().getName();
        for (ActorsData data : ActorsData.values()) {
            if (data.getName().equalsIgnoreCase(name)) {
                Image portrait = data.getMainBackgroundImage();
                return portrait != null ? portrait : data.getFrontCircleImage();
            }
        }
        // Fallback for players whose display name differs from the actor name.
        for (ActorsData data : ActorsData.values()) {
            if (Objects.equals(data.getColor(), piece.getColor())) {
                Image portrait = data.getMainBackgroundImage();
                return portrait != null ? portrait : data.getFrontCircleImage();
            }
        }
        return null;
    }

    private void paintTileIndicator(Graphics graphics) {
        if (Color.WHITE.equals(color)) return;

        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = Math.max(1, getWidth() - 4);
            int height = Math.max(1, getHeight() - 4);

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 65));
            g2.fillRoundRect(2, 2, width, height, 6, 6);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawRoundRect(2, 2, width, height, 6, 6);

            int size = Math.max(2, Math.min(getWidth(), getHeight()) / 5);
            g2.fillOval((getWidth() - size) / 2,
                    (getHeight() - size) / 2, size, size);
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        paintTileIndicator(graphics);

        if (reachable) {
            Graphics2D overlay = (Graphics2D) graphics.create();
            try {
                overlay.setColor(new Color(60, 205, 115, 105));
                overlay.fillRect(2, 2, Math.max(0, getWidth() - 4),
                        Math.max(0, getHeight() - 4));
            } finally {
                overlay.dispose();
            }
        }

        Piece piece = tile.getPieceOnThisTile();
        if (piece == null || tokenHidden) return;

        int tileSize = Math.min(getWidth(), getHeight());
        int margin = Math.max(3, tileSize / 12);
        int tokenSize = tileSize - margin * 2;
        int tokenX = (getWidth() - tokenSize) / 2;
        int tokenY = (getHeight() - tokenSize) / 2;

        boolean activePlayer = piece.getOwner() != null
                && activePlayerName != null
                && Objects.equals(piece.getOwner().getName(), activePlayerName);

        PlayerTokenRenderer.paint(graphics, tokenX, tokenY, tokenSize,
                piece.getColor(), getPlayerPortrait(piece), activePlayer);
    }
}
