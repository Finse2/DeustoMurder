package model;

import java.awt.Point;
import java.util.List;

public record RawRoom(
        String name,
        int[] area,
        List<Point> ignoredPoints,
        List<Point[]> entrance,
        List<Point> playerPositions) {
}
