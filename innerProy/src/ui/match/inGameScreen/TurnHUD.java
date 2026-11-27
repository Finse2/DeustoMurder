package ui.match.inGameScreen;

import javax.swing.*;
import java.awt.*;

public final class TurnHUD extends JPanel {

    private static final Color BACKGROUND =
            new Color(12, 34, 46);

    private static final Color GOLD =
            new Color(214, 179, 113);

    private static final Color TEXT =
            new Color(232, 214, 181);

    private static final Color SECONDARY_TEXT =
            new Color(145, 169, 176);

    private final JLabel playerLabel =
            new JLabel("Waiting for turn");

    private final JLabel diceLabel =
            new JLabel("Dice result: —");

    private final JLabel movesLabel =
            new JLabel("Moves remaining: —");

    private final JProgressBar movesBar =
            new JProgressBar(0, 12);

    public TurnHUD() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setBackground(BACKGROUND);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel title = new JLabel("CURRENT TURN");

        title.setFont(new Font("Georgia", Font.BOLD, 15));
        title.setForeground(GOLD);

        playerLabel.setFont(new Font("Georgia", Font.BOLD, 19));
        playerLabel.setForeground(TEXT);

        diceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        diceLabel.setForeground(SECONDARY_TEXT);

        movesLabel.setFont(new Font("Arial", Font.BOLD, 12));
        movesLabel.setForeground(GOLD);

        movesBar.setForeground(GOLD);
        movesBar.setBackground(new Color(41, 67, 75));

        movesBar.setBorderPainted(false);
        movesBar.setStringPainted(false);
        movesBar.setValue(0);

        movesBar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 8)
        );

        title.setAlignmentX(LEFT_ALIGNMENT);
        playerLabel.setAlignmentX(LEFT_ALIGNMENT);
        diceLabel.setAlignmentX(LEFT_ALIGNMENT);
        movesLabel.setAlignmentX(LEFT_ALIGNMENT);
        movesBar.setAlignmentX(LEFT_ALIGNMENT);

        add(title);
        add(Box.createVerticalStrut(8));

        add(playerLabel);
        add(Box.createVerticalStrut(10));

        add(diceLabel);
        add(Box.createVerticalStrut(6));

        add(movesLabel);
        add(Box.createVerticalStrut(6));

        add(movesBar);
    }

    public void startTurn(String playerName) {

        playerLabel.setText(
                playerName == null
                        ? "Waiting for turn"
                        : playerName
        );

        diceLabel.setText("Dice result: —");
        movesLabel.setText("Moves remaining: —");

        movesBar.setValue(0);
    }

    public void setDiceResult(int first, int second) {

        int total = first + second;

        diceLabel.setText(
                "Dice result: " + first + " + " + second
                        + " = " + total
        );

        movesBar.setMaximum(total);

        setRemainingMoves(total);
    }

    public void setRemainingMoves(int remaining) {

        int value = Math.max(
                0, Math.min(remaining, movesBar.getMaximum())
        );

        movesLabel.setText(
                "Moves remaining: " + value
                        + " / " + movesBar.getMaximum()
        );

        movesBar.setValue(value);
    }
}