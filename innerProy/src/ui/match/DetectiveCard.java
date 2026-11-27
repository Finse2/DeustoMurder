package ui.match;

import model.Actor;
import model.Room;
import model.Weapon;

import ui.NoirTheme;
import ui.NoirDialog;
import ui.NoirCheckBox;
import ui.NoirScrollBarUI;
import ui.NoirConfirmDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DetectiveCard extends JPanel {

    private final List<NoirCheckBox> checkBoxes = new ArrayList<>();

    private final JLabel totalCounter = new JLabel("0 marked clues");

    private final JTextField playerName = new JTextField();

    public DetectiveCard(
            List<Actor> actors,
            List<Weapon> weapons,
            List<Room> rooms
    ) {

        setLayout(new BorderLayout(0, 12));
        setBackground(NoirTheme.BACKGROUND);

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
        paper.add(Box.createVerticalStrut(16));

        // SUSPECTS

        String[] actorNames = new String[actors.size()];

        for (int i = 0; i < actors.size(); i++) {

            actorNames[i] = actors.get(i).getName();
        }

        addSection(paper, "01 / SUSPECTS", actorNames);

        // WEAPONS

        String[] weaponNames = new String[weapons.size()];

        for (int i = 0; i < weapons.size(); i++) {

            weaponNames[i] = weapons.get(i).getName();
        }

        addSection(paper, "02 / WEAPONS", weaponNames);

        // LOCATIONS

        String[] roomNames = new String[rooms.size()];

        for (int i = 0; i < rooms.size(); i++) {

            roomNames[i] = rooms.get(i).getName();
        }

        addSection(paper, "03 / LOCATIONS", roomNames);

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

        updateTotalCounter();
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

        // INVESTIGATOR ICON

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

        // PLAYER INFORMATION

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
        playerName.setCaretColor(NoirTheme.GOLD);

        playerName.setBackground(NoirTheme.SURFACE);

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

        // PRIVATE LABEL

        JLabel privacy = new JLabel("PRIVATE");

        privacy.setFont(NoirTheme.SUBTITLE_FONT);
        privacy.setForeground(NoirTheme.SECONDARY_TEXT);

        panel.add(privacy, BorderLayout.EAST);

        return panel;
    }

    // SECTION

    private void addSection(
            JPanel parent,
            String title,
            String[] entries
    ) {

        JPanel section = new JPanel(new BorderLayout(0, 6));

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

        JLabel counter = new JLabel(
                "0 / " + entries.length
        );

        counter.setFont(NoirTheme.SUBTITLE_FONT);
        counter.setForeground(NoirTheme.GOLD);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(counter, BorderLayout.EAST);

        section.add(header, BorderLayout.NORTH);

        // TWO-COLUMN GRID

        JPanel rows = new JPanel(
                new GridLayout(0, 2, 6, 6)
        );

        rows.setOpaque(false);

        List<NoirCheckBox> sectionBoxes = new ArrayList<>();

        for (String entry : entries) {

            NoirCheckBox checkBox = new NoirCheckBox(entry);

            sectionBoxes.add(checkBox);
            checkBoxes.add(checkBox);

            checkBox.addItemListener(event -> {

                updateSectionCounter(
                        sectionBoxes,
                        counter
                );

                updateTotalCounter();
            });

            rows.add(checkBox);
        }

        // COMPLETE THE LAST ROW

        if (entries.length % 2 != 0) {

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
        parent.add(Box.createVerticalStrut(14));
    }

    // SECTION COUNTER

    private void updateSectionCounter(
            List<NoirCheckBox> boxes,
            JLabel counter
    ) {

        int marked = 0;

        for (NoirCheckBox box : boxes) {

            if (box.isSelected()) {
                marked++;
            }
        }

        counter.setText(
                marked + " / " + boxes.size()
        );
    }

    // TOTAL COUNTER

    private void updateTotalCounter() {

        int marked = 0;

        for (NoirCheckBox box : checkBoxes) {

            if (box.isSelected()) {
                marked++;
            }
        }

        totalCounter.setText(
                marked + " / " + checkBoxes.size()
                        + " marked clues"
        );
    }

    // FOOTER

    private JPanel createFooter() {

        JPanel footer = new JPanel(
                new BorderLayout(0, 12)
        );

        footer.setOpaque(false);

        footer.setBorder(
                BorderFactory.createMatteBorder(
                        1, 0, 0, 0,
                        NoirTheme.BORDER
                )
        );

        JPanel controls = new JPanel(
                new BorderLayout(12, 0)
        );

        controls.setOpaque(false);

        controls.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 0, 0, 0
                )
        );

        totalCounter.setFont(NoirTheme.SUBTITLE_FONT);
        totalCounter.setForeground(NoirTheme.GOLD);

        JButton resetButton = createResetButton();

        controls.add(totalCounter, BorderLayout.WEST);
        controls.add(resetButton, BorderLayout.EAST);

        footer.add(controls, BorderLayout.NORTH);

        JLabel quote = new JLabel(
                "The truth is hidden in the details.",
                SwingConstants.CENTER
        );

        quote.setFont(
                new Font("Georgia", Font.ITALIC, 11)
        );

        quote.setForeground(NoirTheme.GOLD);

        footer.add(quote, BorderLayout.SOUTH);

        return footer;
    }

    // RESET BUTTON

    private JButton createResetButton() {

        JButton button = new JButton("Reset notes");

        button.setFont(NoirTheme.BODY_FONT);
        button.setForeground(NoirTheme.TEXT);
        button.setBackground(NoirTheme.SURFACE);

        button.setFocusPainted(false);

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                NoirTheme.BORDER
                        ),
                        BorderFactory.createEmptyBorder(
                                7, 12, 7, 12
                        )
                )
        );

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e
                    ) {

                        button.setBackground(NoirTheme.HOVER);
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e
                    ) {

                        button.setBackground(NoirTheme.SURFACE);
                    }
                }
        );

        button.addActionListener(event -> resetNotes());

        return button;
    }

    // RESET NOTES

    private void resetNotes() {

        boolean confirmed = NoirConfirmDialog.showConfirm(
                this,
                "RESET INVESTIGATION NOTES?",
                "All marked clues will be cleared.",
                "Keep notes",
                "Reset notes"
        );

        if (!confirmed) {
            return;
        }

        for (NoirCheckBox box : checkBoxes) {

            box.setSelected(false);
        }

        updateTotalCounter();
    }

    // PLAYER NAME

    public void setPlayerName(String name) {

        playerName.setText(name);
    }

    public String getPlayerName() {

        return playerName.getText();
    }

    // STANDALONE DIALOG

    public static void showCard(
            List<Actor> actors,
            List<Weapon> weapons,
            List<Room> rooms
    ) {

        NoirDialog dialog = new NoirDialog(
                null,
                "DETECTIVE NOTEBOOK",
                "Every clue brings you closer to the truth.",
                Dialog.ModalityType.MODELESS
        );

        dialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );

        DetectiveCard card = new DetectiveCard(
                actors,
                weapons,
                rooms
        );

        dialog.setBody(card);

        dialog.setSize(560, 720);

        dialog.open();
    }
}