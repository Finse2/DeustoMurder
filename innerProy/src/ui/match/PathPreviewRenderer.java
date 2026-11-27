package ui.match;

import java.awt.*;
import java.util.List;

public final class PathPreviewRenderer {

    private static final Color GOLD =
            new Color(230, 199, 141);

    private PathPreviewRenderer() {
    }

    public static void paint(
            Graphics graphics,
            List<Point> path,
            int startX,
            int startY,
            int tileSize
    ) {

        if (path == null || path.size() < 2 || tileSize <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) graphics.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        float lineWidth = Math.max(1.5f, tileSize / 12f);

        // DRAW PATH

        for (int i = 0; i < path.size() - 1; i++) {

            Point current = path.get(i);
            Point next = path.get(i + 1);

            int x1 = startX + current.x * tileSize
                    + tileSize / 2;

            int y1 = startY + current.y * tileSize
                    + tileSize / 2;

            int x2 = startX + next.x * tileSize
                    + tileSize / 2;

            int y2 = startY + next.y * tileSize
                    + tileSize / 2;

            // LINE SHADOW

            g2.setColor(new Color(0, 0, 0, 120));

            g2.setStroke(new BasicStroke(
                    lineWidth + 3f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            g2.drawLine(x1, y1, x2, y2);

            // GOLDEN DASHED LINE

            g2.setColor(GOLD);

            g2.setStroke(new BasicStroke(
                    lineWidth,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND,
                    10f,
                    new float[]{5f, 4f},
                    0f
            ));

            g2.drawLine(x1, y1, x2, y2);
        }

        // INTERMEDIATE POINTS

        for (int i = 1; i < path.size() - 1; i++) {

            Point point = path.get(i);

            int x = startX + point.x * tileSize
                    + tileSize / 2;

            int y = startY + point.y * tileSize
                    + tileSize / 2;

            int radius = Math.max(2, tileSize / 10);

            g2.setColor(GOLD);

            g2.fillOval(
                    x - radius,
                    y - radius,
                    radius * 2,
                    radius * 2
            );
        }

        // DESTINATION MARKER

        Point destination = path.getLast();

        int centerX = startX + destination.x * tileSize
                + tileSize / 2;

        int centerY = startY + destination.y * tileSize
                + tileSize / 2;

        int radius = Math.max(5, tileSize / 3);

        g2.setColor(new Color(25, 19, 23, 235));

        g2.fillOval(
                centerX - radius,
                centerY - radius,
                radius * 2,
                radius * 2
        );

        g2.setColor(GOLD);

        g2.setStroke(new BasicStroke(1.5f));

        g2.drawOval(
                centerX - radius,
                centerY - radius,
                radius * 2,
                radius * 2
        );

        // MOVEMENT COST

        String cost = String.valueOf(path.size() - 1);

        g2.setFont(new Font(
                "Georgia",
                Font.BOLD,
                Math.max(8, tileSize / 3)
        ));

        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(
                cost,
                centerX - fm.stringWidth(cost) / 2,
                centerY + (fm.getAscent() - fm.getDescent()) / 2
        );

        g2.dispose();
    }
}