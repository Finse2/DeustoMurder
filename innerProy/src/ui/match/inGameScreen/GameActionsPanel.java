package ui.match.inGameScreen;

import game.core.GameSetup;
import ui.DetectiveButton;
import ui.match.DetectiveCard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Botón inferior derecho que abre y oculta la tarjeta del detective. */
public final class GameActionsPanel extends JPanel {

    private JDialog detectiveDialog;
    private final DetectiveButton detectiveButton = new DetectiveButton();

    public GameActionsPanel() {
        super(new BorderLayout());
        setBackground(new Color(25, 55, 75));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        detectiveButton.addActionListener(event -> toggleDetectiveCard());
        add(detectiveButton, BorderLayout.SOUTH);
    }

    public void toggleDetectiveCard() {
        if (detectiveDialog == null || !detectiveDialog.isDisplayable()) {
            createDetectiveDialog();
        }

        // También funciona si el usuario la cerró con la X:
        // HIDE_ON_CLOSE oculta la ventana y isVisible() vuelve a ser false.
        if (detectiveDialog.isVisible()) {
            detectiveDialog.setVisible(false);
            detectiveButton.setSelected(false);
        } else {
            detectiveDialog.setVisible(true);
            detectiveDialog.toFront();
            detectiveButton.setSelected(true);
        }
    }

    /** Mantiene compatibilidad con cualquier llamada que quiera abrirla explícitamente. */
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
        detectiveDialog = new JDialog(owner, "Tarjeta de marcaje", Dialog.ModalityType.MODELESS);
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
                // La X oculta la tarjeta; el próximo clic vuelve a abrirla.
                detectiveButton.setSelected(false);
            }

            @Override
            public void windowClosed(WindowEvent event) {
                detectiveButton.setSelected(false);
                if (detectiveDialog == currentDialog) detectiveDialog = null;
            }
        });
    }

    public JButton getDetectiveButton() {
        return detectiveButton;
    }
}
