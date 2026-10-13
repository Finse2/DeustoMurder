package model;

public class Tile {

    private Piece pieceOnThisTile;
    private boolean disabled;
    private Room accessibleRoom;
    private Room room;
    private boolean roomPosition;

    public Tile(Piece pieceOnThisTile) {
        this(pieceOnThisTile, null);
    }

    public Tile(Piece pieceOnThisTile, Room accessibleRoom) {
        this.pieceOnThisTile = pieceOnThisTile;
        this.accessibleRoom = accessibleRoom;
    }

    public Piece getPieceOnThisTile() {
        return pieceOnThisTile;
    }

    public void setPieceOnThisTile(Piece pieceOnThisTile) {
        this.pieceOnThisTile = pieceOnThisTile;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Room getAccessibleRoom() {
        return accessibleRoom;
    }

    public void setAccessibleRoom(Room accessibleRoom) {
        this.accessibleRoom = accessibleRoom;
    }

    public boolean givesAccessToRoom() {
        return accessibleRoom != null;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean isRoomPosition() {
        return roomPosition;
    }

    public void setRoomPosition(boolean roomPosition) {
        this.roomPosition = roomPosition;
    }
}
