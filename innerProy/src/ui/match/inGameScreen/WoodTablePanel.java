package ui.match.inGameScreen;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Panel que genera una textura de madera oscura<br>
 * para el fondo de la pantalla de partida.<br>
 */
public final class WoodTablePanel extends JPanel {

    //configuración
    private static final int TILE_WIDTH = 720;
    private static final int TILE_HEIGHT = 420;

    // textura compartida entre todas las instancias del panel.
    private static final BufferedImage TEXTURE = createWoodTexture();
    private static final TexturePaint WOOD_PAINT = new TexturePaint(
            TEXTURE, new Rectangle(0, 0, TILE_WIDTH, TILE_HEIGHT));

    public WoodTablePanel(LayoutManager layout) {
        super(layout);
        setOpaque(true);
        setBackground(new Color(28, 17, 12));
    }

    //renderizado del panel

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics.create();
        try {
            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0) return;

            // dibujar la textura de madera.
            g.setPaint(WOOD_PAINT);
            g.fillRect(0, 0, width, height);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // oscurecimiento progresivo de arriba hacia abajo.
            g.setPaint(new GradientPaint(0, 0, new Color(255, 214, 150, 10),
                    0, height, new Color(0, 0, 0, 120)));
            g.fillRect(0, 0, width, height);

            g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0),
                    width, 0, new Color(28, 8, 5, 36)));
            g.fillRect(0, 0, width, height);

            // bordes oscuros de la mesa.
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRect(0, 0, width, 12);
            g.fillRect(0, height - 12, width, 12);
            g.fillRect(0, 0, 12, height);
            g.fillRect(width - 12, 0, 12, height);

            // varias capas de sombra alrededor del panel.
            int vignetteLayers = 8;
            for (int i = 0; i < vignetteLayers; i++) {
                int alpha = 18 - i * 2;
                g.setColor(new Color(0, 0, 0, Math.max(0, alpha)));
                g.setStroke(new BasicStroke(20f));
                g.drawRoundRect(-20 + i * 6, -20 + i * 6, width + 40 - i * 12, height + 40 - i * 12, 50, 50);
            }

            g.setColor(new Color(15, 8, 6, 155));
            g.setStroke(new BasicStroke(4f));
            g.drawRect(2, 2, Math.max(0, width - 5), Math.max(0, height - 5));
        } finally {
            g.dispose();
        }
    }

    // generación de textura

    private static BufferedImage createWoodTexture() {
        BufferedImage image = new BufferedImage(
                TILE_WIDTH, TILE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        Random random = new Random(20261110L);

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. tablones horizontales con diferentes tonos
            int plankHeight = 84;
            int plankCount = (int) Math.ceil(TILE_HEIGHT / (double) plankHeight);
            for (int plank = 0; plank < plankCount; plank++) {
                int y = plank * plankHeight;
                Color top = plank % 2 == 0
                        ? new Color(67, 40, 26) : new Color(56, 32, 22);
                Color bottom = plank % 2 == 0
                        ? new Color(34, 20, 14) : new Color(29, 17, 12);

                g.setPaint(new GradientPaint(0, y, top, 0, y + plankHeight, bottom));
                g.fillRect(0, y, TILE_WIDTH, plankHeight);

                g.setColor(new Color(17, 9, 6, 210));
                g.fillRect(0, y + plankHeight - 4, TILE_WIDTH, 4);
                g.setColor(new Color(121, 77, 48, 42));
                g.drawLine(0, y + 2, TILE_WIDTH, y + 2);

                // elementos oscuros puestos de manera aleatoria para resaltar contrastes
                for (int j = 0; j < 6; j++) {
                    int stainX = random.nextInt(TILE_WIDTH);
                    int stainY = y + 10 + random.nextInt(Math.max(1, plankHeight - 20));
                    int stainW = 40 + random.nextInt(140);
                    int stainH = 8 + random.nextInt(24);
                    g.setColor(new Color(18, 10, 8, 18 + random.nextInt(20)));
                    g.fillOval(stainX, stainY, stainW, stainH);
                }
            }

            // más elementos  oscuros puestos de manera aleatoria para resaltar contrastes
            for (int i = 0; i < 9200; i++) {
                int x = random.nextInt(TILE_WIDTH);
                int y = random.nextInt(TILE_HEIGHT);
                int length = 8 + random.nextInt(165);
                int alpha = 10 + random.nextInt(34);

                if (random.nextInt(5) == 0) {
                    g.setColor(new Color(102, 67, 44, alpha));
                } else {
                    g.setColor(new Color(18, 10, 7, alpha + 18));
                }

                g.setStroke(new BasicStroke(random.nextInt(14) == 0 ? 2f : 1f));
                g.drawLine(x, y, Math.min(TILE_WIDTH, x + length),
                        y + random.nextInt(5) - 2);
            }

            // rallas más oscuras puestas de manera aleatoria para resaltar contrastes
            for (int i = 0; i < 12; i++) {
                int x = 40 + random.nextInt(TILE_WIDTH - 80);
                int y = 20 + random.nextInt(TILE_HEIGHT - 40);
                int length = 30 + random.nextInt(120);

                g.setColor(new Color(12, 7, 5, 95));
                g.setStroke(new BasicStroke(1.1f));
                g.drawLine(x, y, Math.min(TILE_WIDTH - 8, x + length),
                        y + random.nextInt(7) - 3);

                g.setColor(new Color(110, 79, 54, 35));
                g.drawLine(x, y + 1, Math.min(TILE_WIDTH - 8, x + length),
                        y + 1 + random.nextInt(5) - 2);
            }

            // 4. distribución de óbalos por toda la superficie
            for (int i = 0; i < 5; i++) {
                int x = 90 + random.nextInt(TILE_WIDTH - 180);
                int y = 40 + random.nextInt(TILE_HEIGHT - 80);

                g.setColor(new Color(26, 14, 9, 120));
                g.setStroke(new BasicStroke(2f));
                g.drawOval(x - 34, y - 11, 68, 22);
                g.drawOval(x - 22, y - 7, 44, 14);

                g.setColor(new Color(133, 88, 55, 42));
                g.drawArc(x - 40, y - 16, 80, 32, 200, 140);
            }

            // 5.spam de zonas más oscuras (agujeros) para generar "imperfecciones"

            for (int i = 0; i < 20; i++) {
                int x = random.nextInt(TILE_WIDTH);
                int y = random.nextInt(TILE_HEIGHT);
                int radius = 2 + random.nextInt(4);

                g.setColor(new Color(10, 7, 5, 70));
                g.fillOval(x, y, radius * 2, radius * 2);
            }
        } finally {
            g.dispose();
        }

        return image;
    }
}