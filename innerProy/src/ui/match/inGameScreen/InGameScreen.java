package ui.match.inGameScreen;

import model.Actor;
import ui.match.BoardLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

/**
 * Contenedor principal de la partida. El tablero se muestra dentro de un
 * JScrollPane y permite zoom y desplazamiento horizontal y vertical.
 */
public final class InGameScreen extends JFrame {

    private final BoardLayout board;
    private final List<Actor> actors;
    private JScrollPane boardScrollPane;
    private JPanel boardCanvas;
    private double zoom = 1.0;
    private int baseTileSize = 25;

    public InGameScreen(BoardLayout board, List<Actor> actors, JComponent hand) {
        this.board = board;
        this.actors = actors;

        setTitle("DeustoMurder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(Color.BLACK);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Color headerColor = new Color(10, 35, 55);

        JPanel playersColumn = new JPanel(new BorderLayout(0, 10));
        playersColumn.setBackground(Color.BLACK);
        playersColumn.setPreferredSize(new Dimension(240, 0));

        JPanel playersHeader = new JPanel();
        playersHeader.setBackground(headerColor);
        playersHeader.setPreferredSize(new Dimension(0, 125));
        playersColumn.add(playersHeader, BorderLayout.NORTH);
        playersColumn.add(createPlayersPanel(), BorderLayout.CENTER);

        JPanel rightColumn = new JPanel(new BorderLayout(0, 10));
        rightColumn.setBackground(Color.BLACK);
        rightColumn.setPreferredSize(new Dimension(345, 0));

        JPanel rightHeader = new JPanel();
        rightHeader.setBackground(headerColor);
        rightHeader.setPreferredSize(new Dimension(0, 125));
        rightColumn.add(rightHeader, BorderLayout.NORTH);

        JPanel rightContent = new JPanel(new GridLayout(2, 1, 0, 10));
        rightContent.setBackground(Color.BLACK);
        rightContent.add(new GameHistoryPanel());
        rightContent.add(new ChatPanel());
        rightColumn.add(rightContent, BorderLayout.CENTER);

        JPanel middle = new JPanel(new BorderLayout(10, 0));
        middle.setBackground(Color.BLACK);
        middle.add(playersColumn, BorderLayout.WEST);
        middle.add(createBoardScrollPane(), BorderLayout.CENTER);
        middle.add(rightColumn, BorderLayout.EAST);

        JPanel handPanel = new JPanel(new BorderLayout());
        handPanel.setBackground(new Color(25, 55, 75));
        handPanel.add(hand, BorderLayout.CENTER);
        handPanel.setPreferredSize(new Dimension(0, 230));

        GameActionsPanel actions = new GameActionsPanel();
        actions.setPreferredSize(new Dimension(345, 0));

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(Color.BLACK);
        bottom.add(handPanel, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.EAST);

        root.add(middle, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);

        SwingUtilities.invokeLater(this::fitBoardToViewport);
    }

    private JScrollPane createBoardScrollPane() {
        // La mesa y el tablero forman un único componente dentro del viewport.
        boardCanvas = new JPanel(new GridBagLayout());
        boardCanvas.setBackground(new Color(17, 27, 32));
        boardCanvas.add(board);

        boardScrollPane = new JScrollPane(
                boardCanvas,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        boardScrollPane.setBorder(null);
        boardScrollPane.getViewport().setBackground(boardCanvas.getBackground());
        boardScrollPane.setWheelScrollingEnabled(false);
        boardScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        boardScrollPane.getHorizontalScrollBar().setUnitIncrement(30);

        // Rueda: zoom; Mayús+rueda: scroll horizontal; Alt+rueda: scroll vertical.
        MouseAdapter wheelAdapter = new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent event) {
                if (event.isShiftDown()) {
                    scrollWithWheel(boardScrollPane.getHorizontalScrollBar(), event);
                } else if (event.isAltDown()) {
                    scrollWithWheel(boardScrollPane.getVerticalScrollBar(), event);
                } else {
                    zoomAt(event);
                }
            }
        };

        // Botón central / derecho, o Mayús + botón izquierdo: arrastrar en ambos ejes.
        MouseAdapter panAdapter = new MouseAdapter() {
            private Point lastScreenPoint;

            @Override
            public void mousePressed(MouseEvent event) {
                boolean middleOrRight = SwingUtilities.isMiddleMouseButton(event)
                        || SwingUtilities.isRightMouseButton(event);
                boolean shiftLeft = SwingUtilities.isLeftMouseButton(event) && event.isShiftDown();
                if (!middleOrRight && !shiftLeft) return;

                lastScreenPoint = event.getLocationOnScreen();
                boardCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                event.consume();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (lastScreenPoint == null) return;

                boolean held = (event.getModifiersEx() & (InputEvent.BUTTON1_DOWN_MASK
                        | InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK)) != 0;
                if (!held) return;

                Point point = event.getLocationOnScreen();
                int dx = point.x - lastScreenPoint.x;
                int dy = point.y - lastScreenPoint.y;
                JViewport viewport = boardScrollPane.getViewport();
                Point position = viewport.getViewPosition();
                moveViewport(position.x - dx, position.y - dy);
                lastScreenPoint = point;
                event.consume();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (lastScreenPoint == null) return;
                lastScreenPoint = null;
                boardCanvas.setCursor(Cursor.getDefaultCursor());
                event.consume();
            }
        };

        // Registrar también en las casillas y habitaciones, que son componentes hijos.
        installBoardMouseControls(boardCanvas, wheelAdapter, panAdapter);
        boardScrollPane.addMouseWheelListener(wheelAdapter);
        boardScrollPane.getViewport().addMouseWheelListener(wheelAdapter);

        boardScrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                fitBoardToViewport();
            }
        });

        return boardScrollPane;
    }

    private void installBoardMouseControls(Component component, MouseAdapter wheel, MouseAdapter pan) {
        component.addMouseWheelListener(wheel);
        component.addMouseListener(pan);
        component.addMouseMotionListener(pan);
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                installBoardMouseControls(child, wheel, pan);
            }
        }
    }

    private void fitBoardToViewport() {
        if (boardScrollPane.getWidth() <= 0 || boardScrollPane.getHeight() <= 0) return;

        int width = boardScrollPane.getWidth() - BoardLayout.FRAME_MARGIN * 2 - 25;
        int height = boardScrollPane.getHeight() - BoardLayout.FRAME_MARGIN * 2 - 25;
        if (width <= 0 || height <= 0) return;

        baseTileSize = Math.max(3, Math.min(
                width / BoardLayout.GRID_COLUMNS,
                height / BoardLayout.GRID_ROWS
        ));
        board.setTileSize(Math.max(3, (int) Math.round(baseTileSize * zoom)));
        boardCanvas.revalidate();
    }

    private void scrollWithWheel(JScrollBar scrollBar, MouseWheelEvent event) {
        event.consume();
        int offset = (int) Math.round(event.getPreciseWheelRotation() * scrollBar.getUnitIncrement() * 3);
        scrollBar.setValue(scrollBar.getValue() + offset);
    }

    private void moveViewport(int x, int y) {
        JViewport viewport = boardScrollPane.getViewport();
        Dimension view = viewport.getViewSize();
        Dimension extent = viewport.getExtentSize();
        viewport.setViewPosition(new Point(
                Math.max(0, Math.min(x, Math.max(0, view.width - extent.width))),
                Math.max(0, Math.min(y, Math.max(0, view.height - extent.height)))
        ));
    }

    private void zoomAt(MouseWheelEvent event) {
        event.consume();
        double nextZoom = Math.max(0.50, Math.min(3.00,
                zoom * Math.pow(1.12, -event.getPreciseWheelRotation())));
        if (Math.abs(nextZoom - zoom) < 0.001) return;

        JViewport viewport = boardScrollPane.getViewport();
        Point mouseInViewport = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), viewport);
        Point mouseInBoard = SwingUtilities.convertPoint(viewport, mouseInViewport, board);

        double fractionX = Math.max(0, Math.min(1, mouseInBoard.x / (double) Math.max(1, board.getWidth())));
        double fractionY = Math.max(0, Math.min(1, mouseInBoard.y / (double) Math.max(1, board.getHeight())));

        zoom = nextZoom;
        board.setTileSize(Math.max(3, (int) Math.round(baseTileSize * zoom)));
        boardCanvas.revalidate();
        boardScrollPane.revalidate();

        SwingUtilities.invokeLater(() -> {
            boardScrollPane.doLayout();
            viewport.doLayout();
            boardCanvas.doLayout();

            int anchorX = board.getX() + (int) Math.round(board.getWidth() * fractionX);
            int anchorY = board.getY() + (int) Math.round(board.getHeight() * fractionY);
            moveViewport(anchorX - mouseInViewport.x, anchorY - mouseInViewport.y);
        });
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 55, 75));

        JLabel title = new JLabel("Jugadores");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title);

        for (Actor actor : actors) {
            JLabel name = new JLabel(actor.getName());
            name.setFont(new Font("Arial", Font.PLAIN, 20));
            name.setForeground(actor.getColor());
            panel.add(name);
        }

        return panel;
    }

    public BoardLayout getBoard() {
        return board;
    }

    public JScrollPane getBoardScrollPane() {
        return boardScrollPane;
    }
}
