
package ui.match;

import model.Room;
import utils.AllPaths;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Dibuja una habitación del tablero con su imagen y conserva su forma y sus bordes.
 */
class RoomsLook extends JComponent {

    private final Room room;
    private final BufferedImage image;
    private final int minColumn;
    private final int minRow;
    private final int maxColumn;
    private final int maxRow;
    private int tileSize = 1;
    private Area localRoomShape = new Area();

    RoomsLook(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }

        if (room.getOccupiedCells().isEmpty()) {
            throw new IllegalArgumentException("Room '" + room.getName() + "' does not occupy any grid cells");
        }

        this.room = room;
        this.image = loadImage(getImagePath(room.getName()));

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Room.Cell cell : room.getOccupiedCells()) {
            minX = Math.min(minX, cell.column());
            minY = Math.min(minY, cell.row());
            maxX = Math.max(maxX, cell.column());
            maxY = Math.max(maxY, cell.row());
        }

        minColumn = minX;
        minRow = minY;
        maxColumn = maxX;
        maxRow = maxY;
        setOpaque(false);
    }

    /** Relaciona el nombre de una habitación con la ruta declarada en AllPaths. */
    private static String getImagePath(String roomName) {
        String normalized = Normalizer.normalize(roomName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "CRY", "CRAI" -> AllPaths.Resources.BoardRooms.CRAI;
            case "BANOS" -> AllPaths.Resources.BoardRooms.BANOS;
            case "CAFETERIA", "CAFTERIA" -> AllPaths.Resources.BoardRooms.CAFETERIA;
            case "MAQUINAS", "SALADEMAQUINAS" -> AllPaths.Resources.BoardRooms.MAQUINAS;
            case "LABORATORIO", "LABORATORIOS" -> AllPaths.Resources.BoardRooms.LABORATORIO;
            case "DEUSTOTECH" -> AllPaths.Resources.BoardRooms.DEUSTOTECH;
            case "AULA1", "AULAE208" -> AllPaths.Resources.BoardRooms.AULA_1;
            case "AULA2" -> AllPaths.Resources.BoardRooms.AULA_2;
            case "ROOM9" -> AllPaths.Resources.BoardRooms.ROOM_9;
            case "ROOM10" -> AllPaths.Resources.BoardRooms.ROOM_10;
            default -> null;
        };
    }

    /** Carga una imagen desde el classpath o desde la carpeta del proyecto. */
    private static BufferedImage loadImage(String relativePath) {
        if (relativePath == null) return null;

        String filename = Path.of(relativePath).getFileName().toString();
        for (String resource : new String[]{"/RomsLook/" + filename, "/" + relativePath}) {
            try (InputStream input = RoomsLook.class.getResourceAsStream(resource)) {
                if (input != null) return readImage(input, resource);
            } catch (IOException e) {
                throw new IllegalStateException("Error al cargar " + resource, e);
            }
        }

        Path current = Path.of("").toAbsolutePath().normalize();
        for (Path directory = current; directory != null; directory = directory.getParent()) {
            for (Path file : new Path[]{
                    directory.resolve(relativePath),
                    directory.resolve("innerProy").resolve(relativePath)}) {
                if (!Files.isRegularFile(file)) continue;
                try (InputStream input = Files.newInputStream(file)) {
                    return readImage(input, file.toString());
                } catch (IOException e) {
                    throw new IllegalStateException("Error al cargar " + file, e);
                }
            }
        }

        throw new IllegalStateException("Imagen del tablero no encontrada: " + relativePath);
    }

    private static BufferedImage readImage(InputStream input, String source) throws IOException {
        BufferedImage result = ImageIO.read(input);
        if (result == null) throw new IOException("Imagen no válida: " + source);
        return result;
    }

    void placeOnGrid(int gridStartX, int gridStartY, int tileSize) {
        this.tileSize = tileSize;

        int widthInTiles = maxColumn - minColumn + 1;
        int heightInTiles = maxRow - minRow + 1;

        setBounds(
                gridStartX + minColumn * tileSize,
                gridStartY + minRow * tileSize,
                widthInTiles * tileSize,
                heightInTiles * tileSize
        );

        rebuildShape();
    }

    private void rebuildShape() {
        Area shape = new Area();

        for (Room.Cell cell : room.getOccupiedCells()) {
            int localColumn = cell.column() - minColumn;
            int localRow = cell.row() - minRow;

            shape.add(new Area(new Rectangle2D.Double(
                    localColumn * tileSize,
                    localRow * tileSize,
                    tileSize,
                    tileSize
            )));
        }

        localRoomShape = shape;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            Shape previousClip = g2.getClip();
            g2.clip(localRoomShape);

            if (image == null) {
                g2.setColor(Color.WHITE);
                g2.fill(localRoomShape);
            } else {
                drawCover(g2, image);
            }

            g2.setClip(previousClip);
            float wallThickness = Math.max(2.0f, tileSize / 10.0f);
            g2.setStroke(new BasicStroke(wallThickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            g2.setColor(Color.BLACK);
            g2.draw(localRoomShape);
        } finally {
            g2.dispose();
        }
    }

    /** Ajusta la imagen a la habitación sin deformarla, recortando los bordes sobrantes. */
    private void drawCover(Graphics2D g2, BufferedImage source) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        double sourceRatio = (double) source.getWidth() / source.getHeight();
        double targetRatio = (double) width / height;
        int sx = 0;
        int sy = 0;
        int sw = source.getWidth();
        int sh = source.getHeight();

        if (sourceRatio > targetRatio) {
            sw = Math.max(1, (int) Math.round(sh * targetRatio));
            sx = (source.getWidth() - sw) / 2;
        } else {
            sh = Math.max(1, (int) Math.round(sw / targetRatio));
            sy = (source.getHeight() - sh) / 2;
        }

        g2.drawImage(source, 0, 0, width, height, sx, sy, sx + sw, sy + sh, null);
    }

    @Override
    public boolean contains(int x, int y) {
        return localRoomShape.contains(x, y);
    }
}
