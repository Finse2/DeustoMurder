
package ui.match;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public final class MarbleTilePanel extends JPanel {

    private static final BufferedImage[] TEXTURES = loadTextures();
    private static final Random RANDOM = new Random();

    private final TileAspect tileAspect;
    private final int style;
    private BufferedImage texture;

    public MarbleTilePanel(TileAspect tileAspect) {
        this(tileAspect, RANDOM.nextInt(6) + 1);
    }

    public MarbleTilePanel(TileAspect tileAspect, int style) {
        if (tileAspect == null) {
            throw new IllegalArgumentException("TileAspect cannot be null");
        }

        if (style < 1 || style > 6) {
            throw new IllegalArgumentException("Style must be between 1 and 6");
        }

        this.tileAspect = tileAspect;
        this.style = style;

        setLayout(new BorderLayout());
        setPreferredSize(
                new Dimension(TileAspect.SIZE, TileAspect.SIZE)
        );

        tileAspect.setOpaque(false);
        add(tileAspect, BorderLayout.CENTER);
    }

    private static BufferedImage[] loadTextures() {
        BufferedImage[] result = new BufferedImage[6];

        for (int i = 0; i < result.length; i++) {
            String path = "/tiles/marble/marble_" + (i + 1) + ".png";

            try (var input =
                         MarbleTilePanel.class.getResourceAsStream(path)) {

                if (input == null) {
                    throw new IllegalStateException(
                            "Missing marble texture: " + path
                    );
                }

                result[i] = ImageIO.read(input);

                if (result[i] == null) {
                    throw new IOException("Invalid image: " + path);
                }

            } catch (IOException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        return result;
    }

    @Override
    public void doLayout() {
        switch (style) {
            case 1 -> texture = TEXTURES[0];
            case 2 -> texture = TEXTURES[1];
            case 3 -> texture = TEXTURES[2];
            case 4 -> texture = TEXTURES[3];
            case 5 -> texture = TEXTURES[4];
            case 6 -> texture = TEXTURES[5];
            default -> throw new IllegalStateException(
                    "Unknown marble style"
            );
        }

        super.doLayout();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (texture != null) {
            g.drawImage(
                    texture,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this
            );
        }
    }

    public TileAspect getTileAspect() {
        return tileAspect;
    }

    public int getStyle() {
        return style;
    }
}
