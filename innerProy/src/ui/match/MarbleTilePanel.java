package ui.match;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.io.File;

public final class MarbleTilePanel extends JPanel {

    private static final BufferedImage[] TEXTURES = loadTextures();
    private static final Random RANDOM = new Random();

    private final TileAspect tileAspect;
    private final int style;
    private final BufferedImage texture;

    public MarbleTilePanel(TileAspect tileAspect) {
        this(tileAspect, RANDOM.nextInt(6) + 1);
    }

    public MarbleTilePanel(TileAspect tileAspect, int style) {

        if (tileAspect == null) {
            throw new IllegalArgumentException(
                    "TileAspect cannot be null"
            );
        }

        if (style < 1 || style > TEXTURES.length) {
            throw new IllegalArgumentException(
                    "Invalid marble style: " + style
            );
        }

        this.tileAspect = tileAspect;
        this.style = style;
        this.texture = TEXTURES[style - 1];

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

            String path = "innerProy/ressources/hallwayTileAspects/marble_"
                    + (i + 1) + ".png";

            File file = new File(path);

            System.out.println("Loading: " + file.getAbsolutePath());

            try {

                if (!file.exists()) {
                    throw new IOException(
                            "Missing texture: " + file.getAbsolutePath()
                    );
                }

                result[i] = ImageIO.read(file);

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
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(
                texture,
                0,
                0,
                getWidth(),
                getHeight(),
                this
        );
    }

    public TileAspect getTileAspect() {
        return tileAspect;
    }

    public int getStyle() {
        return style;
    }
}