package ui.boardPiece;

import javax.swing.*;
import java.awt.*;

public class ScenarioAspect extends JPanel {

    public static final int SIZE = TileAspect.SIZE * 2;

    public ScenarioAspect(int x, int y) {

        setBounds(
                x,
                y,
                SIZE,
                SIZE
        );

        setBackground(Color.LIGHT_GRAY);

        setBorder(
                BorderFactory.createLineBorder(
                        Color.BLACK,
                        3
                )
        );
    }
}