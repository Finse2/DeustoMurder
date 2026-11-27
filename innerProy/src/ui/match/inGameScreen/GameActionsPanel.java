
package ui.match.inGameScreen;

import game.core.GameSetup;
import game.dice.Dice;
import ui.match.DetectiveCard;
import ui.match.AccusationCard;
import ui.NoirActionButton;
import ui.NoirTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Panel inferior derecho con los botones de las tarjetas del juego.
 */
public final class GameActionsPanel extends JPanel {

    private static final Color BACKGROUND = NoirTheme.PANEL;

    private static final Color GOLD = NoirTheme.GOLD;

    private static final Color TEXT = NoirTheme.TEXT;

    private static final Color SECONDARY_TEXT = NoirTheme.SECONDARY_TEXT;

    private static final Color BUTTON_BACKGROUND = NoirTheme.SURFACE;

    private static final Color BORDER_COLOR = NoirTheme.INNER_BORDER;

    private JDialog detectiveDialog;
    private JDialog accusationDialog;
    private final DiceRollPanel diceRollPanel;
    private Runnable onDiceRequested;
    private final Dice diceModel;
    private BiConsumer<Integer, Integer> onDiceRolled;

    private final JButton detectiveButton =
            new NoirActionButton(
                    "Detective Notebook",
                    "Review your evidence",
                    NoirActionButton.Style.SECONDARY
            );

    private final JButton accusationButton =
            new NoirActionButton(
                    "Make an Accusation",
                    "Reveal your final theory",
                    NoirActionButton.Style.DANGER
            );

    private final JButton diceButton =
            new NoirActionButton(
                    "Roll Dice",
                    "Make your next move",
                    NoirActionButton.Style.PRIMARY
            );
    private final JButton endTurnButton =
            new NoirActionButton(
                    "End Turn",
                    "Finish your investigation turn",
                    NoirActionButton.Style.SECONDARY
            );
    private final JPanel diceResultPanel = new JPanel(new BorderLayout());
    private final JLabel diceResultLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel diceResultHint = new JLabel("", SwingConstants.CENTER);
    private boolean diceResultVisible;

    public GameActionsPanel() {
        this(new Dice());
    }

    public GameActionsPanel(Dice dice) {

        super(new BorderLayout(0, 6));

        this.diceModel = java.util.Objects.requireNonNull(
                dice, "Dice model cannot be null"
        );

        this.diceRollPanel = new DiceRollPanel(
                diceModel, new Random()
        );

        diceRollPanel.setVisible(false);

        diceRollPanel.setOnRollComplete((first, second) -> {

            showDiceResult(first, second);

            if (onDiceRolled != null) {
                onDiceRolled.accept(first, second);
            }
        });

        // PANEL STYLE

        setBackground(BACKGROUND);

        setBorder(
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );

        // HEADER

        add(createActionsHeader(), BorderLayout.NORTH);

        // ACTION LISTENERS

        detectiveButton.addActionListener(
                event -> toggleDetectiveCard()
        );

        accusationButton.addActionListener(
                event -> showAccusationCard()
        );

        diceButton.addActionListener(
                event -> showDiceOnTable()
        );

        // BUTTON CONTAINER

        JPanel buttonsPanel = new JPanel();

        buttonsPanel.setLayout(
                new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS)
        );

        buttonsPanel.setOpaque(false);

        // MAIN ACTIONS

        buttonsPanel.add(diceButton);
        buttonsPanel.add(Box.createVerticalStrut(4));

        buttonsPanel.add(detectiveButton);
        buttonsPanel.add(Box.createVerticalStrut(4));

        buttonsPanel.add(accusationButton);

        // END TURN BUTTON

        configureEndTurnButton();

        buttonsPanel.add(endTurnButton);

        // DICE RESULT

        configureDiceResultPanel();

        buttonsPanel.add(Box.createVerticalStrut(4));

