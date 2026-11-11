package ui.match;

import model.Piece;
import model.Room;
import model.board.RawPiece;
import model.board.RawRoom;
import model.board.Tile;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BoardLayout extends JPanel {

    public static final int GRID_COLUMNS = 23;
    public static final int GRID_ROWS = 22;
    public static final int FRAME_MARGIN = 30;

    private final int rows;
    private final int columns;
    private int fixedTileSize = 0;
    private final Tile[][] tiles;
    private final List<Room> rooms = new ArrayList<>();
    private final List<ActionListener> entranceListeners = new ArrayList<>();
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
        setBackground(new Color(25, 29, 32));
        setPreferredSize(new Dimension(900, 900));
    }

    public Room checkTileEntrance(int x, int y) {
        return getTileAt(x, y).getAccessibleRoom();
    }

    public boolean checkIsTileDisabled(int x, int y) {
        return getTileAt(x, y).isDisabled();
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

    /** Se notifica cuando una ficha ACABA de entrar en una casilla roja de acceso.
     *  El origen del ActionEvent es el Tile, que ya contiene la ficha y la habitacion.
     *  El evento se emite una sola vez por movimiento realizado mediante movePiece.
     */
    public void addEntranceListener(ActionListener listener) {
        entranceListeners.add(java.util.Objects.requireNonNull(listener));
    }

    public void removeEntranceListener(ActionListener listener) {
        entranceListeners.remove(listener);
    }

    private void notifyEntrance(Tile destination) {
        if (!destination.givesAccessToRoom() || destination.getPieceOnThisTile() == null) return;
        ActionEvent event = new ActionEvent(destination, ActionEvent.ACTION_PERFORMED, "ENTRANCE");
        for (ActionListener listener : List.copyOf(entranceListeners)) {
            listener.actionPerformed(event);
        }
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

        // Al salir de una habitacion hacia su propia puerta, no reentrar
        // automaticamente: de otro modo la ficha quedaria encerrada.
        boolean leavingRoom = origin.isRoomPosition() && origin.getRoom() == destination.getAccessibleRoom();
        origin.setPieceOnThisTile(null);
        destination.setPieceOnThisTile(piece);
        piece.setPosition(xdestination, ydestination);
        repaint();
        if (!leavingRoom) notifyEntrance(destination);
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

    /** Localiza el Tile desde las coordenadas pixel del JPanel, tambien con zoom. */
    public Point pixelToTile(Point pixel) {
        if (pixel == null) return null;
        int size = calculateTileSize();
        if (size <= 0) return null;
        int left = (getWidth() - columns * size) / 2;
        int top = (getHeight() - rows * size) / 2;
        int dx = pixel.x - left;
        int dy = pixel.y - top;
        if (dx < 0 || dy < 0 || dx >= columns * size || dy >= rows * size) return null;
        return new Point(dx / size, dy / size);
    }

    /** Marca casillas alcanzables sin modificar Tile.disabled. */
    public void setReachableTiles(Set<Point> points) {
        Set<Point> safe = points == null ? Set.of() : points;
        for (PlacedTile tile : visibleTiles) {
            tile.look().setReachable(safe.contains(new Point(tile.column(), tile.row())));
        }
    }

    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

    public Piece[][] getPiecesArray() {
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

    /** Allows the board to scale uniformly inside a scroll pane. */
    public void setTileSize(int tileSize) {
        if (tileSize <= 0) throw new IllegalArgumentException("Tile size must be positive");
        if (fixedTileSize == tileSize) return;
        fixedTileSize = tileSize;
        setPreferredSize(new Dimension(columns * tileSize + FRAME_MARGIN * 2,
                rows * tileSize + FRAME_MARGIN * 2));
        revalidate();
        repaint();
    }

    private int calculateTileSize() {
        if (fixedTileSize > 0) return fixedTileSize;
        int width = Math.max(0, getWidth() - FRAME_MARGIN * 2);
        int height = Math.max(0, getHeight() - FRAME_MARGIN * 2);
        return Math.min(width / columns, height / rows);
    }

    @Override
    public void doLayout() {
        super.doLayout();

        int tileSize = calculateTileSize();
        if (tileSize <= 0) return;

        int startX = (getWidth() - columns * tileSize) / 2;
        int startY = (getHeight() - rows * tileSize) / 2;

        for (PlacedTile tile : visibleTiles) {
            tile.look().setBounds(
                    startX + tile.column() * tileSize,
                    startY + tile.row() * tileSize,
                    tileSize,
                    tileSize
            );
            tile.look().setGridLineThickness(Math.max(1, tileSize / 25));
        }

        for (RoomsLook roomLook : roomLooks) {
            roomLook.placeOnGrid(startX, startY, tileSize, tileSize);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        int tileSize = calculateTileSize();
        if (tileSize <= 0) return;

        int boardWidth = columns * tileSize;
        int boardHeight = rows * tileSize;
        int startX = (getWidth() - boardWidth) / 2;
        int startY = (getHeight() - boardHeight) / 2;

        Graphics2D g2 = (Graphics2D) graphics.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.clipRect(0, 0, getWidth(), getHeight());

            paintAntiqueWoodTable(g2, startX, startY, boardWidth, boardHeight, tileSize);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(Math.max(2.0f, tileSize / 10.0f)));
            g2.drawRect(startX, startY, boardWidth, boardHeight);
        } finally {
            g2.dispose();
        }
    }

    private void paintAntiqueWoodTable(Graphics2D g2, int boardX, int boardY, int boardWidth, int boardHeight, int tileSize) {
        int paddingX = FRAME_MARGIN - 5;
        int paddingTop = FRAME_MARGIN - 5;
        int paddingBottom = FRAME_MARGIN - 5;

        int tableX = boardX - paddingX;
        int tableY = boardY - paddingTop;
        int tableWidth = boardWidth + paddingX * 2;
        int tableHeight = boardHeight + paddingTop + paddingBottom;
        int arc = Math.max(28, tileSize + 10);

        RoundRectangle2D tableShape = new RoundRectangle2D.Double(tableX, tableY, tableWidth, tableHeight, arc, arc);
        RoundRectangle2D innerTopShape = new RoundRectangle2D.Double(tableX + 10, tableY + 10, tableWidth - 20, tableHeight - 20, arc - 8, arc - 8);

        g2.setColor(new Color(0, 0, 0, 55));
        g2.fill(new RoundRectangle2D.Double(tableX + 4, tableY + 4, tableWidth, tableHeight, arc, arc));

        Paint oldPaint = g2.getPaint();
        g2.setPaint(new GradientPaint(tableX, tableY, new Color(78, 46, 24), tableX, tableY + tableHeight, new Color(42, 23, 11)));
        g2.fill(tableShape);

        g2.setPaint(new GradientPaint(tableX, tableY, new Color(120, 74, 40), tableX, tableY + tableHeight, new Color(82, 48, 25)));
        g2.fill(innerTopShape);

        Shape previousClip = g2.getClip();
        g2.clip(innerTopShape);
        int plankCount = 8;
        double plankWidth = (double) tableWidth / plankCount;
        for (int i = 0; i < plankCount; i++) {
            int x = (int) Math.round(tableX + i * plankWidth);
            int width = (int) Math.ceil(plankWidth);
            Color plankColor = i % 2 == 0 ? new Color(134, 84, 47, 145) : new Color(105, 63, 35, 135);
            g2.setColor(plankColor);
            g2.fillRect(x, tableY, width, tableHeight);

            g2.setColor(new Color(255, 235, 205, 28));
            g2.drawLine(x + width / 3, tableY + 8, x + width / 3, tableY + tableHeight - 8);
            g2.setColor(new Color(60, 30, 14, 35));
            g2.drawLine(x + (width * 2) / 3, tableY + 10, x + (width * 2) / 3, tableY + tableHeight - 10);
        }
        g2.setClip(previousClip);

        g2.setColor(new Color(255, 235, 205, 30));
        g2.fillRoundRect(tableX + 14, tableY + 14, tableWidth - 28, Math.max(24, tableHeight / 6), arc - 10, arc - 10);

        g2.setColor(new Color(58, 31, 15));
        g2.setStroke(new BasicStroke(Math.min(6f, Math.max(3f, tileSize / 7f))));
        g2.draw(tableShape);

        g2.setColor(new Color(170, 120, 72, 130));
        g2.setStroke(new BasicStroke(Math.max(2f, tileSize / 12f)));
        g2.draw(innerTopShape);

        g2.setPaint(oldPaint);
    }
}
