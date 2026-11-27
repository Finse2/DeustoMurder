package ui.match;

import model.Room;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public final class RoomHoverEffect {
    private RoomHoverEffect() {}

    public static void paint(Graphics graphics, Room room, int startX,
                             int startY, int tileSize) {
        if (room == null || tileSize <= 0) return;
        Area shape = new Area();
        for (Room.Cell cell : room.getOccupiedCells()) {
            shape.add(new Area(new Rectangle2D.Double(
                    startX + cell.column() * tileSize,
                    startY + cell.row() * tileSize, tileSize, tileSize)));
        }
        if (shape.isEmpty()) return;
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(230, 199, 141, 35));
            g.fill(shape);
            g.setColor(new Color(230, 199, 141, 220));
            g.setStroke(new BasicStroke(Math.max(1f, tileSize / 12f)));
            g.draw(shape);
            Rectangle bounds = shape.getBounds();
            g.clip(shape);
            g.setFont(new Font("Georgia", Font.BOLD, Math.max(10, Math.min(16, tileSize / 2))));
            FontMetrics fm = g.getFontMetrics();
            String name = room.getName();
            int width = fm.stringWidth(name) + 12;
            int x = bounds.x + (bounds.width - width) / 2;
            int y = bounds.y + 3;
            g.setColor(new Color(25, 19, 23, 235));
            g.fillRoundRect(x, y, width, fm.getHeight() + 6, 8, 8);
            g.setColor(new Color(230, 199, 141));
            g.drawString(name, x + 6, y + 3 + fm.getAscent());
        } finally {
            g.dispose();
        }
    }
}
