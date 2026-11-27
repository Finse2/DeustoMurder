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

    private final JLabel timerLabel =
            new JLabel("Time remaining: --:--");

    private final JProgressBar timerBar =
            new JProgressBar();

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

        // TURN TIMER

        timerLabel.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        timerLabel.setForeground(SECONDARY_TEXT);
        timerLabel.setAlignmentX(LEFT_ALIGNMENT);

        timerBar.setBackground(new Color(41, 67, 75));
        timerBar.setForeground(new Color(101, 187, 135));

        timerBar.setBorderPainted(false);
        timerBar.setStringPainted(false);
        timerBar.setAlignmentX(LEFT_ALIGNMENT);

        timerBar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 8)
        );

        add(Box.createVerticalStrut(12));
        add(timerLabel);
        add(Box.createVerticalStrut(6));
        add(timerBar);
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

    public void updateTimer(int remainingSeconds, int totalSeconds) {

        int total = Math.max(1, totalSeconds);
        int remaining = Math.max(0, Math.min(remainingSeconds, total));

        int minutes = remaining / 60;
        int seconds = remaining % 60;

        timerLabel.setText(
                String.format(
                        "Time remaining: %02d:%02d",
                        minutes, seconds
                )
        );

        timerBar.setMaximum(total);
        timerBar.setValue(remaining);

        double progress = (double) remaining / total;

        Color color;

        if (progress <= 0.15) {
            color = new Color(219, 119, 112);
        } else if (progress <= 0.35) {
            color = GOLD;
        } else {
            color = new Color(101, 187, 135);
        }

        timerLabel.setForeground(color);
        timerBar.setForeground(color);
    }

}