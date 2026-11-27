package ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Objects;

public final class NoirEmojiPicker extends JPopupMenu {

    private static final String[] FACES = {
            "😀", "😂", "🤣", "😊", "😉",
            "😎", "🤔", "🤨", "😏", "😮",
            "😱", "😡", "😭", "🙄", "🤫",
            "🤯", "😴", "😈", "🥸", "🧐"
    };

    private static final String[] INVESTIGATION = {
            "🕵️", "🔍", "🔎", "🗝️", "🔐",
            "📜", "📁", "📌", "👣", "👀",
            "💀", "🕯️", "⏳", "⚠️", "🎲",
            "🧩", "🏛️", "🔦", "❓", "❗"
    };

    private static final String[] REACTIONS = {
            "👍", "👎", "👏", "🙌", "🤝",
            "💪", "🔥", "❤️", "💯", "✅",
            "❌", "🎉", "🏆", "👋", "🤞",
            "✌️", "👊", "💡", "⭐", "💬"
    };

    private final JTextComponent target;

    private final JPanel emojiGrid = new JPanel(
            new GridLayout(0, 5, 4, 4)
    );

    public NoirEmojiPicker(JTextComponent target) {

        this.target = Objects.requireNonNull(target);

        setBackground(NoirTheme.BACKGROUND);

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                NoirTheme.GOLD
                        ),
                        BorderFactory.createEmptyBorder(
                                10, 10, 10, 10
                        )
                )
        );

        JPanel root = new JPanel(
                new BorderLayout(0, 10)
        );

        root.setOpaque(false);

        // HEADER

        JLabel title = new JLabel("SELECT EMOJI");

        title.setFont(NoirTheme.SUBTITLE_FONT);
        title.setForeground(NoirTheme.GOLD);

        root.add(title, BorderLayout.NORTH);

        // CATEGORIES

        JPanel categories = new JPanel(
                new GridLayout(1, 3, 4, 0)
        );

        categories.setOpaque(false);

        categories.add(
                createCategoryButton("Faces", FACES)
        );

        categories.add(
                createCategoryButton(
                        "Investigation",
                        INVESTIGATION
                )
        );

        categories.add(
                createCategoryButton(
                        "Reactions",
                        REACTIONS
                )
        );

        // EMOJI GRID

        emojiGrid.setOpaque(false);

        JPanel content = new JPanel(
                new BorderLayout(0, 10)
        );

        content.setOpaque(false);

        content.add(categories, BorderLayout.NORTH);
        content.add(emojiGrid, BorderLayout.CENTER);

        root.add(content, BorderLayout.CENTER);

        add(root);

        showCategory(FACES);
    }

    private JButton createCategoryButton(
            String title,
            String[] emojis
    ) {

        JButton button = new JButton(title);

        button.setFont(NoirTheme.BODY_FONT);
        button.setForeground(NoirTheme.TEXT);
        button.setBackground(NoirTheme.SURFACE);

        button.setFocusPainted(false);

        button.setBorder(
                BorderFactory.createLineBorder(
                        NoirTheme.INNER_BORDER
                )
        );

        button.setPreferredSize(
                new Dimension(82, 28)
        );

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addActionListener(
                event -> showCategory(emojis)
        );

        return button;
    }

    private void showCategory(String[] emojis) {

        emojiGrid.removeAll();

        for (String emoji : emojis) {

            emojiGrid.add(createEmojiButton(emoji));
        }

        emojiGrid.revalidate();
        emojiGrid.repaint();
    }

    private JButton createEmojiButton(String emoji) {

        JButton button = new JButton();

        ImageIcon icon = NoirEmojiManager.getIcon(
                emoji,
                30
        );

        if (icon != null) {

            button.setIcon(icon);

        } else {

            button.setText(emoji);

            button.setFont(
                    new Font("Segoe UI Emoji", Font.PLAIN, 22)
            );
        }

        button.setToolTipText(emoji);

        button.getAccessibleContext().setAccessibleName(emoji);

        button.setBackground(NoirTheme.SURFACE);
        button.setForeground(NoirTheme.TEXT);

        button.setPreferredSize(
                new Dimension(46, 46)
        );

        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setBorder(
                BorderFactory.createLineBorder(
                        NoirTheme.INNER_BORDER
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

                        button.setBorder(
                                BorderFactory.createLineBorder(
                                        NoirTheme.GOLD
                                )
                        );
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e
                    ) {

                        button.setBackground(NoirTheme.SURFACE);

                        button.setBorder(
                                BorderFactory.createLineBorder(
                                        NoirTheme.INNER_BORDER
                                )
                        );
                    }
                }
        );

        button.addActionListener(event -> {

            target.replaceSelection(emoji);

            setVisible(false);

            target.requestFocusInWindow();
        });

        return button;
    }
}