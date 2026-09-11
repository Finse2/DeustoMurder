package activation;

import model.Tile;
import ui.boardPiece.ScenarioAspect;
import ui.boardPiece.TileAspect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class Window extends JFrame {


    private static final int GRID_COLUMNS = 13;
    private static final int GRID_ROWS = 13;

    /*
     *      ROOM       ROOM        ROOM
     *
     * ROOM                     ROOM
     *
     *             CENTER
     *
     * ROOM                     ROOM
     *
     *      ROOM       ROOM        ROOM
     */
    private static final int[][] SPECIAL_BLOCK_POSITIONS = {

            // upper area
            {1, 0},
            {5, 0},
            {10, 1},

            // sides
            {0, 3},
            {11, 4},

            {0, 7},
            {11, 8},

            // lower area
            {2, 11},
            {7, 11}
    };

    private static final int CENTER_COLUMN = 5;
    private static final int CENTER_ROW = 5;
    private static final int CENTER_WIDTH = 3;
    private static final int CENTER_HEIGHT = 3;

    private final JPanel board;

    private final List<PlacedComponent> components =
            new ArrayList<>();

    private record PlacedComponent(
            JComponent component,
            int column,
            int row,
            int widthInTiles,
            int heightInTiles,
            ComponentType type
    ) {
    }

    private enum ComponentType {
        CORRIDOR,
        ROOM,
        CENTER
    }

    public Window() {

        setTitle("Cluedo");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        board = new JPanel();

        board.setLayout(null);

        board.setBackground(
                new Color(30, 35, 40)
        );

        createBoard();

        add(
                board,
                BorderLayout.CENTER
        );

        Dimension screenSize =
                Toolkit
                        .getDefaultToolkit()
                        .getScreenSize();

        setSize(
                (int) (screenSize.width * 0.80),
                (int) (screenSize.height * 0.85)
        );

        setMinimumSize(
                new Dimension(
                        500,
                        500
                )
        );

        setLocationRelativeTo(null);

        board.addComponentListener(
                new ComponentAdapter() {

                    @Override
                    public void componentResized(
                            ComponentEvent e
                    ) {

                        resizeBoard();
                    }
                }
        );

        setVisible(true);
    }

    private void createBoard() {

        boolean[][] occupied =
                new boolean[GRID_ROWS][GRID_COLUMNS];

        for (int[] position :
                SPECIAL_BLOCK_POSITIONS) {

            int column = position[0];
            int row = position[1];

            markOccupied(
                    occupied,
                    column,
                    row,
                    2,
                    2
            );
        }


        markOccupied(
                occupied,
                CENTER_COLUMN,
                CENTER_ROW,
                CENTER_WIDTH,
                CENTER_HEIGHT
        );

        /*
         * Every remaining square is corridor.
         */
        for (int row = 0;
             row < GRID_ROWS;
             row++) {

            for (int column = 0;
                 column < GRID_COLUMNS;
                 column++) {

                if (!occupied[row][column]) {

                    TileAspect corridor =
                            new TileAspect(
                                    new Tile(null)
                            );

                    board.add(corridor);

                    components.add(
                            new PlacedComponent(
                                    corridor,
                                    column,
                                    row,
                                    1,
                                    1,
                                    ComponentType.CORRIDOR
                            )
                    );
                }
            }
        }

        for (int[] position :
                SPECIAL_BLOCK_POSITIONS) {

            int column = position[0];
            int row = position[1];

            ScenarioAspect room =
                    new ScenarioAspect(
                            0,
                            0
                    );

            board.add(room);

            components.add(
                    new PlacedComponent(
                            room,
                            column,
                            row,
                            2,
                            2,
                            ComponentType.ROOM
                    )
            );
        }

        JPanel center =
                new JPanel();

        center.setBackground(
                new Color(70, 70, 70)
        );

        board.add(center);

        components.add(
                new PlacedComponent(
                        center,
                        CENTER_COLUMN,
                        CENTER_ROW,
                        CENTER_WIDTH,
                        CENTER_HEIGHT,
                        ComponentType.CENTER
                )
        );
    }

    private void markOccupied(
            boolean[][] occupied,
            int column,
            int row,
            int width,
            int height
    ) {

        for (int y = row;
             y < row + height;
             y++) {

            for (int x = column;
                 x < column + width;
                 x++) {

                if (
                        y >= 0
                                && y < GRID_ROWS
                                && x >= 0
                                && x < GRID_COLUMNS
                ) {

                    occupied[y][x] = true;
                }
            }
        }
    }

    private void resizeBoard() {

        int availableWidth =
                board.getWidth();

        int availableHeight =
                board.getHeight();

        int margin =
                Math.max(
                        15,
                        Math.min(
                                availableWidth,
                                availableHeight
                        ) / 30
                );

        int usableWidth =
                availableWidth
                        - margin * 2;

        int usableHeight =
                availableHeight
                        - margin * 2;

        int tileSize =
                Math.min(
                        usableWidth / GRID_COLUMNS,
                        usableHeight / GRID_ROWS
                );

        if (tileSize <= 0) {
            return;
        }

        int totalBoardWidth =
                tileSize * GRID_COLUMNS;

        int totalBoardHeight =
                tileSize * GRID_ROWS;

        /*
         * Center the complete square board.
         */
        int startX =
                (availableWidth
                        - totalBoardWidth) / 2;

        int startY =
                (availableHeight
                        - totalBoardHeight) / 2;

        for (PlacedComponent placed :
                components) {

            int x =
                    startX
                            + placed.column()
                            * tileSize;

            int y =
                    startY
                            + placed.row()
                            * tileSize;

            int width =
                    placed.widthInTiles()
                            * tileSize;

            int height =
                    placed.heightInTiles()
                            * tileSize;

            placed.component().setBounds(
                    x,
                    y,
                    width,
                    height
            );

            switch (placed.type()) {

                case CORRIDOR -> {

                    int border =
                            Math.max(
                                    1,
                                    tileSize / 16
                            );

                    placed.component().setBorder(
                            BorderFactory.createLineBorder(
                                    new Color(
                                            150,
                                            150,
                                            150
                                    ),
                                    border
                            )
                    );
                }

                case ROOM -> {

                    int border =
                            Math.max(
                                    2,
                                    tileSize / 12
                            );

                    placed.component().setBorder(
                            BorderFactory.createLineBorder(
                                    Color.BLACK,
                                    border
                            )
                    );
                }

                case CENTER -> {

                    int border =
                            Math.max(
                                    2,
                                    tileSize / 10
                            );

                    placed.component().setBorder(
                            BorderFactory.createLineBorder(
                                    new Color(
                                            20,
                                            20,
                                            20
                                    ),
                                    border
                            )
                    );
                }
            }
        }

        board.revalidate();
        board.repaint();
    }
}