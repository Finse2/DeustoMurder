package activation.Screen.lobby;
import javax.swing.*;
import java.awt.*;

public class LoadingSpinner extends JPanel {

    private final Timer timer;
    private int rotation = 0;

    private final int lines = 12;
    private final Color spinnerColor = new Color(215, 180, 125);

    public LoadingSpinner() {
        setOpaque(false);
        setPreferredSize(new Dimension(80, 80));

        timer = new Timer(70, e -> {
            rotation = (rotation + 1) % lines;
            repaint();
        });
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        g2.translate(centerX, centerY);

        for (int i = 0; i < lines; i++) {

            float alpha = (i + 1f) / lines;

            int index = (i + rotation) % lines;

            double angle = Math.toRadians(index * 360.0 / lines);

            Graphics2D line = (Graphics2D) g2.create();

            line.rotate(angle);

            line.setColor(new Color(spinnerColor.getRed(),spinnerColor.getGreen(),spinnerColor.getBlue(),Math.round(alpha * 255)));

            line.setStroke(new BasicStroke(4,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));

            line.drawLine(0, -20, 0, -30);
            line.dispose();
        }

        g2.dispose();
    }
}