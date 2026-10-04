package model;
import java.awt.Color;
import java.util.List;

public class Figure {

    private int idFigura;

    // Current posision
    private int x;
    private int y;

    // Initial position

    private int startX;
    private int startY;
    private Color color;

    public Figure(int idFigura, int startX, int startY, Color color) {
        this.idFigura = idFigura;
        this.startX = startX;
        this.startY = startY;
        this.color = color;
    }

    // GETTERS

    public int getIdFigura() {
        return idFigura;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public Color getColor() {
        return color;
    }

    // SETTERS

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    // MOVEMENT

    public void moveTo(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void resetPosition() {
        this.x = startX;
        this.y = startY;
    }

    // POSITION CHECK

    public boolean isAt(int x, int y) {
        return this.x == x && this.y == y;
    }

    public boolean isPositionOccupied(
            int x,
            int y,
            List<Figure> figures
    ) {
        for (Figure figure : figures) {
            if (figure == this) {
                continue;
            }
            if (figure.getX() == x && figure.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public boolean canMoveTo(int x, int y, List<Figure> figures) {
        for (Figure figure : figures) {
            if (figure == this) {
                continue;
            }

            if (figure.getX() == x && figure.getY() == y) {
                return false;
            }

        }

        return true;
    }
}






