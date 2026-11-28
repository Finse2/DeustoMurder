
package activation.Screen;

import app.ApplicationContext;

import game.statistics.StatisticsSnapshot;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import java.awt.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Locale;

public final class ProfileStatisticsDialog {

    private ProfileStatisticsDialog() {
    }

    public static void open(

            JFrame owner,

            Runnable onChangePicture

    ) {

        ApplicationContext context =
                ApplicationContext.getInstance();

        JDialog dialog = new JDialog(

                owner,

                "Profile Statistics",

                Dialog.ModalityType.MODELESS
        );

        dialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );

        dialog.setLayout(
                new BorderLayout()
        );

        StatisticsTableModel model =
                new StatisticsTableModel();

        JTable table =
                new JTable(model);

        JScrollPane scrollPane =
                CluedoTableTheme.style(table);

        dialog.add(
                scrollPane,
                BorderLayout.CENTER
        );

        JLabel statusLabel =
                new JLabel("Loading statistics...");

        statusLabel.setForeground(
                new Color(225, 190, 140)
        );

        statusLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        8, 10, 8, 10
                )
        );

        dialog.add(
                statusLabel,
                BorderLayout.NORTH
        );

        JPanel controls =
                new JPanel(new GridLayout(1, 0, 8, 0));

        controls.setBackground(
                new Color(20, 22, 27)
        );

        JButton refreshButton =
                createButton("Refresh");

        controls.add(refreshButton);

        if (onChangePicture != null) {

            JButton changePicture =
                    createButton(
                            "Change Profile Picture"
                    );

            changePicture.addActionListener(
                    event -> onChangePicture.run()
            );

            controls.add(changePicture);
        }

        dialog.add(
                controls,
                BorderLayout.SOUTH
        );

        // LOAD DATABASE SNAPSHOT IN BACKGROUND

        Runnable refresh = () -> {

            if (!refreshButton.isEnabled()) {
                return;
            }

            refreshButton.setEnabled(false);

            statusLabel.setText(
                    "Loading statistics..."
            );

            SwingWorker<StatisticsSnapshot, Void> worker =
                    new SwingWorker<>() {

                        @Override
                        protected StatisticsSnapshot
                        doInBackground() throws Exception {

                            return context
                                    .getStatisticsService()
                                    .getStatistics(
                                            context.getLocalPlayerId()
                                    );
                        }

                        @Override
                        protected void done() {

                            if (!dialog.isDisplayable()) {
                                return;
                            }

                            try {

                                model.setStatistics(get());

                                statusLabel.setText(
                                        "Statistics up to date"
                                );

                            } catch (Exception e) {

                                statusLabel.setText(
                                        "Could not load statistics."
                                );

                                e.printStackTrace();

                            } finally {

                                refreshButton.setEnabled(true);
                            }
                        }
                    };

            worker.execute();
        };

        refreshButton.addActionListener(
                event -> refresh.run()
        );

        // Refresh automatically while the dialog is open.

        Timer refreshTimer = new Timer(

                5000,

                event -> refresh.run()
        );

        refreshTimer.setCoalesce(true);

        dialog.addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowClosed(
                            WindowEvent event
                    ) {

                        refreshTimer.stop();
                    }
                }
        );

        dialog.getContentPane().setBackground(
                new Color(20, 22, 27)
        );

        dialog.setSize(520, 460);

        dialog.setLocationRelativeTo(owner);

        dialog.setVisible(true);

        refresh.run();

        refreshTimer.start();
    }

    // BUTTON STYLING

    private static JButton createButton(
            String text
    ) {

        JButton button =
                new JButton(text);

        button.setBackground(
                new Color(75, 38, 42)
        );

        button.setForeground(
                new Color(225, 190, 140)
        );

        button.setFont(
                new Font(
                        "Georgia",
                        Font.BOLD,
                        14
                )
        );

        button.setFocusPainted(false);

        return button;
    }

    // STATISTICS TABLE MODEL

    private static final class StatisticsTableModel
            extends AbstractTableModel {

        private final String[] labels = {

                "Games Played",

                "Games Won",

                "Games Lost",

                "Win Rate",

                "Total Accusations",

                "Correct Accusations",

                "Incorrect Accusations",

                "Current Win Streak",

                "Best Win Streak",

                "Average Match Duration",

                "Total Match Duration",

                "Total Points"
        };

        private StatisticsSnapshot statistics;

        public void setStatistics(

                StatisticsSnapshot statistics

        ) {

            this.statistics = statistics;

            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {

            return labels.length;
        }

        @Override
        public int getColumnCount() {

            return 2;
        }

        @Override
        public String getColumnName(
                int column
        ) {

            return column == 0

                    ? "Statistic"

                    : "Value";
        }

        @Override
        public boolean isCellEditable(
                int row,
                int column
        ) {

            return false;
        }

        @Override
        public Object getValueAt(
                int row,
                int column
        ) {

            if (column == 0) {

                return labels[row];
            }

            if (statistics == null) {

                return "—";
            }

            return switch (row) {

                case 0 -> statistics.gamesPlayed();

                case 1 -> statistics.wins();

                case 2 -> statistics.losses();

                case 3 -> String.format(

                        Locale.ROOT,

                        "%.1f%%",

                        statistics.gamesPlayed() == 0

                                ? 0.0

                                : 100.0 * statistics.wins()
                                / statistics.gamesPlayed()
                );

                case 4 -> statistics.accusationsMade();

                case 5 -> statistics.correctAccusations();

                case 6 -> statistics.incorrectAccusations();

                case 7 -> statistics.currentWinStreak();

                case 8 -> statistics.bestWinStreak();

                case 9 -> statistics
                        .getFormattedAverageGameDuration();

                case 10 -> statistics
                        .getFormattedTotalGameDuration();

                case 11 -> statistics.totalPoints();

                default -> "";
            };
        }
    }
}
