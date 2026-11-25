package activation.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class CustomOptionPane extends JDialog {

    // Color themes

    public static final Color DEFAULT = new Color(53, 48, 63);
    public static final Color ERROR = new Color(84, 48, 48);
    public static final Color SUCCESS = new Color(41, 69, 55);
    public static final Color WARNING = new Color(101, 81, 59);
    public static final Color INFO = new Color(44, 64, 89);

    // Return values

    public static final int YES_OPTION = 0;
    public static final int OK_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int CLOSED_OPTION = -1;

    // Option types

    public static final int YES_NO_OPTION = 0;
    public static final int YES_NO_CANCEL_OPTION = 1;
    public static final int OK_CANCEL_OPTION = 2;

    // Design settings

    private static final int RADIUS = 24;
    private static final int BORDER_SIZE = 3;
    private static final int IMAGE_SIZE = 64;

    private final Color backgroundColor;
    private final Color borderColor;
    private final Color textColor;

    private final JPanel buttonPanel;
    private final JPanel contentPanel;
    private final JPanel messagePanel;

    private JTextField inputField;

    private String inputResult = null;
    private int selectedOption = CLOSED_OPTION;

    private Point mousePosition;

    // Constructors

    public CustomOptionPane(ImageIcon image, String text) {
        this(image, text, DEFAULT);
    }

    public CustomOptionPane(
            ImageIcon image,
            String text,
            Color background
    ) {
        this(null, image, text, background);
    }

    public CustomOptionPane(
            Component parent,
            ImageIcon image,
            String text,
            Color background
    ) {

        super(
                parent == null
                        ? null
                        : SwingUtilities.getWindowAncestor(parent),
                "Deusto Cluedo",
                ModalityType.APPLICATION_MODAL
        );

        this.backgroundColor =
                background == null ? DEFAULT : background;

        this.borderColor = backgroundColor.darker();

        this.textColor = getContrastColor(backgroundColor);

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0));

        // MAIN PANEL

        JPanel mainPanel = new JPanel(new BorderLayout()) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // Outer border

                g2.setColor(borderColor);

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        RADIUS,
                        RADIUS
                );

                // Inner background

                g2.setColor(backgroundColor);

                g2.fillRoundRect(
                        BORDER_SIZE,
                        BORDER_SIZE,
                        getWidth() - BORDER_SIZE * 2,
                        getHeight() - BORDER_SIZE * 2,
                        RADIUS - BORDER_SIZE * 2,
                        RADIUS - BORDER_SIZE * 2
                );

                g2.dispose();
            }
        };

        mainPanel.setOpaque(false);

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        BORDER_SIZE,
                        BORDER_SIZE,
                        BORDER_SIZE,
                        BORDER_SIZE
                )
        );

        // TITLE BAR

        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // CONTENT PANEL

        contentPanel = new JPanel(new BorderLayout(20, 10));

        contentPanel.setOpaque(false);

        contentPanel.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 15, 25)
        );

        // IMAGE

        if (image != null) {

            if (image.getIconWidth() > 0
                    && image.getIconHeight() > 0) {

                JLabel imageLabel = new JLabel(
                        scaleImage(image)
                );

                imageLabel.setVerticalAlignment(
                        SwingConstants.CENTER
                );

                contentPanel.add(
                        imageLabel,
                        BorderLayout.WEST
                );

            } else {

                System.err.println(
                        "CustomOptionPane: Invalid image."
                );
            }
        }

        // MESSAGE PANEL

        messagePanel = new JPanel(new BorderLayout(0, 15));

        messagePanel.setOpaque(false);

        JTextArea message = new JTextArea(text);

        message.setFont(new Font("Arial", Font.PLAIN, 15));
        message.setForeground(textColor);
        message.setOpaque(false);
        message.setEditable(false);
        message.setFocusable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setColumns(30);

        // Calculate the message height

        message.setSize(
                message.getPreferredSize().width,
                Short.MAX_VALUE
        );

        Dimension preferred = message.getPreferredSize();

        int height = Math.min(preferred.height + 4, 250);

        message.setPreferredSize(
                new Dimension(preferred.width, height)
        );

        JScrollPane scrollPane = new JScrollPane(message);

        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        messagePanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(messagePanel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // BUTTON PANEL

        buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12)
        );

        buttonPanel.setOpaque(false);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        configureMessageDialog();

        // ESC closes the dialog

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        pack();

        setLocationRelativeTo(parent);
    }

    // TITLE BAR

    private JPanel createTitleBar() {

        JPanel titleBar = new JPanel(new BorderLayout()) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(borderColor);

                g2.fillRoundRect(
                        0, 0,
                        getWidth(), getHeight(),
                        RADIUS - BORDER_SIZE * 2,
                        RADIUS - BORDER_SIZE * 2
                );

                g2.fillRect(
                        0,
                        getHeight() / 2,
                        getWidth(),
                        getHeight() / 2
                );

                g2.dispose();
            }
        };

        titleBar.setOpaque(false);
        titleBar.setPreferredSize(new Dimension(0, 38));

        JLabel title = new JLabel("Deusto Cluedo");

        title.setForeground(getContrastColor(borderColor));
        title.setFont(new Font("Arial", Font.BOLD, 14));

        title.setBorder(
                BorderFactory.createEmptyBorder(0, 12, 0, 0)
        );

        // Custom painted close button

        JButton closeButton = new JButton() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                if (getModel().isRollover()) {

                    g2.setColor(new Color(180, 50, 50));

                    g2.fillRoundRect(
                            4, 4,
                            getWidth() - 8,
                            getHeight() - 8,
                            8, 8
                    );
                }

                g2.setColor(getContrastColor(borderColor));

                g2.setStroke(new BasicStroke(
                        2,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                int x = getWidth() / 2;
                int y = getHeight() / 2;

                g2.drawLine(x - 5, y - 5, x + 5, y + 5);
                g2.drawLine(x - 5, y + 5, x + 5, y - 5);

                g2.dispose();
            }
        };

        closeButton.setPreferredSize(new Dimension(40, 38));
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setRolloverEnabled(true);
        closeButton.setToolTipText("Close");

        closeButton.getAccessibleContext()
                .setAccessibleName("Close");

        closeButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        closeButton.addActionListener(e -> dispose());

        titleBar.add(title, BorderLayout.CENTER);
        titleBar.add(closeButton, BorderLayout.EAST);

        // Draggable title bar

        MouseAdapter dragListener = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                mousePosition = e.getLocationOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {

                Point current = e.getLocationOnScreen();

                setLocation(
                        getX() + current.x - mousePosition.x,
                        getY() + current.y - mousePosition.y
                );

                mousePosition = current;
            }
        };

        titleBar.addMouseListener(dragListener);
        titleBar.addMouseMotionListener(dragListener);

        title.addMouseListener(dragListener);
        title.addMouseMotionListener(dragListener);

        return titleBar;
    }

    // CUSTOM BUTTON

    private JButton createButton(
            String text,
            Runnable action
    ) {

        JButton button = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color color = borderColor;

                if (getModel().isPressed()) {

                    color = borderColor.darker();

                } else if (getModel().isRollover()) {

                    color = borderColor.brighter();
                }

                g2.setColor(color);

                g2.fillRoundRect(
                        0, 0,
                        getWidth(),
                        getHeight(),
                        12, 12
                );

                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setForeground(getContrastColor(borderColor));
        button.setFont(new Font("Arial", Font.BOLD, 13));

        button.setPreferredSize(new Dimension(90, 34));

        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setOpaque(false);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addActionListener(e -> action.run());

        return button;
    }

    // MESSAGE DIALOG

    private void configureMessageDialog() {

        buttonPanel.removeAll();

        JButton ok = createButton("OK", () -> {

            selectedOption = OK_OPTION;

            dispose();
        });

        buttonPanel.add(ok);

        getRootPane().setDefaultButton(ok);
    }

    // INPUT DIALOG

    private void configureInputDialog() {

        buttonPanel.removeAll();

        inputField = new JTextField();

        inputField.setFont(
                new Font("Arial", Font.PLAIN, 15)
        );

        inputField.setBackground(borderColor);
        inputField.setForeground(getContrastColor(borderColor));
        inputField.setCaretColor(getContrastColor(borderColor));

        inputField.setPreferredSize(new Dimension(200, 35));

        inputField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                borderColor.darker(), 2
                        ),
                        BorderFactory.createEmptyBorder(
                                5, 10, 5, 10
                        )
                )
        );

        messagePanel.add(inputField, BorderLayout.SOUTH);

        JButton cancel = createButton("Cancel", () -> {

            inputResult = null;
            selectedOption = CANCEL_OPTION;

            dispose();
        });

        JButton ok = createButton("OK", () -> {

            inputResult = inputField.getText().trim();
            selectedOption = OK_OPTION;

            dispose();
        });

        buttonPanel.add(cancel);
        buttonPanel.add(ok);

        getRootPane().setDefaultButton(ok);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {

                inputField.requestFocusInWindow();
            }
        });
    }

    // CONFIRMATION DIALOG

    private void configureConfirmDialog(int optionType) {

        buttonPanel.removeAll();

        JButton yes = createButton("Yes", () -> {

            selectedOption = YES_OPTION;

            dispose();
        });

        JButton no = createButton("No", () -> {

            selectedOption = NO_OPTION;

            dispose();
        });

        JButton cancel = createButton("Cancel", () -> {

            selectedOption = CANCEL_OPTION;

            dispose();
        });

        JButton ok = createButton("OK", () -> {

            selectedOption = OK_OPTION;

            dispose();
        });

        switch (optionType) {

            case YES_NO_OPTION -> {

                buttonPanel.add(no);
                buttonPanel.add(yes);

                getRootPane().setDefaultButton(no);
            }

            case YES_NO_CANCEL_OPTION -> {

                buttonPanel.add(cancel);
                buttonPanel.add(no);
                buttonPanel.add(yes);

                getRootPane().setDefaultButton(cancel);
            }

            case OK_CANCEL_OPTION -> {

                buttonPanel.add(cancel);
                buttonPanel.add(ok);

                getRootPane().setDefaultButton(cancel);
            }

            default -> throw new IllegalArgumentException(
                    "Invalid option type: " + optionType
            );
        }
    }

    // IMAGE SCALING

    private ImageIcon scaleImage(ImageIcon image) {

        int width = image.getIconWidth();
        int height = image.getIconHeight();

        double scale = Math.min(
                (double) IMAGE_SIZE / width,
                (double) IMAGE_SIZE / height
        );

        int newWidth = Math.max(1, (int) (width * scale));
        int newHeight = Math.max(1, (int) (height * scale));

        return new ImageIcon(
                image.getImage().getScaledInstance(
                        newWidth,
                        newHeight,
                        Image.SCALE_SMOOTH
                )
        );
    }

    // AUTOMATIC TEXT COLOR

    private static Color getContrastColor(Color color) {

        double brightness =
                color.getRed() * 0.299
                        + color.getGreen() * 0.587
                        + color.getBlue() * 0.114;

        return brightness > 150
                ? Color.BLACK
                : Color.WHITE;
    }

    // SHOW DIALOG

    public void showDialog() {

        pack();

        setLocationRelativeTo(getOwner());

        setVisible(true);
    }

    // STATIC MESSAGE METHODS

    public static void showMessageDialog(
            ImageIcon image,
            String text
    ) {

        showMessageDialog(null, image, text, DEFAULT);
    }

    public static void showMessageDialog(
            ImageIcon image,
            String text,
            Color background
    ) {

        showMessageDialog(null, image, text, background);
    }

    public static void showMessageDialog(
            Component parent,
            ImageIcon image,
            String text,
            Color background
    ) {

        CustomOptionPane dialog = new CustomOptionPane(
                parent, image, text, background
        );

        dialog.showDialog();
    }

    // STATIC INPUT METHODS

    public static String showInputDialog(
            ImageIcon image,
            String text
    ) {

        return showInputDialog(null, image, text, DEFAULT);
    }

    public static String showInputDialog(
            ImageIcon image,
            String text,
            Color background
    ) {

        return showInputDialog(null, image, text, background);
    }

    public static String showInputDialog(
            Component parent,
            ImageIcon image,
            String text,
            Color background
    ) {

        CustomOptionPane dialog = new CustomOptionPane(
                parent, image, text, background
        );

        dialog.configureInputDialog();

        dialog.showDialog();

        return dialog.inputResult;
    }

    // STATIC CONFIRMATION METHODS

    public static int showConfirmDialog(
            ImageIcon image,
            String text,
            int optionType
    ) {

        return showConfirmDialog(
                null, image, text, DEFAULT, optionType
        );
    }

    public static int showConfirmDialog(
            ImageIcon image,
            String text,
            Color background,
            int optionType
    ) {

        return showConfirmDialog(
                null, image, text, background, optionType
        );
    }

    public static int showConfirmDialog(
            Component parent,
            ImageIcon image,
            String text,
            Color background,
            int optionType
    ) {

        CustomOptionPane dialog = new CustomOptionPane(
                parent, image, text, background
        );

        dialog.configureConfirmDialog(optionType);

        dialog.showDialog();

        return dialog.selectedOption;
    }

    // TEST

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            ImageIcon icon = new ImageIcon(
                    "innerProy/ressources/settings.png"
            );

            showMessageDialog(
                    icon,
                    "Welcome to Deusto Cluedo!",
                    SUCCESS
            );

            String username = showInputDialog(
                    icon,
                    "What is your username?",
                    DEFAULT
            );

            System.out.println("Username: " + username);

            int answer = showConfirmDialog(
                    icon,
                    "Are you sure you want to exit the game?",
                    WARNING,
                    YES_NO_CANCEL_OPTION
            );

            System.out.println("Selected option: " + answer);

        });
    }
}