package ui.match;

import ui.NoirTheme;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public final class PlayerTokenRenderer {

    private PlayerTokenRenderer() {
    }

    public static void paint(
            Graphics graphics,
            int x,
            int y,
            int size,
            Color playerColor,
            Image portrait,
            boolean active
    ) {

        if (size < 6) return;

        Graphics2D g2 = (Graphics2D) graphics.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        float borderWidth = Math.max(1f, size * 0.045f);

        // SHADOW

        g2.setColor(new Color(0, 0, 0, 110));

        g2.fillOval(
                x,
                y + 2,
                size,
                size
        );

        // ACTIVE PLAYER GLOW

        if (active) {

            g2.setColor(new Color(183, 154, 116, 75));

            g2.fillOval(
                    x - 2,
                    y - 2,
                    size + 4,
                    size + 4
            );
        }

        // OUTER METALLIC BORDER

        GradientPaint metal = new GradientPaint(
                x,
                y,
                new Color(236, 210, 165),
                x + size,
                y + size,
                new Color(91, 62, 35)
        );

        g2.setPaint(metal);

        g2.fillOval(x, y, size, size);

        // INNER BACKGROUND

        int inset = Math.max(2, size / 10);

        int innerX = x + inset;
        int innerY = y + inset;

        int innerSize = size - inset * 2;

        g2.setColor(NoirTheme.BACKGROUND);

        g2.fillOval(
                innerX,
                innerY,
                innerSize,
                innerSize
        );

        // PLAYER PORTRAIT

        if (portrait != null
                && portrait.getWidth(null) > 0
                && portrait.getHeight(null) > 0) {

            Shape previousClip = g2.getClip();

            g2.clip(new Ellipse2D.Float(
                    innerX,
                    innerY,
                    innerSize,
                    innerSize
            ));

            // PRESERVE IMAGE PROPORTIONS

            int imageWidth = portrait.getWidth(null);
            int imageHeight = portrait.getHeight(null);

            double scale = Math.max(
                    (double) innerSize / imageWidth,
                    (double) innerSize / imageHeight
            );

            int scaledWidth = (int) Math.ceil(
                    imageWidth * scale
            );

            int scaledHeight = (int) Math.ceil(
                    imageHeight * scale
            );

            int imageX = innerX
                    + (innerSize - scaledWidth) / 2;

            int imageY = innerY
                    + (innerSize - scaledHeight) / 2;

            g2.drawImage(
                    portrait,
                    imageX,
                    imageY,
                    scaledWidth,
                    scaledHeight,
                    null
            );

            g2.setClip(previousClip);

        } else {

            // FALLBACK IF NO PORTRAIT IS AVAILABLE

            g2.setColor(playerColor);

            g2.fillOval(
                    innerX,
                    innerY,
                    innerSize,
                    innerSize
            );
        }

        // PLAYER COLOR BORDER

        g2.setColor(playerColor);

        g2.setStroke(
                new BasicStroke(borderWidth)
        );

        g2.drawOval(
                innerX,
                innerY,
                innerSize,
                innerSize
        );

        // ACTIVE PLAYER BORDER

        if (active) {

            g2.setColor(NoirTheme.GOLD);

            g2.setStroke(
                    new BasicStroke(borderWidth + 0.5f)
            );

            g2.drawOval(
                    x,
                    y,
                    size,
                    size
            );
        }

        g2.dispose();
    }
}