package model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A room card that also stores the cells
 * occupied by that room on the board.
 */
public class Room extends Card {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final Set<Cell> occupiedCells =
            new LinkedHashSet<>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<Cell> getOccupiedCells() {
        return Collections.unmodifiableSet(
                occupiedCells
        );
    }

    public boolean occupies(
            int column,
            int row
    ) {

        return occupiedCells.contains(
                new Cell(
                        column,
                        row
                )
        );
    }

    public Room occupy(
            int column,
            int row
    ) {

        if (column < 0 || row < 0) {

            throw new IllegalArgumentException(
                    "Room coordinates cannot be negative"
            );
        }

        occupiedCells.add(
                new Cell(
                        column,
                        row
                )
        );

        return this;
    }

    public Room occupyRectangle(
            int column,
            int row,
            int width,
            int height
    ) {

        if (width <= 0 || height <= 0) {

            throw new IllegalArgumentException(
                    "Room width and height must be positive"
            );
        }

        for (
                int y = row;
                y < row + height;
                y++
        ) {

            for (
                    int x = column;
                    x < column + width;
                    x++
            ) {

                occupy(
                        x,
                        y
                );
            }
        }

        return this;
    }

    public Room free(
            int column,
            int row
    ) {

        occupiedCells.remove(
                new Cell(
                        column,
                        row
                )
        );

        return this;
    }

    public record Cell(
            int column,
            int row
    ) implements Serializable {

        private static final long serialVersionUID = 1L;
    }
}