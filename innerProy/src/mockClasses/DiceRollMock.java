package mockClasses;

import ui.match.inGameScreen.DiceRollPanel;
import ui.match.inGameScreen.WoodTablePanel;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/** Mock visual para probar la mecánica hold-to-shake / release-to-drop. */
public final class DiceRollMock {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("DeustoMurder - Mock de dados animados");
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            WoodTablePanel table = new WoodTablePanel(new BorderLayout());
            DiceRollPanel dice = new DiceRollPanel();
            JLabel result = new JLabel("Mantén pulsado sobre la mesa y suelta para lanzar los dados", SwingConstants.CENTER);
            result.setForeground(new Color(220, 199, 163));
            result.setFont(new Font("Georgia", Font.BOLD, 16));

            dice.setOnRollComplete((first, second) -> {
                result.setText("Primero: " + first + "   |   Segundo: " + second
                        + "   |   Total: " + (first + second));
                System.out.println("RESULTADO: " + first + " + " + second + " = " + (first + second));
            });

            table.add(dice, BorderLayout.CENTER);
            dice.prepareRoll();
            table.add(result, BorderLayout.SOUTH);
            window.setContentPane(table);
            window.setSize(800, 570);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}