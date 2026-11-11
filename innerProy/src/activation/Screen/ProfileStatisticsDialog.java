package activation.Screen;

import game.statistics.LocalProfileStatistics;
import game.statistics.PlayerStatistics;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class ProfileStatisticsDialog {

    private ProfileStatisticsDialog() {}

    public static void open(
            JFrame owner,
            Runnable onChangePicture
    ) {

        LocalProfileStatistics store =
                LocalProfileStatistics.getInstance();

        JDialog dialog = new JDialog(
                owner,
                "Profile Statistics",
                Dialog.ModalityType.MODELESS
        );

        dialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );

        dialog.setLayout(new BorderLayout());

        AbstractTableModel model = new AbstractTableModel() {

            private final String[] labels = {
                    "Games Played",
                    "Games Won",
                    "Games Lost",
                    "Win Rate",
                    "Correct Accusations",
                    "Average Match Duration",
                    "Total Points"
            };

            @Override
            public int getRowCount() {
                return labels.length;
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public String getColumnName(int column) {
                return column == 0 ? "Statistic" : "Value";
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Object getValueAt(int row, int column) {

                if (column == 0) {
                    return labels[row];
                }

                PlayerStatistics stats = store.snapshot();

                return switch (row) {

                    case 0 -> stats.getGamesPlayed();

                    case 1 -> stats.getWins();

                    case 2 -> stats.getLosses();

                    case 3 -> String.format(
                            Locale.ROOT,
                            "%.1f%%",
                            stats.getWinRate() * 100.0
                    );

                    case 4 -> stats.getCorrectAccusations();

                    case 5 -> {
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(
                                stats.getAverageGameDurationMillis()
                        );

                        yield String.format(
                                Locale.ROOT,
                                "%02d:%02d",
                                seconds / 60,
                                seconds % 60
                        );
                    }

                    // No point-scoring system exists yet.
                    case 6 -> "Not tracked yet";

                    default -> "";
                };
            }
        };

        JTable table = new JTable(model);

        JScrollPane scrollPane =
                CluedoTableTheme.style(table);

        dialog.add(scrollPane, BorderLayout.CENTER);

        if (onChangePicture != null) {

            JButton changePicture =
                    new JButton("Change Profile Picture");

            changePicture.setBackground(
                    new Color(75, 38, 42)
            );

            changePicture.setForeground(
                    new Color(225, 190, 140)
            );

            changePicture.setFont(
                    new Font("Georgia", Font.BOLD, 14)
            );

            changePicture.setFocusPainted(false);

            changePicture.addActionListener(
                    event -> onChangePicture.run()
            );

            dialog.add(changePicture, BorderLayout.SOUTH);
        }

        // Refresh automatically after a game finishes.
        Runnable refresh = () -> SwingUtilities.invokeLater(() -> {

            if (dialog.isDisplayable()) {
                model.fireTableDataChanged();
            }

        });

        store.addChangeListener(refresh);

        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent event) {
                store.removeChangeListener(refresh);
            }
        });

        dialog.getContentPane().setBackground(
                new Color(20, 22, 27)
        );

        dialog.setSize(450, 375);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
