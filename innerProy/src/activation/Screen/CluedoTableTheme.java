package activation.Screen;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class CluedoTableTheme {

    // Theme colors
    private static final Color BACKGROUND =
            new Color(20, 22, 27);

    private static final Color ALT_ROW =
            new Color(33, 32, 36);

    private static final Color HEADER =
            new Color(45, 35, 30);

    private static final Color GOLD =
            new Color(215, 180, 125);

    private static final Color TEXT =
            new Color(235, 220, 195);

    private static final Color SELECTED =
            new Color(95, 45, 50);

    public static JScrollPane style(JTable table) {

        // General table appearance
        table.setBackground(BACKGROUND);
        table.setForeground(TEXT);
        table.setFont(new Font("Georgia", Font.PLAIN, 15));

        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setSelectionBackground(SELECTED);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // Custom cell renderer
        table.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                        super.getTableCellRendererComponent(
                                table, value, isSelected,
                                hasFocus, row, column
                        );

                        // Alternating rows
                        if (isSelected) {
                            setBackground(SELECTED);
                        } else {
                            setBackground(
                                    row % 2 == 0
                                            ? BACKGROUND
                                            : ALT_ROW
                            );
                        }

                        // Gold labels, light values
                        if (isSelected) {
                            setForeground(Color.WHITE);
                        } else {
                            setForeground(
                                    column == 0 ? GOLD : TEXT
                            );
                        }

                        setBorder(
                                BorderFactory.createEmptyBorder(
                                        0, 12, 0, 10
                                )
                        );

                        return this;
                    }
                }
        );

        // Header customization
        JTableHeader header = table.getTableHeader();

        header.setPreferredSize(new Dimension(0, 45));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                        super.getTableCellRendererComponent(
                                table, value, isSelected,
                                hasFocus, row, column
                        );

                        setBackground(HEADER);
                        setForeground(GOLD);

                        setFont(new Font(
                                "Georgia", Font.BOLD, 16
                        ));

                        setHorizontalAlignment(
                                SwingConstants.LEFT
                        );

                        setBorder(
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(
                                                0, 0, 2, 0, GOLD
                                        ),
                                        BorderFactory.createEmptyBorder(
                                                8, 12, 8, 12
                                        )
                                )
                        );

                        return this;
                    }
                }
        );

        // Scroll pane customization
        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.getViewport().setBackground(BACKGROUND);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(110, 85, 55), 1
                )
        );

        // Match text editors to the theme
        TableCellEditor editor =
                table.getDefaultEditor(Object.class);

        if (editor instanceof DefaultCellEditor cellEditor) {
            Component component = cellEditor.getComponent();

            if (component instanceof JTextField field) {
                field.setBackground(new Color(55, 43, 39));
                field.setForeground(TEXT);
                field.setCaretColor(GOLD);
                field.setFont(new Font(
                        "Georgia", Font.PLAIN, 15
                ));
                field.setBorder(
                        BorderFactory.createEmptyBorder(
                                0, 12, 0, 10
                        )
                );
            }
        }

        return scrollPane;
    }
    public static JScrollPane styleConnection(JTable table){
        JScrollPane scrollPane = style(table);
        table.setRowHeight(50);
        TableCellRenderer panelRenderer = (t, value, isSelected, hasFocus, row, column) -> {
            Color rowColor = isSelected ? SELECTED: row % 2 == 0 ? BACKGROUND : ALT_ROW;
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(rowColor);

            panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

            JLabel label = new JLabel(value == null ? "" : value.toString());

            label.setFont(new Font("Georgia", Font.PLAIN, 15));
            label.setForeground(isSelected ? Color.WHITE : column == 0 ? GOLD : TEXT);

            if (column == 0) {
                panel.add(label, BorderLayout.CENTER);
            } else {
                JPanel fieldPanel = new JPanel(new BorderLayout());
                fieldPanel.setBackground(isSelected ? new Color(75,43,44) : new Color(55,43,39));
                fieldPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(hasFocus ? GOLD : new Color(110,85,55), 1), BorderFactory.createEmptyBorder(5,12,5,12)));
                fieldPanel.add(label, BorderLayout.CENTER);
                panel.add(fieldPanel, BorderLayout.CENTER);
            }
            return panel;
        };
        table.getColumnModel().getColumn(0).setCellRenderer(panelRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(panelRenderer);
        JTextField editorField = new JTextField();

        editorField.setBackground(new Color(55,43,39));
        editorField.setForeground(TEXT);
        editorField.setCaretColor(GOLD);

        editorField.setFont(new Font("Georgia", Font.PLAIN, 15));
        editorField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD, 1),BorderFactory.createEmptyBorder(5, 12, 5, 12)));

        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(editorField));

        return scrollPane;
    }
}
