package ui;

import model.Piece;
import model.RawPiece;
import model.RawRoom;
import model.Room;
import model.Tile;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BoardLayout extends JPanel {

    public static final int GRID_COLUMNS = 23;
    public static final int GRID_ROWS = 22;

    private final int rows;
    private final int columns;
    private final Tile[][] tiles;
    private final List<Room> rooms = new ArrayList<>();
    private final List<Piece> pieces = new ArrayList<>();
    private final List<PlacedTile> visibleTiles = new ArrayList<>();
    private final List<PlacedTile> roomPositionTiles = new ArrayList<>();
    private final List<RoomsLook> roomLooks = new ArrayList<>();

    private record PlacedTile(TileAspect look, int column, int row) {
    }

    public BoardLayout(int rows, int columns, RawRoom[] rawRooms, RawPiece[] rawPieces) {
        this(rows, columns);
        createRooms(rawRooms == null ? new RawRoom[0] : rawRooms);
        createBoard();
        placePieces(rawPieces == null ? new RawPiece[0] : rawPieces);
    }

    public BoardLayout(List<Room> rooms) {
        this(GRID_ROWS, GRID_COLUMNS);
        this.rooms.addAll(rooms);
        createBoard();
    }

    private BoardLayout(int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Board rows and columns must be positive");
        }

        this.rows = rows;
        this.columns = columns;
        this.tiles = new Tile[rows][columns];

        setLayout(null);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(900, 900));
    }

    public Room checkTileEntrance(int x, int y) {
        return getTileAt(x, y).getAccessibleRoom();
    }

    public boolean checkIsTileDissabled(int x, int y) {
        return getTileAt(x, y).isDisabled();
    }

    public boolean checkIsTileDisabled(int x, int y) {
        return checkIsTileDissabled(x, y);
    }

    public boolean canMovePiece(Piece piece, int x, int y) {
        if (piece == null || x < 0 || x >= columns || y < 0 || y >= rows) {
            return false;
        }

        int pieceX = piece.getX();
        int pieceY = piece.getY();

        if (pieceX < 0 || pieceX >= columns || pieceY < 0 || pieceY >= rows) {
            return false;
        }

        if (tiles[pieceY][pieceX].getPieceOnThisTile() != piece) {
            return false;
        }

        Tile destination = tiles[y][x];
        return !destination.isDisabled()
                && (destination.getPieceOnThisTile() == null || destination.getPieceOnThisTile() == piece);
    }

    public void movePiece(Piece piece, int xorigin, int yorigin, int xdestination, int ydestination) {
        if (piece == null) {
            throw new IllegalArgumentException("Piece cannot be null");
        }

        Tile origin = getTileAt(xorigin, yorigin);
        Tile destination = getTileAt(xdestination, ydestination);

        if (origin.getPieceOnThisTile() != piece) {
            throw new IllegalArgumentException("Piece is not at the supplied origin tile");
        }

        if (destination.isDisabled()) {
            throw new IllegalArgumentException("Destination tile is disabled");
        }

        if (destination.getPieceOnThisTile() != null && destination.getPieceOnThisTile() != piece) {
            throw new IllegalArgumentException("Destination tile is occupied");
        }

        origin.setPieceOnThisTile(null);
        destination.setPieceOnThisTile(piece);
        piece.setPosition(xdestination, ydestination);
        repaint();
    }

    public void enterRoom(Piece piece, Room room) {
        if (piece == null || room == null) {
            throw new IllegalArgumentException("Piece and room cannot be null");
        }

        Tile origin = getTileAt(piece.getX(), piece.getY());

        if (origin.getPieceOnThisTile() != piece) {
            throw new IllegalArgumentException("Piece is not on its recorded tile");
        }

        Room.Cell destinationCell = findEmptyRoomPosition(room);

        if (destinationCell == null) {
            throw new IllegalStateException("No empty blue tile is available in room '" + room.getName() + "'");
        }

        Tile destination = getTileAt(destinationCell.column(), destinationCell.row());

        origin.setPieceOnThisTile(null);
        destination.setPieceOnThisTile(piece);
        piece.setPosition(destinationCell.column(), destinationCell.row());
        repaint();
    }

    public Tile getTileAt(int x, int y) {
        validateCoordinates(x, y);
        return tiles[y][x];
    }

    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

    public Piece[][] getPieces() {
        Piece[][] boardPieces = new Piece[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                boardPieces[row][column] = tiles[row][column].getPieceOnThisTile();
            }
        }

        return boardPieces;
    }

    public List<Piece> getPieceList() {
        return List.copyOf(pieces);
    }

    private void createRooms(RawRoom[] rawRooms) {
        for (RawRoom rawRoom : rawRooms) {
            if (rawRoom == null) {
                throw new IllegalArgumentException("Raw rooms cannot contain null");
            }

            int[] area = rawRoom.area();

            if (area == null || area.length != 4) {
                throw new IllegalArgumentException("Room area must contain x, y, width and height");
            }

            Room room = new Room(rawRoom.name()).occupyRectangle(area[0], area[1], area[2], area[3]);

            for (Point ignoredPoint : rawRoom.ignoredPoints()) {
                room.free(ignoredPoint.x, ignoredPoint.y);
            }

            for (Point[] entrance : rawRoom.entrance()) {
                Room.Cell[] entranceCells = new Room.Cell[entrance.length];

                for (int i = 0; i < entrance.length; i++) {
                    entranceCells[i] = new Room.Cell(entrance[i].x, entrance[i].y);
                }

                room.addEntrance(entranceCells);
            }

            for (Point position : rawRoom.playerPositions()) {
                room.addPlayerPosition(position.x, position.y);
            }

            rooms.add(room);
        }
    }

    private void createBoard() {
        Map<Room.Cell, Room> roomByCell = new HashMap<>();
        Map<Room.Cell, Room> roomByEntranceCell = new HashMap<>();
        Map<Room.Cell, Room> roomByPlayerPosition = new HashMap<>();

        for (Room room : rooms) {
            mapRoomCells(room, roomByCell);
            mapEntranceCells(room, roomByEntranceCell);
            mapPlayerPositions(room, roomByPlayerPosition);
        }

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Room.Cell cell = new Room.Cell(column, row);
                Room room = roomByCell.get(cell);
                Room accessibleRoom = roomByEntranceCell.get(cell);
                Room positionRoom = roomByPlayerPosition.get(cell);
                Tile tile = new Tile(null, accessibleRoom);

                if (room != null) {
                    tile.setDisabled(true);
                    tile.setRoom(room);
                }

                if (positionRoom != null) {
                    tile.setRoom(positionRoom);
                    tile.setRoomPosition(true);
                }

                tiles[row][column] = tile;

                if (room == null) {
                    TileAspect tileLook = new TileAspect(tile);

                    if (tile.givesAccessToRoom()) {
                        tileLook.setTileColor(Color.RED);
                    }

                    add(tileLook);
                    visibleTiles.add(new PlacedTile(tileLook, column, row));
                }

                if (positionRoom != null) {
                    TileAspect tileLook = new TileAspect(tile);
                    tileLook.setTileColor(Color.BLUE);
                    roomPositionTiles.add(new PlacedTile(tileLook, column, row));
                }
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

        for (PlacedTile tile : roomPositionTiles) {
            add(tile.look());
            setComponentZOrder(tile.look(), 0);
            visibleTiles.add(tile);
        }
    }

    private void placePieces(RawPiece[] rawPieces) {
        for (RawPiece rawPiece : rawPieces) {
            if (rawPiece == null || rawPiece.piece() == null) {
                throw new IllegalArgumentException("Raw pieces and their pieces cannot be null");
            }

            Tile tile = getTileAt(rawPiece.x(), rawPiece.y());

            if (tile.getPieceOnThisTile() != null) {
                throw new IllegalArgumentException("Two pieces cannot occupy the same tile");
            }

            if (tile.isDisabled() && !tile.isRoomPosition()) {
                throw new IllegalArgumentException("A piece cannot start on a disabled non-room-position tile");
            }

            Piece piece = rawPiece.piece();
            tile.setPieceOnThisTile(piece);
            piece.setPosition(rawPiece.x(), rawPiece.y());
            pieces.add(piece);
        }
    }

    private void mapRoomCells(Room room, Map<Room.Cell, Room> roomByCell) {
        for (Room.Cell cell : room.getOccupiedCells()) {
            validateRoomCell(room, cell);
            Room previousRoom = roomByCell.putIfAbsent(cell, room);

            if (previousRoom != null) {
                throw new IllegalArgumentException(
                        "Rooms '" + previousRoom.getName() + "' and '" + room.getName()
                                + "' overlap at grid cell (" + cell.column() + ", " + cell.row() + ")"
                );
            }
        }
    }

    private void mapEntranceCells(Room room, Map<Room.Cell, Room> roomByEntranceCell) {
        for (List<Room.Cell> entrance : room.getEntrances()) {
            for (Room.Cell cell : entrance) {
                validateRoomCell(room, cell);
                Room previousRoom = roomByEntranceCell.putIfAbsent(cell, room);

                if (previousRoom != null && previousRoom != room) {
                    throw new IllegalArgumentException(
                            "Rooms '" + previousRoom.getName() + "' and '" + room.getName()
                                    + "' use the same entrance tile (" + cell.column() + ", " + cell.row() + ")"
                    );
                }
            }
        }
    }

    private void mapPlayerPositions(Room room, Map<Room.Cell, Room> roomByPlayerPosition) {
        for (Room.Cell cell : room.getPlayerPositions()) {
            validateRoomCell(room, cell);

            if (!room.occupies(cell.column(), cell.row())) {
                throw new IllegalArgumentException(
                        "Player position (" + cell.column() + ", " + cell.row() + ") is not inside room '"
                                + room.getName() + "'"
                );
            }

            Room previousRoom = roomByPlayerPosition.putIfAbsent(cell, room);

            if (previousRoom != null && previousRoom != room) {
                throw new IllegalArgumentException(
                        "Rooms '" + previousRoom.getName() + "' and '" + room.getName()
                                + "' use the same player position (" + cell.column() + ", " + cell.row() + ")"
                );
            }
        }
    }

    private Room.Cell findEmptyRoomPosition(Room room) {
        if (!rooms.contains(room)) {
            throw new IllegalArgumentException("Room does not belong to this board");
        }

        for (Room.Cell cell : room.getPlayerPositions()) {
            if (getTileAt(cell.column(), cell.row()).getPieceOnThisTile() == null) {
                return cell;
            }
        }

        return null;
    }

    private void validateRoomCell(Room room, Room.Cell cell) {
        if (cell.column() < 0 || cell.column() >= columns || cell.row() < 0 || cell.row() >= rows) {
            throw new IllegalArgumentException(
                    "Room '" + room.getName() + "' uses grid cell (" + cell.column() + ", " + cell.row()
                            + ") outside the " + columns + "x" + rows + " board"
            );
        }
    }

    private void validateCoordinates(int x, int y) {
        if (x < 0 || x >= columns || y < 0 || y >= rows) {
            throw new IllegalArgumentException(
                    "Tile coordinates (" + x + ", " + y + ") are outside the " + columns + "x" + rows + " board"
            );
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();

        int availableWidth = getWidth();
        int availableHeight = getHeight();
        int margin = Math.max(24, Math.min(availableWidth, availableHeight) / 28);
        int tileSize = Math.min(
                (availableWidth - margin * 2) / columns,
                (availableHeight - margin * 2) / rows
        );

        if (tileSize <= 0) {
            return;
        }

        int gridWidth = tileSize * columns;
        int gridHeight = tileSize * rows;
        int startX = (availableWidth - gridWidth) / 2;
        int startY = (availableHeight - gridHeight) / 2;

        for (PlacedTile tile : visibleTiles) {
            tile.look().setBounds(
                    startX + tile.column() * tileSize,
                    startY + tile.row() * tileSize,
                    tileSize,
                    tileSize
            );
            tile.look().setGridLineThickness(Math.max(1, tileSize / 24));
        }

        for (RoomsLook roomLook : roomLooks) {
            roomLook.placeOnGrid(startX, startY, tileSize);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        int availableWidth = getWidth();
        int availableHeight = getHeight();
        int margin = Math.max(24, Math.min(availableWidth, availableHeight) / 28);
        int tileSize = Math.min(
                (availableWidth - margin * 2) / columns,
                (availableHeight - margin * 2) / rows
        );

        if (tileSize <= 0) {
            return;
        }

        int gridWidth = tileSize * columns;
        int gridHeight = tileSize * rows;
        int startX = (availableWidth - gridWidth) / 2;
        int startY = (availableHeight - gridHeight) / 2;
        Graphics2D g2 = (Graphics2D) graphics.create();

        try {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(Math.max(2.0f, tileSize / 10.0f)));
            g2.drawRect(startX, startY, gridWidth, gridHeight);
        } finally {
            g2.dispose();
        }
    }
}
