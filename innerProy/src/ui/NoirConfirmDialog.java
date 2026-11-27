package ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;

public final class NoirConfirmDialog extends JDialog {

    private boolean confirmed = false;

    private NoirConfirmDialog(
            Window owner,
            String title,
            String message,
            String cancelText,
            String confirmText
    ) {

        super(owner, Dialog.ModalityType.APPLICATION_MODAL);

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // MAIN PANEL

        JPanel root = new JPanel(new BorderLayout(0, 20));

        root.setBackground(NoirTheme.BACKGROUND);

        root.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                NoirTheme.GOLD
                        ),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createEmptyBorder(4, 4, 4, 4),
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(
                                                NoirTheme.INNER_BORDER
                                        ),
                                        BorderFactory.createEmptyBorder(
                                                24, 28, 24, 28
                                        )
                                )
                        )
                )
        );

        // BRAND HEADER

        JLabel brand = new JLabel(
                "D E U S T O M U R D E R",
                SwingConstants.CENTER
        );

        brand.setFont(NoirTheme.SUBTITLE_FONT);
        brand.setForeground(NoirTheme.GOLD);

        root.add(brand, BorderLayout.NORTH);

        // CENTRAL CONTENT

        JPanel content = new JPanel();

        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.setOpaque(false);

        // ICON

        JComponent icon = createWarningIcon();

        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(icon);
        content.add(Box.createVerticalStrut(18));

        // TITLE

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Georgia", Font.BOLD, 21)
        );

        titleLabel.setForeground(NoirTheme.TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(12));

        // MESSAGE

        JTextArea messageArea = new JTextArea(message);

        messageArea.setFont(new Font("Arial", Font.PLAIN, 13));
        messageArea.setForeground(NoirTheme.SECONDARY_TEXT);
        messageArea.setOpaque(false);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setColumns(36);
        messageArea.setRows(
                Math.max(2, message.split("\n", -1).length)
        );

        messageArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(messageArea);
        content.add(Box.createVerticalStrut(12));

        // DECORATIVE SEPARATOR

        JPanel separator = new JPanel();

        separator.setBackground(NoirTheme.BORDER);
        separator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1)
        );

        separator.setPreferredSize(new Dimension(1, 1));

        content.add(separator);

        root.add(content, BorderLayout.CENTER);

        // BUTTONS

        JButton cancelButton = createButton(
                cancelText,
                false
        );

        JButton confirmButton = createButton(
                confirmText,
                true
        );

        cancelButton.addActionListener(event -> {

            confirmed = false;
            dispose();
        });

        confirmButton.addActionListener(event -> {

            confirmed = true;
            dispose();
        });

        JPanel buttons = new JPanel(
                new GridLayout(1, 2, 12, 0)
        );

        buttons.setOpaque(false);

        buttons.add(cancelButton);
        buttons.add(confirmButton);

        // FOOTER

        JLabel footer = new JLabel(
                "Everyone is a suspect.",
                SwingConstants.CENTER
        );

        footer.setFont(
                new Font("Georgia", Font.ITALIC, 11)
        );

        footer.setForeground(NoirTheme.GOLD);

        JPanel bottom = new JPanel(
                new BorderLayout(0, 18)
        );

        bottom.setOpaque(false);

        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(footer, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);

        // ESCAPE KEY

        getRootPane().getInputMap(
                JComponent.WHEN_IN_FOCUSED_WINDOW
        ).put(
                KeyStroke.getKeyStroke("ESCAPE"),
                "cancel"
        );

        getRootPane().getActionMap().put(
                "cancel",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        confirmed = false;
                        dispose();
                    }
                }
        );

        // DEFAULT BUTTON: CANCEL

        getRootPane().setDefaultButton(cancelButton);

        // FINAL CONFIGURATION

        setContentPane(root);

        pack();

        setSize(
                Math.max(480, getWidth()),
                Math.max(310, getHeight())
        );

        setLocationRelativeTo(owner);
    }

    // CUSTOM BUTTON

    private JButton createButton(
            String text,
            boolean danger
    ) {

        JButton button = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color background = danger
                        ? NoirTheme.DARK_CRIMSON
                        : NoirTheme.SURFACE;

                if (getModel().isPressed()) {

                    background = background.darker();

                } else if (getModel().isRollover()) {

                    background = danger
                            ? NoirTheme.CRIMSON
                            : NoirTheme.HOVER;
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

                Color borderColor = danger
                        ? NoirTheme.CRIMSON
                        : NoirTheme.BORDER;

                if (hasFocus()) {
                    borderColor = NoirTheme.GOLD;
                }

                g2.setColor(borderColor);

                g2.drawRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        8, 8
                );

                g2.dispose();

                // BUTTON TEXT

                super.paintComponent(g);
            }
        };

        button.setFont(
                new Font("Georgia", Font.BOLD, 13)
        );

        button.setForeground(NoirTheme.TEXT);

        button.setPreferredSize(new Dimension(180, 44));

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        Border padding = BorderFactory.createEmptyBorder(
                8, 12, 8, 12
        );

        button.setBorder(padding);

        return button;
    }

    // WARNING ICON

    private JComponent createWarningIcon() {

        JComponent icon = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                int x = (getWidth() - 58) / 2;
                int y = (getHeight() - 58) / 2;

                g2.translate(x, y);

                // CIRCULAR BACKGROUND

                g2.setColor(NoirTheme.DARK_CRIMSON);
                g2.fillOval(1, 1, 56, 56);

                g2.setColor(NoirTheme.CRIMSON);
                g2.drawOval(1, 1, 56, 56);

                // WARNING TRIANGLE

                g2.setColor(NoirTheme.GOLD);

                g2.setStroke(new BasicStroke(
                        2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                Polygon triangle = new Polygon();

                triangle.addPoint(29, 13);
                triangle.addPoint(45, 43);
                triangle.addPoint(13, 43);

                g2.drawPolygon(triangle);

                g2.drawLine(29, 23, 29, 32);
                g2.fillOval(27, 35, 4, 4);

                g2.dispose();
            }
        };

        Dimension size = new Dimension(60, 60);

        icon.setPreferredSize(size);
        icon.setMinimumSize(size);
        icon.setMaximumSize(size);

        return icon;
    }

    // PUBLIC API

    public static boolean showConfirm(
            Component parent,
            String title,
            String message,
            String cancelText,
            String confirmText
    ) {

        Window owner = parent instanceof Window
                ? (Window) parent
                : parent == null
                ? null
                : SwingUtilities.getWindowAncestor(parent);

        NoirConfirmDialog dialog = new NoirConfirmDialog(
                owner,
                title,
                message,
                cancelText,
                confirmText
        );

        dialog.setVisible(true);

        return dialog.confirmed;
    }
}