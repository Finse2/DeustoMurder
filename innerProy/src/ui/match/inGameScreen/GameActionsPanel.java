
package ui.match.inGameScreen;

import game.core.GameSetup;
import game.dice.Dice;
import ui.DetectiveButton;
import ui.match.DetectiveCard;
import ui.match.AccusationCard;

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

    private JDialog detectiveDialog;
    private JDialog accusationDialog;
    private final DiceRollPanel diceRollPanel;
    private Runnable onDiceRequested;
    private final Dice diceModel;
    private BiConsumer<Integer, Integer> onDiceRolled;

    private final DetectiveButton detectiveButton = new DetectiveButton();
    private final JButton accusationButton = new JButton("Accusation Card");
    private final JButton diceButton = new JButton("Tirar dados");
    private final JPanel diceResultPanel = new JPanel(new BorderLayout());
    private final JLabel diceResultLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel diceResultHint = new JLabel("", SwingConstants.CENTER);
    private boolean diceResultVisible;

    public GameActionsPanel() {
        this(new Dice());
    }

    public GameActionsPanel(Dice dice) {
        super(new BorderLayout());
        this.diceModel = java.util.Objects.requireNonNull(dice, "Dice model cannot be null");
        this.diceRollPanel = new DiceRollPanel(diceModel, new Random());
        diceRollPanel.setVisible(false);
        diceRollPanel.setOnRollComplete((first, second) -> {
            showDiceResult(first, second);
            if (onDiceRolled != null) onDiceRolled.accept(first, second);
        });

        setBackground(new Color(25, 55, 75));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        // Botón de la tarjeta del detective.
        detectiveButton.addActionListener(event -> toggleDetectiveCard());

        // Botón de la tarjeta de acusación.
        accusationButton.setFont(new Font("Georgia", Font.BOLD, 17));
        accusationButton.setForeground(new Color(25, 25, 25));
        accusationButton.setBackground(new Color(238, 207, 105));
        accusationButton.setFocusPainted(false);
        accusationButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accusationButton.setPreferredSize(new Dimension(0, 35));

        accusationButton.addActionListener(event -> showAccusationCard());

        // Colocamos los botones uno debajo del otro.
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(new Color(25, 55, 75));

        accusationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        detectiveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        detectiveButton.setPreferredSize(new Dimension(310, 35));

        buttonsPanel.add(accusationButton);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(detectiveButton);
        buttonsPanel.add(Box.createVerticalStrut(10));

        // Los dados aparecen encima de la mesa, no en un JDialog.
        diceButton.setFont(new Font("Georgia", Font.BOLD, 17));
        diceButton.setForeground(new Color(229, 211, 177));
        diceButton.setBackground(new Color(64, 38, 28));
        diceButton.setFocusPainted(false);
        diceButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        diceButton.setPreferredSize(new Dimension(0, 35));
        diceButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        diceButton.addActionListener(event -> showDiceOnTable());
        buttonsPanel.add(diceButton);

        // Espacio fijo bajo el boton: el resto de controles no se mueve al aparecer el resultado.
        diceResultPanel.setOpaque(false);
        diceResultPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        diceResultPanel.setPreferredSize(new Dimension(240, 60));
        diceResultPanel.setMinimumSize(new Dimension(170, 60));
        diceResultPanel.setMaximumSize(new Dimension(240, 60));

        diceResultLabel.setFont(new Font("Georgia", Font.BOLD, 21));
        diceResultLabel.setForeground(new Color(240, 220, 180));
        diceResultHint.setFont(new Font("Arial", Font.PLAIN, 11));
        diceResultHint.setForeground(new Color(205, 180, 143));

        diceResultPanel.add(diceResultLabel, BorderLayout.CENTER);
        diceResultPanel.add(diceResultHint, BorderLayout.SOUTH);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(diceResultPanel);

        add(buttonsPanel, BorderLayout.SOUTH);
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
        diceResultLabel.setText(first + " + " + second + " = " + (first + second));
        diceResultHint.setText("Resultado final");
        diceResultPanel.setBackground(new Color(29, 19, 13));
        diceResultPanel.setOpaque(true);
        diceResultPanel.setBorder(BorderFactory.createLineBorder(new Color(205, 165, 105), 2));
        diceResultVisible = true;
        diceResultPanel.repaint();
    }

    private void clearDiceResult() {
        diceResultLabel.setText("");
        diceResultHint.setText("");
        diceResultPanel.setOpaque(false);
        diceResultPanel.setBorder(null);
        diceResultVisible = false;
        diceResultPanel.repaint();
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
