
package ui.match.inGameScreen;

import game.core.GameSetup;
import ui.DetectiveButton;
import ui.match.DetectiveCard;
import ui.match.AccusationCard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Panel inferior derecho con los botones de las tarjetas del juego.
 */
public final class GameActionsPanel extends JPanel {

    private JDialog detectiveDialog;
    private JDialog accusationDialog;

    private final DetectiveButton detectiveButton = new DetectiveButton();
    private final JButton accusationButton = new JButton("Accusation Card");

    public GameActionsPanel() {
        super(new BorderLayout());

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
        accusationButton.setPreferredSize(new Dimension(0, 55));

        accusationButton.addActionListener(event -> showAccusationCard());

        // Colocamos los dos botones uno encima del otro.
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(new Color(25, 55, 75));

        accusationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        detectiveButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonsPanel.add(accusationButton);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(detectiveButton);

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

    // GETTERS

    public JButton getDetectiveButton() {
        return detectiveButton;
    }

    public JButton getAccusationButton() {
        return accusationButton;
    }
}