        add(buttonsPanel, BorderLayout.CENTER);
    }

    private JPanel createActionsHeader() {

        JPanel header = new JPanel(
                new BorderLayout(0, 5)
        );

        header.setOpaque(false);

        JLabel title = new JLabel("DETECTIVE ACTIONS");

        title.setFont(NoirTheme.TITLE_FONT);
        title.setForeground(NoirTheme.TEXT);

        JLabel subtitle = new JLabel(
                "Your investigation tools"
        );

        subtitle.setFont(NoirTheme.SUBTITLE_FONT);
        subtitle.setForeground(NoirTheme.SECONDARY_TEXT);

        JPanel titlePanel = new JPanel();

        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        titlePanel.setOpaque(false);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(subtitle);

        JPanel titleRow = new JPanel(
                new BorderLayout(8, 0)
        );

        titleRow.setOpaque(false);

        titleRow.add(createActionsIcon(), BorderLayout.WEST);
        titleRow.add(titlePanel, BorderLayout.CENTER);

        JPanel separator = new JPanel();

        separator.setBackground(NoirTheme.BORDER);
        separator.setPreferredSize(new Dimension(1, 1));

        header.add(titleRow, BorderLayout.CENTER);
        header.add(separator, BorderLayout.SOUTH);

        return header;
    }

    private JComponent createActionsIcon() {

        JComponent icon = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(GOLD);

                g2.setStroke(new BasicStroke(
                        1.6f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                int x = (getWidth() - 26) / 2;
                int y = (getHeight() - 26) / 2;

                g2.translate(x, y);

                // MAGNIFYING GLASS

                g2.drawOval(2, 2, 16, 16);

                g2.drawLine(15, 15, 23, 23);

                g2.drawArc(5, 5, 10, 10, 40, 100);

                g2.dispose();
            }
        };

        icon.setPreferredSize(new Dimension(30, 30));

        return icon;
    }

    // TARJETA DEL DETECTIVE

    public void toggleDetectiveCard() {

        if (detectiveDialog == null || !detectiveDialog.isDisplayable()) {
            createDetectiveDialog();
        }

        if (detectiveDialog.isVisible()) {
            detectiveDialog.setVisible(false);
            detectiveButton.setSelected(false);
        } else {
            detectiveDialog.setVisible(true);
            detectiveDialog.toFront();
            detectiveButton.setSelected(true);
        }
    }

    private void configureEndTurnButton() {

        endTurnButton.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        endTurnButton.setVisible(false);
        endTurnButton.setEnabled(false);
    }

    private void configureDiceResultPanel() {

        diceResultPanel.setOpaque(true);

        diceResultPanel.setBackground(BUTTON_BACKGROUND);

        diceResultPanel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        diceResultPanel.setPreferredSize(
                new Dimension(280, 38)
        );

        diceResultPanel.setMinimumSize(
                new Dimension(160, 38)
        );

        diceResultPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );

        diceResultPanel.setBorder(
                BorderFactory.createLineBorder(BORDER_COLOR)
        );

        diceResultLabel.setFont(
                new Font("Georgia", Font.BOLD, 17)
        );

        diceResultLabel.setForeground(GOLD);

        diceResultHint.setFont(
                new Font("Arial", Font.PLAIN, 10)
        );

        diceResultHint.setForeground(SECONDARY_TEXT);

        diceResultPanel.add(
                diceResultLabel, BorderLayout.CENTER
        );

        diceResultPanel.add(
                diceResultHint, BorderLayout.SOUTH
        );

        clearDiceResult();
    }

    public void showDetectiveCard() {

        if (detectiveDialog == null || !detectiveDialog.isDisplayable()) {
            createDetectiveDialog();
        }

        detectiveDialog.setVisible(true);
        detectiveDialog.toFront();
        detectiveButton.setSelected(true);
    }

    private void createDetectiveDialog() {

        Window owner = SwingUtilities.getWindowAncestor(this);

        detectiveDialog = new JDialog(
                owner,
                "Tarjeta de marcaje",
                Dialog.ModalityType.MODELESS
        );

        detectiveDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        detectiveDialog.setContentPane(new DetectiveCard(
                GameSetup.createActors(),
                GameSetup.createWeapons(),
                GameSetup.createRooms()
        ));

        detectiveDialog.setSize(520, 750);
        detectiveDialog.setMinimumSize(new Dimension(450, 550));
        detectiveDialog.setLocationRelativeTo(owner);

        JDialog currentDialog = detectiveDialog;

        detectiveDialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent event) {
                detectiveButton.setSelected(false);
            }

            @Override
            public void windowClosed(WindowEvent event) {
                detectiveButton.setSelected(false);

                if (detectiveDialog == currentDialog) {
                    detectiveDialog = null;
                }
            }
        });
    }

    // TARJETA DE ACUSACIÓN

    public void showAccusationCard() {

        if (accusationDialog == null || !accusationDialog.isDisplayable()) {
            createAccusationDialog();
        }

        accusationDialog.setVisible(true);
        accusationDialog.toFront();
    }

    private void createAccusationDialog() {

        Window owner = SwingUtilities.getWindowAncestor(this);

        accusationDialog = new JDialog(
                owner,
                "Tarjeta de acusación",
                Dialog.ModalityType.MODELESS
        );

        accusationDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        accusationDialog.setContentPane(new AccusationCard(
                GameSetup.createActors(),
                GameSetup.createWeapons(),
                GameSetup.createRooms(),
                selection -> {

                    System.out.println("ACUSACIÓN CONFIRMADA");
                    System.out.println("Sospechoso: " + selection.actor().getName());
                    System.out.println("Arma: " + selection.weapon().getName());
                    System.out.println("Ubicación: " + selection.room().getName());

                }
        ));

        accusationDialog.setSize(520, 800);
        accusationDialog.setMinimumSize(new Dimension(450, 600));
        accusationDialog.setLocationRelativeTo(owner);

        JDialog currentDialog = accusationDialog;

        accusationDialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent event) {
                if (accusationDialog == currentDialog) {
                    accusationDialog = null;
                }
            }
        });
    }

    // DADOS ANIMADOS SOBRE EL TABLERO

    /** El contenedor principal instala la accion que activa la capa de dados. */
    public void setOnDiceRequested(Runnable listener) {
        onDiceRequested = listener;
    }

    public void showDiceOnTable() {
        if (onDiceRequested == null) return;
        if (diceRollPanel.isVisible() || diceRollPanel.isHolding() || diceRollPanel.isRolling()) return;
        clearDiceResult();
        onDiceRequested.run();
    }

    /** El resultado queda visible en el panel derecho hasta la siguiente tirada. */
    private void showDiceResult(int first, int second) {

        int total = first + second;

        diceResultLabel.setText(
                first + " + " + second + " = " + total
        );

        diceResultHint.setText("Dice roll result");

        diceResultPanel.setBackground(BUTTON_BACKGROUND);

        diceResultPanel.setBorder(
                BorderFactory.createLineBorder(GOLD)
        );

        diceResultVisible = true;

        diceResultPanel.setVisible(true);

        revalidate();
        repaint();
    }

    private void clearDiceResult() {

        diceResultLabel.setText("");
        diceResultHint.setText("");

        diceResultVisible = false;

        diceResultPanel.setVisible(false);

        revalidate();
        repaint();
    }

    /** Permite comprobar el resultado sin acceder a componentes internos. */
    public boolean isDiceResultVisible() { return diceResultVisible; }
    public String getDiceResultText() { return diceResultLabel.getText(); }

    /** Alias compatible con el codigo anterior; ya no abre un dialogo. */
    public void showDiceDialog() {
        showDiceOnTable();
    }

    /** Permite integrar el resultado con el juego (movimientos, historial, etc.). */
    public void setOnDiceRolled(BiConsumer<Integer, Integer> listener) {
        onDiceRolled = listener;
    }

    /** Habilita el boton especial del mock, sin afectar a la partida normal. */
    public void showMockEndTurnButton(Runnable onEndTurn) {
        endTurnButton.setVisible(true);
        for (var listener : endTurnButton.getActionListeners()) endTurnButton.removeActionListener(listener);
        if (onEndTurn != null) endTurnButton.addActionListener(event -> onEndTurn.run());
        revalidate();
        repaint();
    }

    public JButton getEndTurnButton() { return endTurnButton; }

    public JButton getDiceButton() {
        return diceButton;
    }

    public DiceRollPanel getDiceRollPanel() {
        return diceRollPanel;
    }

    // GETTERS

    public JButton getDetectiveButton() {
        return detectiveButton;
    }

    public JButton getAccusationButton() {
        return accusationButton;
    }
}
