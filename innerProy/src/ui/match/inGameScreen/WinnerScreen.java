package ui.match.inGameScreen;

import Networking.packets.GameOverPacket;
import utils.AllFonts;
import utils.AllThemes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;


public final class WinnerScreen extends JFrame {


    private final Timer revealTimer;
    private final FadePanel headline;
    private final FadePanel evidence;
    private int elapsedMs;

    /**
     * @param result        verified final match result received from the server
     * @param localPlayerID this client's own player ID
     * @param onReturn      application callback to close the match and show
     *                      the main menu (nullable; the screen still closes)
     */
    public WinnerScreen(GameOverPacket result, int localPlayerID, Runnable onReturn) {
        Objects.requireNonNull(result, "Game over packet is required");
        if (!result.getGameOver()) {
            throw new IllegalArgumentException("Cannot show a winner while the game is running");
        }

        boolean localPlayerWon = result.getWinnerID() >= 0
                && result.getWinnerID() == localPlayerID;
        String winnerName = readable(result.getWinnerName(), "No winner");

        setTitle("Deusto Cluedo | Case Closed");
        setSize(1120, 790);
        setMinimumSize(new Dimension(850, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        GradientBackground background = new GradientBackground();
        background.setLayout(new GridBagLayout());
        setContentPane(background);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(28, 40, 28, 40));

        headline = new FadePanel();
        headline.setLayout(new BoxLayout(headline, BoxLayout.Y_AXIS));
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        headline.add(centeredLabel("✦  DEUSTO CLUEDO  ✦",
                AllFonts.GEORGIA_PLAIN_14, AllThemes.CremaDM));
        headline.add(Box.createVerticalStrut(14));
        headline.add(centeredLabel("THE MYSTERY HAS BEEN SOLVED",
                AllFonts.GEORGIA_BOLD_28, AllThemes.DoradorDM));
        headline.add(Box.createVerticalStrut(22));
        headline.add(centeredLabel(localPlayerWon ? "YOU WON!" : "THE WINNER IS",
                AllFonts.GEORGIA_BOLD_21, AllThemes.CremaDM));
        headline.add(Box.createVerticalStrut(10));
        int nameSize = winnerName.length() > 23 ? 33 : winnerName.length() > 15 ? 41 : 52;
        headline.add(centeredLabel(winnerName,
                new Font("Georgia", Font.BOLD, nameSize), AllThemes.DoradorDM));
        headline.add(Box.createVerticalStrut(9));
        headline.add(centeredLabel(localPlayerWon
                        ? "You uncovered the truth. Case closed."
                        : "The case has finally been solved.",
                AllFonts.SANS_SERIF_PLAIN_15, AllThemes.CremaDM));

        evidence = new FadePanel();
        evidence.setLayout(new BoxLayout(evidence, BoxLayout.Y_AXIS));
        evidence.setAlignmentX(Component.CENTER_ALIGNMENT);
        evidence.add(centeredLabel("THE FINAL REVELATION",
                AllFonts.GEORGIA_BOLD_19, AllThemes.CremaDM));
        evidence.add(Box.createVerticalStrut(18));

        JPanel cards = new JPanel(new GridLayout(1, 3, 17, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.CENTER_ALIGNMENT);
        cards.setPreferredSize(new Dimension(870, 170));
        cards.setMaximumSize(new Dimension(870, 170));
        cards.add(new CaseCard("01", "MURDERER", readable(result.getMurderer(), "Not revealed")));
        cards.add(new CaseCard("02", "WEAPON", readable(result.getWeapon(), "Not revealed")));
        cards.add(new CaseCard("03", "LOCATION", readable(result.getRoom(), "Not revealed")));
        evidence.add(cards);

        JButton returnButton = new JButton("RETURN TO MAIN MENU");
        returnButton.setFont(AllFonts.GEORGIA_BOLD_15);
        returnButton.setForeground(AllThemes.CremaDM);
        returnButton.setBackground(AllThemes.GranateDM);
        returnButton.setFocusPainted(false);
        returnButton.setOpaque(true);
        returnButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AllThemes.DoradorDM, 1),
                new EmptyBorder(14, 28, 14, 28)));
        returnButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnButton.addActionListener(event -> {
            dispose();
            if (onReturn != null) {
                onReturn.run();
            }
        });

        content.add(headline);
        content.add(Box.createVerticalStrut(30));
        content.add(evidence);
        content.add(Box.createVerticalStrut(33));
        content.add(returnButton);

        background.add(content);

        revealTimer = new Timer(30, e -> {
            elapsedMs += 30;
            headline.setOpacity(Math.min(1f, elapsedMs / 650f));
            evidence.setOpacity(Math.min(1f, Math.max(0f, (elapsedMs - 450f) / 700f)));
            if (elapsedMs >= 1200) {
                ((Timer) e.getSource()).stop();
            }
        });
        revealTimer.start();

    }
    public static void main(String[] args) {
        boolean previewAsLoser = args.length > 0
                && "lose".equalsIgnoreCase(args[0]);

        SwingUtilities.invokeLater(() -> {
            GameOverPacket testResult = new GameOverPacket(
                    1,          // Winner ID
                    "Finse",            // Winner name
                    "ANTAL",            // Murderer
                    "TECLADO",          // Weapon
                    "CRAI"              // Room
            );

            int localPlayerID = previewAsLoser ? 2 : 1;

            WinnerScreen preview = new WinnerScreen(
                    testResult,
                    localPlayerID
            );

            preview.setVisible(true);
        });
    }

    public WinnerScreen(GameOverPacket result, int localPlayerID) {
        this(result, localPlayerID, null);
    }

    @Override
    public void dispose() {
        if (revealTimer != null) {
            revealTimer.stop();
        }
        super.dispose();
    }

    private static String readable(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static JLabel centeredLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private static final class FadePanel extends JPanel {
        private float opacity;

        FadePanel() {
            setOpaque(false);
        }

        void setOpacity(float value) {
            opacity = Math.max(0f, Math.min(1f, value));
            repaint();
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            super.paint(g2);
            g2.dispose();
        }
    }

    private static final class GradientBackground extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, AllThemes.Granate2DM,
                    0, getHeight(), AllThemes.NegroDM));
            g2.fillRect(0, 0, getWidth(), getHeight());

            float radius = Math.max(1, Math.max(getWidth(), getHeight()) * .68f);
            g2.setPaint(new RadialGradientPaint(
                    new Point(getWidth() / 2, getHeight() / 3), radius,
                    new float[]{0f, 1f},
                    new Color[]{AllThemes.MarronOscuro2DM, AllThemes.MoradoOscuroDM}));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static final class CaseCard extends JPanel {
        CaseCard(String number, String category, String solution) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(20, 12, 16, 12));

            add(centeredLabel(number, AllFonts.GEORGIA_BOLD_19, AllThemes.DoradorDM));
            add(Box.createVerticalStrut(8));
            add(centeredLabel(category, AllFonts.SANS_SERIF_BOLD_12, AllThemes.CremaDM));
            add(Box.createVerticalStrut(17));
            int fontSize = solution.length() > 18 ? 15 : 20;
            add(centeredLabel(solution, new Font("Georgia", Font.BOLD, fontSize), AllThemes.CremaDM));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape box = new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
            g2.setColor(AllThemes.MoradoOscuroDM);
            g2.fill(box);
            g2.setColor(AllThemes.MielDM);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(box);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
