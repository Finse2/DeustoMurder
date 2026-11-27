package ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class NoirScrollBarUI extends BasicScrollBarUI {

    @Override
    protected void configureScrollBarColors() {

        trackColor = NoirTheme.BACKGROUND;
        thumbColor = NoirTheme.GOLD;
    }

    @Override
    protected void paintTrack(
            Graphics g,
            JComponent c,
            Rectangle trackBounds
    ) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(NoirTheme.BACKGROUND);
        g2.fillRect(
                trackBounds.x,
                trackBounds.y,
                trackBounds.width,
                trackBounds.height
        );

        g2.dispose();
    }

    @Override
    protected void paintThumb(
            Graphics g,
            JComponent c,
            Rectangle thumbBounds
    ) {

        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        if (isDragging) {
            g2.setColor(NoirTheme.CRIMSON);
        } else if (isThumbRollover()) {
            g2.setColor(NoirTheme.GOLD.brighter());
        } else {
            g2.setColor(NoirTheme.GOLD);
        }

        g2.fillRoundRect(
                thumbBounds.x + 2,
                thumbBounds.y + 2,
                Math.max(1, thumbBounds.width - 4),
                Math.max(1, thumbBounds.height - 4),
                8,
                8
        );

        g2.dispose();
    }

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

        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusable(false);

        return button;
    }
}