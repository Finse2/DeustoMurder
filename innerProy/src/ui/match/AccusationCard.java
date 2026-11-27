package ui.match;

import model.Actor;
import model.Room;
import model.Weapon;

import ui.NoirTheme;
import ui.NoirDialog;
import ui.NoirRadioButton;
import ui.NoirScrollBarUI;
import ui.NoirConfirmDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccusationCard extends JPanel {

    private Actor selectedActor;
    private Weapon selectedWeapon;
    private Room selectedRoom;

    private final JLabel suspectValue = new JLabel("Not selected");
    private final JLabel weaponValue = new JLabel("Not selected");
    private final JLabel roomValue = new JLabel("Not selected");

    private final JLabel progressLabel = new JLabel("0 / 3 selections");

    private final JProgressBar progressBar = new JProgressBar(0, 3);

    private final JTextField playerName = new JTextField();

    private final JButton confirmButton;

    private final Consumer<AccusationSelection> onConfirm;

    public AccusationCard(
            List<Actor> actors,
            List<Weapon> weapons,
            List<Room> rooms,
            Consumer<AccusationSelection> onConfirm
    ) {

        this.onConfirm = onConfirm;

        setLayout(new BorderLayout(0, 12));
        setBackground(NoirTheme.BACKGROUND);

        confirmButton = createConfirmButton();

        // MAIN CONTENT

        JPanel paper = new JPanel();

        paper.setLayout(
                new BoxLayout(paper, BoxLayout.Y_AXIS)
        );

        paper.setBackground(NoirTheme.BACKGROUND);

        paper.setBorder(
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        );

        // INVESTIGATOR

        paper.add(createPlayerPanel());
        paper.add(Box.createVerticalStrut(14));

        // SUSPECTS

        addSection(
                paper,
                "01 / SELECT SUSPECT",
                actors,
                Actor::getName,
                actor -> selectedActor = actor
        );

        // WEAPONS

        addSection(
                paper,
                "02 / SELECT WEAPON",
                weapons,
                Weapon::getName,
                weapon -> selectedWeapon = weapon
        );

        // LOCATIONS

        addSection(
                paper,
                "03 / SELECT LOCATION",
                rooms,
                Room::getName,
                room -> selectedRoom = room
        );

        // SCROLL

        JScrollPane scrollPane = new JScrollPane(
                paper,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scrollPane.setViewportBorder(null);

        scrollPane.setBackground(NoirTheme.BACKGROUND);

        scrollPane.getViewport().setBackground(
                NoirTheme.BACKGROUND
        );

        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();

        scrollBar.setUI(new NoirScrollBarUI());

        scrollBar.setPreferredSize(new Dimension(9, 9));
        scrollBar.setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // FOOTER

        add(createFooter(), BorderLayout.SOUTH);

        updateSelection();
    }

    // INVESTIGATOR PANEL

    private JPanel createPlayerPanel() {

        JPanel panel = new JPanel(
                new BorderLayout(12, 0)
        );

        panel.setBackground(NoirTheme.SURFACE);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 62)
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                NoirTheme.INNER_BORDER
                        ),
                        BorderFactory.createEmptyBorder(
                                10, 12, 10, 12
                        )
                )
        );

        JLabel icon = new JLabel(
                "\u25C9",
                SwingConstants.CENTER
        );

        icon.setFont(
                new Font("Serif", Font.PLAIN, 28)
        );

        icon.setForeground(NoirTheme.GOLD);

        icon.setPreferredSize(new Dimension(35, 40));

        panel.add(icon, BorderLayout.WEST);

        JPanel information = new JPanel();

        information.setLayout(
                new BoxLayout(information, BoxLayout.Y_AXIS)
        );

        information.setOpaque(false);

        JLabel label = new JLabel("INVESTIGATOR");

        label.setFont(NoirTheme.SUBTITLE_FONT);
        label.setForeground(NoirTheme.GOLD);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        playerName.setFont(
                new Font("Georgia", Font.BOLD, 14)
        );

        playerName.setForeground(NoirTheme.TEXT);
        playerName.setBackground(NoirTheme.SURFACE);
        playerName.setCaretColor(NoirTheme.GOLD);

        playerName.setSelectionColor(
                NoirTheme.DARK_CRIMSON
        );

        playerName.setBorder(
                BorderFactory.createEmptyBorder(4, 0, 0, 0)
        );

        playerName.setAlignmentX(Component.LEFT_ALIGNMENT);

        playerName.setToolTipText("Enter investigator name");

        information.add(label);
        information.add(playerName);

        panel.add(information, BorderLayout.CENTER);

        JLabel classified = new JLabel("CLASSIFIED");

        classified.setFont(NoirTheme.SUBTITLE_FONT);
        classified.setForeground(NoirTheme.GOLD);

        panel.add(classified, BorderLayout.EAST);

        return panel;
    }

    // GENERIC SELECTION SECTION

    private <T> void addSection(
            JPanel parent,
            String title,
            List<T> entries,
            Function<T, String> getName,
            Consumer<T> onSelection
    ) {

        JPanel section = new JPanel(
                new BorderLayout(0, 6)
        );

        section.setOpaque(false);

        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // SECTION HEADER

        JPanel header = new JPanel(
                new BorderLayout()
        );

        header.setBackground(NoirTheme.DARK_CRIMSON);

        header.setPreferredSize(new Dimension(0, 32));

        header.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                NoirTheme.INNER_BORDER
                        ),
                        BorderFactory.createEmptyBorder(
                                6, 10, 6, 10
                        )
                )
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        titleLabel.setForeground(NoirTheme.TEXT);

        JLabel status = new JLabel("PENDING");

        status.setFont(NoirTheme.SUBTITLE_FONT);

        status.setForeground(NoirTheme.SECONDARY_TEXT);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);

        section.add(header, BorderLayout.NORTH);

        // OPTIONS GRID

        JPanel rows = new JPanel(
                new GridLayout(0, 2, 6, 6)
        );

        rows.setOpaque(false);

        ButtonGroup group = new ButtonGroup();

        for (T entry : entries) {

            NoirRadioButton option =
                    new NoirRadioButton(
                            getName.apply(entry)
                    );

            group.add(option);

            option.setToolTipText(getName.apply(entry));

            option.addItemListener(event -> {

                if (option.isSelected()) {

                    onSelection.accept(entry);

                    status.setText("SELECTED");

                    status.setForeground(
                            NoirTheme.SUCCESS
                    );

                    updateSelection();
                }
            });

            rows.add(option);
        }

        // COMPLETE THE LAST ROW

        if (entries.size() % 2 != 0) {

            JPanel empty = new JPanel();

            empty.setOpaque(false);

            rows.add(empty);
        }

        section.add(rows, BorderLayout.CENTER);

        section.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        section.getPreferredSize().height
                )
        );

        parent.add(section);
        parent.add(Box.createVerticalStrut(12));
    }

    // ACCUSATION SUMMARY

    private JPanel createSummaryPanel() {

        JPanel summary = new JPanel(
                new BorderLayout(0, 12)
        );

        summary.setBackground(NoirTheme.PANEL);

        summary.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                NoirTheme.BORDER
                        ),
                        BorderFactory.createEmptyBorder(
                                12, 12, 12, 12
                        )
                )
        );

        JLabel title = new JLabel("ACCUSATION SUMMARY");

        title.setFont(NoirTheme.SUBTITLE_FONT);
        title.setForeground(NoirTheme.GOLD);

        summary.add(title, BorderLayout.NORTH);

        JPanel values = new JPanel(
                new GridLayout(1, 3, 10, 0)
        );

        values.setOpaque(false);

        values.add(
                createSummaryItem("SUSPECT", suspectValue)
        );

        values.add(
                createSummaryItem("WEAPON", weaponValue)
        );

        values.add(
                createSummaryItem("LOCATION", roomValue)
        );

        summary.add(values, BorderLayout.CENTER);

        // PROGRESS

        JPanel progressPanel = new JPanel();

        progressPanel.setLayout(
                new BoxLayout(progressPanel, BoxLayout.Y_AXIS)
        );

        progressPanel.setOpaque(false);

        progressLabel.setFont(NoirTheme.SUBTITLE_FONT);
        progressLabel.setForeground(NoirTheme.GOLD);

        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressBar.setForeground(NoirTheme.GOLD);
        progressBar.setBackground(NoirTheme.SURFACE);

        progressBar.setBorderPainted(false);

        progressBar.setPreferredSize(
                new Dimension(0, 5)
        );

        progressBar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 5)
        );

        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressPanel.add(progressLabel);
        progressPanel.add(Box.createVerticalStrut(6));
        progressPanel.add(progressBar);

        summary.add(progressPanel, BorderLayout.SOUTH);

        return summary;
    }

    // SUMMARY ITEM

    private JPanel createSummaryItem(
            String title,
            JLabel value
    ) {

        JPanel item = new JPanel();

        item.setLayout(
                new BoxLayout(item, BoxLayout.Y_AXIS)
        );

        item.setBackground(NoirTheme.SURFACE);

        item.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                NoirTheme.INNER_BORDER
                        ),
                        BorderFactory.createEmptyBorder(
                                8, 6, 8, 6
                        )
                )
        );

        JLabel label = new JLabel(title);

        label.setFont(NoirTheme.SUBTITLE_FONT);
        label.setForeground(NoirTheme.GOLD);

        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        value.setFont(
                new Font("Georgia", Font.BOLD, 13)
        );

        value.setForeground(NoirTheme.SECONDARY_TEXT);

        value.setAlignmentX(Component.CENTER_ALIGNMENT);

        item.add(label);
        item.add(Box.createVerticalStrut(7));
        item.add(value);

        return item;
    }

    // UPDATE SELECTION

    private void updateSelection() {

        updateSummaryValue(
                suspectValue,
                selectedActor == null
                        ? null
                        : selectedActor.getName()
        );

        updateSummaryValue(
                weaponValue,
                selectedWeapon == null
                        ? null
                        : selectedWeapon.getName()
        );

        updateSummaryValue(
                roomValue,
                selectedRoom == null
                        ? null
                        : selectedRoom.getName()
        );

        int completed = 0;

        if (selectedActor != null) {
            completed++;
        }

        if (selectedWeapon != null) {
            completed++;
        }

        if (selectedRoom != null) {
            completed++;
        }

        progressLabel.setText(
                completed + " / 3 selections completed"
        );

        progressBar.setValue(completed);

        confirmButton.setEnabled(completed == 3);

        confirmButton.repaint();
    }

    private void updateSummaryValue(
            JLabel label,
            String value
    ) {

        if (value == null) {

            label.setText("Not selected");

            label.setForeground(
                    NoirTheme.SECONDARY_TEXT
            );

            label.setToolTipText(null);

        } else {

            label.setText(value);

            label.setForeground(NoirTheme.TEXT);

            label.setToolTipText(value);
        }
    }

    // FOOTER

    private JPanel createFooter() {

        JPanel footer = new JPanel();

        footer.setLayout(
                new BoxLayout(footer, BoxLayout.Y_AXIS)
        );

        footer.setOpaque(false);

        JPanel summary = createSummaryPanel();

        summary.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.add(summary);

        footer.add(Box.createVerticalStrut(12));

        // WARNING

        JLabel warning = new JLabel(
                "Make sure your evidence supports this accusation."
        );

        warning.setFont(
                new Font("Arial", Font.PLAIN, 11)
        );

        warning.setForeground(
                new Color(219, 135, 145)
        );

        warning.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        footer.add(warning);

        footer.add(Box.createVerticalStrut(10));

        // CONFIRM BUTTON

        confirmButton.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        footer.add(confirmButton);

        footer.add(Box.createVerticalStrut(12));

        // QUOTE

        JLabel quote = new JLabel(
                "One suspect. One weapon. One location."
        );

        quote.setFont(
                new Font("Georgia", Font.ITALIC, 11)
        );

        quote.setForeground(NoirTheme.GOLD);

        quote.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        footer.add(quote);

        return footer;
    }

    // CUSTOM CONFIRM BUTTON

    private JButton createConfirmButton() {

        JButton button = new JButton("SUBMIT ACCUSATION") {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color background = NoirTheme.DARK_CRIMSON;
                Color border = NoirTheme.CRIMSON;
                Color foreground = NoirTheme.TEXT;

                if (!isEnabled()) {

                    background = NoirTheme.PANEL;
                    border = NoirTheme.INNER_BORDER;
                    foreground = NoirTheme.SECONDARY_TEXT;

                } else if (getModel().isPressed()) {

                    background = NoirTheme.DARK_CRIMSON.darker();

                } else if (getModel().isRollover()) {

                    background = NoirTheme.CRIMSON;
                    border = NoirTheme.GOLD;
                }

                if (isEnabled() && hasFocus()) {
                    border = NoirTheme.GOLD;
                }

                // BACKGROUND

                g2.setColor(background);

                g2.fillRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        8, 8
                );

                // BORDER

                g2.setColor(border);

                g2.drawRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        8, 8
                );

                // TEXT

                g2.setColor(foreground);

                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();

                int x = (getWidth()
                        - fm.stringWidth(getText())) / 2;

                int y = (getHeight()
                        - fm.getHeight()) / 2
                        + fm.getAscent();

                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        button.setFont(
                new Font("Georgia", Font.BOLD, 15)
        );

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);

        button.setPreferredSize(new Dimension(300, 46));

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 46)
        );

        button.setEnabled(false);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addActionListener(
                event -> submitAccusation()
        );

        return button;
    }

    // FINAL ACCUSATION

    private void submitAccusation() {

        if (selectedActor == null
                || selectedWeapon == null
                || selectedRoom == null) {

            return;
        }

        AccusationSelection selection =
                new AccusationSelection(
                        selectedActor,
                        selectedWeapon,
                        selectedRoom
                );

        String message =
                "Suspect: " + selection.actor().getName()
                        + "\nWeapon: " + selection.weapon().getName()
                        + "\nLocation: " + selection.room().getName()
                        + "\n\nAre you sure about your accusation?";

        boolean confirmed = NoirConfirmDialog.showConfirm(
                this,
                "CONFIRM FINAL ACCUSATION?",
                message,
                "Review evidence",
                "Confirm accusation"
        );

        if (!confirmed) {
            return;
        }

        if (onConfirm != null) {

            onConfirm.accept(selection);
        }

        Window window =
                SwingUtilities.getWindowAncestor(this);

        if (window != null) {

            window.dispose();
        }
    }

    // PLAYER NAME

    public void setPlayerName(String name) {

        playerName.setText(name);
    }

    public String getPlayerName() {

        return playerName.getText();
    }

    // ACCUSATION RESULT

    public record AccusationSelection(
            Actor actor,
            Weapon weapon,
            Room room
    ) {}

    // STANDALONE DIALOG

    public static void showCard(
            List<Actor> actors,
            List<Weapon> weapons,
            List<Room> rooms,
            Consumer<AccusationSelection> onConfirm
    ) {

        NoirDialog dialog = new NoirDialog(
                null,
                "FINAL ACCUSATION",
                "Present your theory. Reveal the truth.",
                Dialog.ModalityType.MODELESS
        );

        dialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );

        AccusationCard card = new AccusationCard(
                actors,
                weapons,
                rooms,
                onConfirm
        );

        dialog.setBody(card);

        dialog.setSize(560, 800);

        dialog.open();
    }
}