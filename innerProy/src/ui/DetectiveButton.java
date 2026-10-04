package ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DetectiveButton extends JButton {
    private static final Color PAPER = new Color (235, 220, 185);
    private static final Color PAPER_HOVER = new Color (245, 230, 195);
    private static final Color PAPER_PRESSED = new Color (215, 195, 155);

    private static final Color DARK = new Color (45, 35, 25);
    private static final Color GOLD = new Color (170, 125, 50);
    private static final Color GOLD_BRIGHT = new Color (220, 175, 75);
    private static final Color TEXT_HOVER = new Color (150, 105, 35);

    // ESTADO DEL RATÓN

    private boolean mouseOver = false;
    private boolean pressed = false;

    private float glow = 0;
    private Timer animationTimer;

    public DetectiveButton () {
        super("Tarjeta del detective");
        setFont(new Font ("Serif", Font.BOLD, 17));
        setForeground(DARK);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension (310, 70));

        // EFECTO DEL RATÓN

        addMouseListener(new MouseAdapter() {
            @Override

            public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                SoundEffect.playHover();
                startGlowAnimation();
            }

            @Override

            public void mouseExited (MouseEvent e) {
                mouseOver = false;
                pressed = false;
                startGlowAnimation();
            }

            public void mousePressed (MouseEvent e){
                pressed = true;

                SoundEffect.playClick();
                repaint();
            }

            public void mouseReleased (MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
        }

        private void startGlowAnimation() {
        if (animationTimer!= null && animationTimer.isRunning()) {
            return;
        }

        animationTimer = new Timer (20, e -> {
            if (mouseOver) {
                if (glow < 1) {
                    glow += 0.08f;
                }
            } else {
                if (glow > 0) {
                    glow -= 0.08f;
                }
            }

            if (glow < 0) {
                glow = 0;
            }

            repaint();

            if (!mouseOver && glow == 0) {
                animationTimer.stop();
            }
        });

        animationTimer.start();

        }

    // DIBUJAR BOTÓN
    @Override
    protected void paintComponent (Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Antialiasing

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // EFECTO DE PULSACIÓN

        int offset = pressed ? 4 : 0;

        // COLOR DEL BOTÓN

        Color background;

        if (pressed) {
            background = PAPER_PRESSED;
        } else if (mouseOver) {
            background = PAPER_HOVER;
        } else {
            background = PAPER;
        }

        // SOMBRA

        if (!pressed) {
            g2.setColor(new Color (0,0,0,45));
            g2.fillRoundRect(5, 6, getWidth()-10, getHeight()-5, 20, 20);
        }

        // FONDO

        g2.setColor(background);
        g2.fillRoundRect(
                3, 3 + offset, getWidth()-6, getHeight()-6, 20, 20
        );

        // BORDE EXTERIOR

        g2.setColor(DARK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(3, 3 + offset, getWidth()-6, getHeight()-6, 20, 20);

        // BORDE DORADO EXTERIOR

        Color borderColor;

        if (mouseOver) {
            borderColor = GOLD_BRIGHT;
        } else {
            borderColor = GOLD;
        }

        g2.setColor (borderColor);

        g2.setStroke(new BasicStroke(2));

        g2.drawRoundRect(8, 8 + offset, getWidth()-16, getHeight()-16, 14, 14);

        // BRILLO

        if (glow > 0) {
            int alpha = (int) (100 * glow);
            g2.setColor(new Color (255, 215, 100, alpha));
        }

        g2.setStroke(new BasicStroke(4));

        g2.drawRoundRect(5, 5 + offset, getWidth() - 10, getHeight(), 18, 18);

        // LUPA

        int lupaOffset = (int) (3 * glow);

        g2.setColor(DARK);

        g2.setStroke(new BasicStroke(3));

        // PARTE CIRCULAR

        g2.drawOval(getWidth()-58 + lupaOffset, 17 + offset, 24, 24);

        // MANGO

        g2.drawLine(getWidth()-38 + lupaOffset, 38 + offset, getWidth()-23 + lupaOffset, 53 + offset);

        // TEXTO

        g2.setFont(getFont());

        FontMetrics metrics =
                g2.getFontMetrics();

        int textX = 25;

        int textY =
                (getHeight() - metrics.getHeight()) / 2
                        + metrics.getAscent()
                        + offset;

        // Color del texto dependiendo del hover

        int red = (int) (
                DARK.getRed()
                        + (TEXT_HOVER.getRed() - DARK.getRed()) * glow
        );

        int green = (int) (
                DARK.getGreen()
                        + (TEXT_HOVER.getGreen() - DARK.getGreen()) * glow
        );

        int blue = (int) (
                DARK.getBlue()
                        + (TEXT_HOVER.getBlue() - DARK.getBlue()) * glow
        );
        g2.setColor(
                new Color(red, green, blue)
        );
        g2.drawString(
                "Tarjeta del detective",
                textX,
                textY
        );

    }

}
