package ui;

import model.Room;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

class RoomsLook extends JComponent {

    private final Room room;
    private final int minColumn;
    private final int minRow;
    private final int maxColumn;
    private final int maxRow;
    private int tileSize = 1;
    private Area localRoomShape = new Area();

    RoomsLook(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }

        if (room.getOccupiedCells().isEmpty()) {
            throw new IllegalArgumentException("Room '" + room.getName() + "' does not occupy any grid cells");
        }

        this.room = room;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Room.Cell cell : room.getOccupiedCells()) {
            minX = Math.min(minX, cell.column());
            minY = Math.min(minY, cell.row());
            maxX = Math.max(maxX, cell.column());
            maxY = Math.max(maxY, cell.row());
        }

        minColumn = minX;
        minRow = minY;
        maxColumn = maxX;
        maxRow = maxY;
        setOpaque(false);
    }

    void placeOnGrid(int gridStartX, int gridStartY, int tileSize) {
        this.tileSize = tileSize;

        int widthInTiles = maxColumn - minColumn + 1;
        int heightInTiles = maxRow - minRow + 1;

        setBounds(
                gridStartX + minColumn * tileSize,
                gridStartY + minRow * tileSize,
                widthInTiles * tileSize,
                heightInTiles * tileSize
        );

        rebuildShape();
    }

    private void rebuildShape() {
        Area shape = new Area();

        for (Room.Cell cell : room.getOccupiedCells()) {
            int localColumn = cell.column() - minColumn;
            int localRow = cell.row() - minRow;

            shape.add(new Area(new Rectangle2D.Double(
                    localColumn * tileSize,
                    localRow * tileSize,
                    tileSize,
                    tileSize
            )));
        }

        localRoomShape = shape;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2 = (Graphics2D) graphics.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(localRoomShape);

            float wallThickness = Math.max(2.0f, tileSize / 10.0f);

            g2.setStroke(new BasicStroke(
                    wallThickness,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER
            ));

            g2.setColor(Color.BLACK);
            g2.draw(localRoomShape);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public boolean contains(int x, int y) {
        return localRoomShape.contains(x, y);
    }
}
