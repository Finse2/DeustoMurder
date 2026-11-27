package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NoirDialog extends JDialog {

    private final JPanel bodyPanel;

    private Point dragOffset;

    public NoirDialog(
            Window owner,
            String title,
            String subtitle,
            Dialog.ModalityType modality
    ) {

        super(owner, title, modality);

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        // MAIN PANEL

        JPanel root = new JPanel(new BorderLayout(0, 10));

        root.setBackground(NoirTheme.BACKGROUND);

        root.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                NoirTheme.GOLD
                        ),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createEmptyBorder(
                                        4, 4, 4, 4
                                ),
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(
                                                NoirTheme.INNER_BORDER
                                        ),
                                        BorderFactory.createEmptyBorder(
                                                14, 16, 14, 16
                                        )
                                )
                        )
                )
        );

        // HEADER

        JPanel header = createHeader(title, subtitle);

        root.add(header, BorderLayout.NORTH);

        // BODY

        bodyPanel = new JPanel(new BorderLayout());

        bodyPanel.setOpaque(false);

        root.add(bodyPanel, BorderLayout.CENTER);

        setContentPane(root);

        // ESCAPE KEY

        getRootPane().getInputMap(
                JComponent.WHEN_IN_FOCUSED_WINDOW
        ).put(
                KeyStroke.getKeyStroke("ESCAPE"),
                "closeDialog"
        );

        getRootPane().getActionMap().put(
                "closeDialog",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        closeDialog();
                    }
                }
        );
    }

    // HEADER

    private JPanel createHeader(
            String title,
            String subtitle
    ) {

        JPanel header = new JPanel(
                new BorderLayout(12, 8)
        );

        header.setOpaque(false);

        JPanel titles = new JPanel();

        titles.setLayout(
                new BoxLayout(titles, BoxLayout.Y_AXIS)
        );

        titles.setOpaque(false);

        // BRAND

        JLabel brandLabel = new JLabel(
                "DEUSTOMURDER / CONFIDENTIAL"
        );

        brandLabel.setFont(NoirTheme.SUBTITLE_FONT);
        brandLabel.setForeground(NoirTheme.GOLD);

        // TITLE

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Georgia", Font.BOLD, 24)
        );

        titleLabel.setForeground(NoirTheme.TEXT);

        // SUBTITLE

        JLabel subtitleLabel = new JLabel(subtitle);

        subtitleLabel.setFont(NoirTheme.BODY_FONT);
        subtitleLabel.setForeground(
                NoirTheme.SECONDARY_TEXT
        );

        titles.add(brandLabel);
        titles.add(Box.createVerticalStrut(8));
        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(5));
        titles.add(subtitleLabel);

        header.add(titles, BorderLayout.CENTER);

        // CLOSE BUTTON

        JPanel closeContainer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );

        closeContainer.setOpaque(false);

        closeContainer.add(createCloseButton());

        header.add(
                closeContainer,
                BorderLayout.EAST
        );

        // SEPARATOR

        JPanel separator = new JPanel();

        separator.setBackground(NoirTheme.BORDER);
        separator.setPreferredSize(new Dimension(1, 1));

        header.add(separator, BorderLayout.SOUTH);

        // WINDOW DRAGGING

        installDragging(header);
        installDragging(titles);
        installDragging(brandLabel);
        installDragging(titleLabel);
        installDragging(subtitleLabel);

        return header;
    }

    // CLOSE BUTTON

    private JButton createCloseButton() {

        JButton button = new JButton() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // BACKGROUND

                if (getModel().isPressed()) {

                    g2.setColor(NoirTheme.CRIMSON);

                } else if (getModel().isRollover()) {

                    g2.setColor(NoirTheme.DARK_CRIMSON);

                } else {

                    g2.setColor(NoirTheme.SURFACE);
                }

                g2.fillRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        8, 8
                );

                // BORDER

                g2.setColor(
                        getModel().isRollover() || hasFocus()
                                ? NoirTheme.GOLD
                                : NoirTheme.BORDER
                );

                g2.drawRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        8, 8
                );

                // CLOSE ICON

                g2.setColor(NoirTheme.TEXT);

                g2.setStroke(
                        new BasicStroke(
                                1.8f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

                int x = getWidth() / 2;
                int y = getHeight() / 2;

                g2.drawLine(x - 5, y - 5, x + 5, y + 5);
                g2.drawLine(x + 5, y - 5, x - 5, y + 5);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(34, 34));

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);

        button.setToolTipText("Close");
        button.getAccessibleContext().setAccessibleName("Close");

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addActionListener(event -> closeDialog());

        return button;
    }

    // WINDOW DRAGGING

    private void installDragging(Component component) {

        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent event) {

                dragOffset = new Point(
                        event.getXOnScreen() - getX(),
                        event.getYOnScreen() - getY()
                );
            }

            @Override
            public void mouseDragged(MouseEvent event) {

                if (dragOffset == null) {
                    return;
                }

                setLocation(
                        event.getXOnScreen() - dragOffset.x,
                        event.getYOnScreen() - dragOffset.y
                );
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                dragOffset = null;
            }
        };

        component.addMouseListener(adapter);
        component.addMouseMotionListener(adapter);
    }

    // CONTENT MANAGEMENT

    public void setBody(JComponent component) {

        bodyPanel.removeAll();

        bodyPanel.add(
                component,
                BorderLayout.CENTER
        );

        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    // CLOSE DIALOG

    public void closeDialog() {

        dispatchEvent(
                new WindowEvent(
                        this,
                        WindowEvent.WINDOW_CLOSING
                )
        );
    }

    // OPEN DIALOG

    public void open() {

        setLocationRelativeTo(getOwner());
        setVisible(true);
    }
}