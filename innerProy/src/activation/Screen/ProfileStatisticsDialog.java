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

/** The same persistent, read-only profile table in menu, lobby, and game. */
public final class ProfileStatisticsDialog {
    private ProfileStatisticsDialog() {}

    public static void open(JFrame owner, Runnable onChangePicture) {
        LocalProfileStatistics store = LocalProfileStatistics.getInstance();
        JDialog dialog = new JDialog(owner, "Profile Statistics", Dialog.ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        AbstractTableModel model = new AbstractTableModel() {
            private PlayerStatistics stats = store.snapshot();
            private final String[] labels = {
                    "Games Played", "Games Won", "Games Lost", "Win Rate",
                    "Correct Accusations", "Average Match Duration", "Total Points"
            };

            @Override
            public int getRowCount() { return labels.length; }

            @Override
            public int getColumnCount() { return 2; }

            @Override
            public String getColumnName(int column) {
                return column == 0 ? "Statistic" : "Value";
            }

            @Override
            public Object getValueAt(int row, int column) {
                if (column == 0) return labels[row];
                return switch (row) {
                    case 0 -> stats.getGamesPlayed();
                    case 1 -> stats.getWins();
                    case 2 -> stats.getLosses();
                    case 3 -> String.format(Locale.ROOT, "%.1f%%", stats.getWinRate() * 100.0);
                    case 4 -> stats.getCorrectAccusations();
                    case 5 -> {
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(stats.getAverageGameDurationMillis());
                        yield String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
                    }
                    case 6 -> "Not tracked yet";
                    default -> "";
                };
            }

            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            public void refresh() {
                stats = store.snapshot();
                fireTableDataChanged();
            }
        };
        // Keep the displayed model synchronized even if a match finishes while the dialog is open.
        Runnable onUpdate = () -> SwingUtilities.invokeLater(() -> {
            if (dialog.isDisplayable()) {
                // Refresh through a table model that always reads the latest snapshot.
                ((AbstractTableModel) dialog.getRootPane().getClientProperty("profileStatsModel"))
                        .fireTableDataChanged();
            }
        });

        // A dedicated model is also used to make the first load and later updates identical.
        JTable table = new JTable(model);
        dialog.getRootPane().putClientProperty("profileStatsModel", model);
        dialog.add(CluedoTableTheme.style(table), BorderLayout.CENTER);

        if (onChangePicture != null) {
            JButton changePicture = new JButton("Change Profile Picture");
            changePicture.setBackground(new Color(75, 38, 42));
            changePicture.setForeground(new Color(225, 190, 140));
            changePicture.setFont(new Font("Georgia", Font.BOLD, 14));
            changePicture.setFocusPainted(false);
            changePicture.addActionListener(event -> onChangePicture.run());
            dialog.add(changePicture, BorderLayout.SOUTH);
        }

        // Always re-read the persisted statistics when notified of a result.
        Runnable refresh = () -> SwingUtilities.invokeLater(() -> {
            if (dialog.isDisplayable()) {
                for (int row = 0; row < table.getRowCount(); row++) {
                    // The model is rebuilt on the EDT; not by the network thread.
                }
                table.setModel(createUpdatedModel(store));
            }
        });
        store.addChangeListener(refresh);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                store.removeChangeListener(refresh);
            }
        });
        dialog.getContentPane().setBackground(new Color(20, 22, 27));
        dialog.setSize(450, 375);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private static AbstractTableModel createUpdatedModel(LocalProfileStatistics store) {
        PlayerStatistics stats = store.snapshot();
        final String[] labels = {
                "Games Played", "Games Won", "Games Lost", "Win Rate",
                "Correct Accusations", "Average Match Duration", "Total Points"
        };
        final Object[] values = {
                stats.getGamesPlayed(), stats.getWins(), stats.getLosses(),
                String.format(Locale.ROOT, "%.1f%%", stats.getWinRate() * 100.0),
                stats.getCorrectAccusations(),
                String.format(Locale.ROOT, "%02d:%02d",
                        TimeUnit.MILLISECONDS.toSeconds(stats.getAverageGameDurationMillis()) / 60,
                        TimeUnit.MILLISECONDS.toSeconds(stats.getAverageGameDurationMillis()) % 60),
                "Not tracked yet"
        };
        return new AbstractTableModel() {
            @Override public int getRowCount() { return labels.length; }
            @Override public int getColumnCount() { return 2; }
            @Override public String getColumnName(int column) { return column == 0 ? "Statistic" : "Value"; }
            @Override public Object getValueAt(int row, int column) {
                return column == 0 ? labels[row] : values[row];
            }
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
}
