package ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public final class NoirScrollBarUI extends BasicScrollBarUI {

    private static final Color TRACK =
            new Color(16, 38, 49);

    private static final Color THUMB =
            new Color(160, 133, 87);

    private static final Color GOLD =
            new Color(214, 179, 113);

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createInvisibleButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createInvisibleButton();
    }

    private JButton createInvisibleButton() {

        JButton button = new JButton();

        Dimension size = new Dimension(0, 0);

        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);

        return button;
    }

    @Override
    protected void paintTrack(
            Graphics g, JComponent c, Rectangle bounds
    ) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(TRACK);

        g2.fillRoundRect(
                bounds.x, bounds.y,
                bounds.width, bounds.height,
                8, 8
        );

        g2.dispose();
    }

    @Override
    protected void paintThumb(
            Graphics g, JComponent c, Rectangle bounds
    ) {

        if (bounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(
                isDragging || isThumbRollover()
                        ? GOLD
                        : THUMB
        );

        g2.fillRoundRect(
                bounds.x + 2,
                bounds.y + 2,
                Math.max(1, bounds.width - 4),
                Math.max(1, bounds.height - 4),
                8, 8
        );

        g2.dispose();
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        return new Dimension(12, 30);
    }
}
