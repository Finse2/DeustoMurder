package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A room card that also stores the cells
 * occupied by that room on the board.
 */
public class Room extends Card {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Set<Cell> occupiedCells = new LinkedHashSet<>();
    private final List<List<Cell>> entrances = new ArrayList<>();
    private final Set<Cell> playerPositions = new LinkedHashSet<>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<Cell> getOccupiedCells() {
        return Collections.unmodifiableSet(occupiedCells);
    }

    public boolean occupies(int column, int row) {
        return occupiedCells.contains(new Cell(column, row));
    }

    public List<List<Cell>> getEntrances() {
        return Collections.unmodifiableList(entrances);
    }

    public Set<Cell> getPlayerPositions() {
        return Collections.unmodifiableSet(playerPositions);
    }

    public boolean isEntrance(int column, int row) {
        Cell target = new Cell(column, row);

        for (List<Cell> entrance : entrances) {
            if (entrance.contains(target)) {
                return true;
            }
        }

        return false;
    }

    public boolean isPlayerPosition(int column, int row) {
        return playerPositions.contains(new Cell(column, row));
    }

    public Room addEntrance(Cell... cells) {
        if (cells == null || cells.length == 0) {
            throw new IllegalArgumentException("An entrance must contain at least one cell");
        }

        List<Cell> entrance = new ArrayList<>();

        for (Cell cell : cells) {
            if (cell == null) {
                throw new IllegalArgumentException("Entrance cells cannot be null");
            }

            if (cell.column() < 0 || cell.row() < 0) {
                throw new IllegalArgumentException("Entrance coordinates cannot be negative");
            }

            entrance.add(cell);
        }

        entrances.add(List.copyOf(entrance));
        return this;
    }

    public Room addPlayerPosition(int column, int row) {
        Cell cell = new Cell(column, row);

        if (!occupiedCells.contains(cell)) {
            throw new IllegalArgumentException(
                    "Player position (" + column + ", " + row + ") is not inside room '" + name + "'"
            );
        }

        playerPositions.add(cell);
        return this;
    }

    public Room occupy(int column, int row) {
        if (column < 0 || row < 0) {
            throw new IllegalArgumentException("Room coordinates cannot be negative");
        }

        occupiedCells.add(new Cell(column, row));
        return this;
    }

    public Room occupyRectangle(int column, int row, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Room width and height must be positive");
        }

        for (int y = row; y < row + height; y++) {
            for (int x = column; x < column + width; x++) {
                occupy(x, y);
            }
        }

        return this;
    }

    public Room free(int column, int row) {
        Cell cell = new Cell(column, row);
        occupiedCells.remove(cell);
        playerPositions.remove(cell);
        return this;
    }

    public record Cell(int column, int row) implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
