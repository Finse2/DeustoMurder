package ui;

import model.Room;
import model.Tile;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BoardLayout extends JPanel {

    public static final int GRID_COLUMNS = 23;
    public static final int GRID_ROWS = 22;

    private final List<PlacedTile> corridorTiles = new ArrayList<>();
    private final List<RoomsLook> roomLooks = new ArrayList<>();

    private record PlacedTile(
            TileAspect look,
            int column,
            int row
    ) {
    }

    public BoardLayout(List<Room> rooms) {
        setLayout(null);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(900, 900));

        createBoard(rooms);
    }

    private void createBoard(List<Room> rooms) {
        boolean[][] occupied = new boolean[GRID_ROWS][GRID_COLUMNS];
        Map<Room.Cell, Room> roomByCell = new HashMap<>();

        for (Room room : rooms) {
            for (Room.Cell cell : room.getOccupiedCells()) {
                validateCell(room, cell);

                Room previousRoom = roomByCell.putIfAbsent(cell, room);

                if (previousRoom != null) {
                    throw new IllegalArgumentException(
                            "Rooms '" + previousRoom.getName()
                                    + "' and '" + room.getName()
                                    + "' overlap at grid cell ("
                                    + cell.column() + ", "
                                    + cell.row() + ")"
                    );
                }

                occupied[cell.row()][cell.column()] = true;
            }
        }

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int column = 0; column < GRID_COLUMNS; column++) {

                if (occupied[row][column]) {
                    continue;
                }

                TileAspect tileLook = new TileAspect(new Tile(null));

                add(tileLook);

                corridorTiles.add(
                        new PlacedTile(
                                tileLook,
                                column,
                                row
                        )
                );
            }
        }

        for (Room room : rooms) {

            if (room.getOccupiedCells().isEmpty()) {
                continue;
            }

            RoomsLook roomLook = new RoomsLook(room);

            add(roomLook);

            roomLooks.add(roomLook);
        }
    }

    private void validateCell(Room room, Room.Cell cell) {

        if (
                cell.column() < 0
                        || cell.column() >= GRID_COLUMNS
                        || cell.row() < 0
                        || cell.row() >= GRID_ROWS
        ) {

            throw new IllegalArgumentException(
                    "Room '" + room.getName()
                            + "' uses grid cell ("
                            + cell.column() + ", "
                            + cell.row()
                            + ") outside the "
                            + GRID_COLUMNS + "x"
                            + GRID_ROWS + " board"
            );
        }
    }

    @Override
    public void doLayout() {

        super.doLayout();

        int availableWidth = getWidth();
        int availableHeight = getHeight();

        int margin = Math.max(
                24,
                Math.min(
                        availableWidth,
                        availableHeight
                ) / 28
        );

        int tileSize = Math.min(
                (availableWidth - margin * 2) / GRID_COLUMNS,
                (availableHeight - margin * 2) / GRID_ROWS
        );

        if (tileSize <= 0) {
            return;
        }

        int gridWidth = tileSize * GRID_COLUMNS;
        int gridHeight = tileSize * GRID_ROWS;

        int startX = (availableWidth - gridWidth) / 2;
        int startY = (availableHeight - gridHeight) / 2;

        for (PlacedTile tile : corridorTiles) {

            tile.look().setBounds(
                    startX + tile.column() * tileSize,
                    startY + tile.row() * tileSize,
                    tileSize,
                    tileSize
            );

            tile.look().setGridLineThickness(
                    Math.max(
                            1,
                            tileSize / 24
                    )
            );
        }

        for (RoomsLook roomLook : roomLooks) {
            roomLook.placeOnGrid(
                    startX,
                    startY,
                    tileSize
            );
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        int availableWidth = getWidth();
        int availableHeight = getHeight();

        int margin = Math.max(
                24,
                Math.min(
                        availableWidth,
                        availableHeight
                ) / 28
        );

        int tileSize = Math.min(
                (availableWidth - margin * 2) / GRID_COLUMNS,
                (availableHeight - margin * 2) / GRID_ROWS
        );

        if (tileSize <= 0) {
            return;
        }

        int gridWidth = tileSize * GRID_COLUMNS;
        int gridHeight = tileSize * GRID_ROWS;

        int startX = (availableWidth - gridWidth) / 2;
        int startY = (availableHeight - gridHeight) / 2;

        Graphics2D g2 =
                (Graphics2D) graphics.create();

        try {

            g2.setColor(Color.BLACK);

            g2.setStroke(
                    new BasicStroke(
                            Math.max(
                                    2.0f,
                                    tileSize / 10.0f
                            )
                    )
            );

            g2.drawRect(
                    startX,
                    startY,
                    gridWidth,
                    gridHeight
            );

        } finally {
            g2.dispose();
        }
    }
}