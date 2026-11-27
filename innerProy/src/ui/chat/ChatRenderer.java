package ui.chat;

import Networking.common.ChatChannel;

import ui.NoirScrollBarUI;
import ui.NoirTheme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import Networking.common.ChatChannel;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.Timer;

/** Vista Swing de un Chat. No guarda mensajes ni gestiona sockets. */
public final class ChatRenderer extends JPanel {

    private static final Color BACKGROUND = NoirTheme.BACKGROUND;
    private static final Color GOLD = NoirTheme.GOLD;
    private static final Color TEXT = NoirTheme.TEXT;
    private static final Color SECONDARY_TEXT = NoirTheme.SECONDARY_TEXT;
    private static final Color BUTTON_BACKGROUND = NoirTheme.SURFACE;
    private static final Color HOVER_BACKGROUND = NoirTheme.HOVER;
    private static final Color BORDER_COLOR = NoirTheme.INNER_BORDER;
    private static final Color ONLINE_COLOR = NoirTheme.SUCCESS;

    private final Chat chat;
    private final DefaultListModel<ChatHistory.Message> messageModel = new DefaultListModel<>();
    private final JList<ChatHistory.Message> messageList = new JList<>(messageModel);
    private final JTextField input = createMessageInput();
    private final JLabel participantCount = new JLabel();

    private ChatChannel activeChannel = ChatChannel.GENERAL;
    private final Map<ChatChannel, Integer> unreadMessages =
            new java.util.EnumMap<>(ChatChannel.class);
    private final CardLayout channelLayout = new CardLayout();
    private final JPanel channelsPanel = new JPanel(channelLayout);
    private final Map<ChatChannel, JButton> channelButtons =
            new java.util.EnumMap<>(ChatChannel.class);

    // PRIVATE CHANNEL
    private final DefaultListModel<ChatHistory.Message> privateModel =
            new DefaultListModel<>();

    private final JList<ChatHistory.Message> privateList =
            new JList<>(privateModel);

    private final JComboBox<String> recipientSelector =
            new JComboBox<>();

    private final JTextField privateInput = createMessageInput();

    private boolean updatingRecipients;

    // SYSTEM CHANNEL
    private final DefaultListModel<ChatHistory.Message> systemModel =
            new DefaultListModel<>();

    private final JList<ChatHistory.Message> systemList =
            new JList<>(systemModel);

    // MESSAGE NOTIFICATION
    private final JLabel activityLabel = new JLabel(" ");
    private Timer activityTimer;

    public ChatRenderer(Chat chat) {
        this(chat, Map.of());
    }

    public ChatRenderer(Chat chat, Map<String, Color> playerColors) {
        super(new BorderLayout(0, 2));
        this.chat = Objects.requireNonNull(chat, "chat");
        Objects.requireNonNull(playerColors, "playerColors");
        setBackground(BACKGROUND);

        // COMMUNICATIONS HEADER
        add(createChatHeader(), BorderLayout.NORTH);

        messageList.setBackground(BACKGROUND);

        messageList.setSelectionBackground(BACKGROUND);
        messageList.setSelectionForeground(TEXT);

        messageList.setFocusable(false);

        messageList.setFixedCellHeight(-1);
        messageList.setFixedCellWidth(-1);

        messageList.setCellRenderer(
                new BubbleRenderer(
                        chat.getLocalPlayer(), playerColors
                )
        );

        messageList.addComponentListener(
                new java.awt.event.ComponentAdapter() {

                    @Override
                    public void componentResized(
                            java.awt.event.ComponentEvent e
                    ) {

                        int width = messageList.getWidth();

                        if (width > 0
                                && messageList.getFixedCellWidth() != width) {

                            messageList.setFixedCellWidth(width);
                            messageList.revalidate();
                            messageList.repaint();
                        }
                    }
                });

        for (ChatHistory.Message message : chat.getHistory().getMessages()) {
            messageModel.addElement(message);
        }

        JScrollPane scroll = new JScrollPane(messageList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.setBackground(BACKGROUND);
        messageList.setBorder(
                BorderFactory.createEmptyBorder(3, 0, 3, 0)
        );
        scroll.setOpaque(true);
        scroll.getViewport().setBackground(BACKGROUND);
        styleScrollBar(scroll.getVerticalScrollBar());

        // MESSAGE INPUT
        input.setToolTipText("Write a message");

        // SEND BUTTON
        JButton sendButton = createSendButton();

        sendButton.addActionListener(event -> sendMessage());
        input.addActionListener(event -> sendMessage());

        sendButton.setEnabled(!input.getText().isBlank());

        input.getDocument().addDocumentListener(
                new DocumentListener() {

                    private void updateButton() {

                        sendButton.setEnabled(
                                !input.getText().isBlank()
                        );
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateButton();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateButton();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateButton();
                    }
                });

        // BOTTOM PANEL
        JPanel bottom = new JPanel(new BorderLayout(7, 0));

        bottom.setOpaque(false);

        bottom.setBorder(new EmptyBorder(5, 6, 6, 6));

        bottom.add(input, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);
        // GENERAL CHANNEL
        JPanel generalPanel = new JPanel(
                new BorderLayout(0, 4)
        );

        generalPanel.setBackground(BACKGROUND);

        generalPanel.add(scroll, BorderLayout.CENTER);
        generalPanel.add(bottom, BorderLayout.SOUTH);

        // CHANNEL CONTAINER
        channelsPanel.setBackground(BACKGROUND);

        channelsPanel.add(
                generalPanel,
                ChatChannel.GENERAL.name()
        );

        channelsPanel.add(
                createPrivateChannelPanel(playerColors),
                ChatChannel.PRIVATE.name()
        );

        channelsPanel.add(
                createSystemChannelPanel(),
                ChatChannel.SYSTEM.name()
        );

        // ADD CHANNELS TO CHAT
        add(channelsPanel, BorderLayout.CENTER);

        // DEFAULT CHANNEL
        switchChannel(ChatChannel.GENERAL);

        chat.getHistory().addListener(this::showMessage);
        if (!messageModel.isEmpty()) {
            messageList.ensureIndexIsVisible(messageModel.size() - 1);
        }
    }

    private JPanel createChatHeader() {

        JPanel header = new JPanel();

        header.setLayout(
                new BoxLayout(header, BoxLayout.Y_AXIS)
        );

        header.setOpaque(false);

        header.setBorder(
                new EmptyBorder(8, 8, 6, 8)
        );

        // MAIN TITLE
        JLabel title = new JLabel("COMMUNICATIONS");

        title.setFont(NoirTheme.TITLE_FONT);
        title.setForeground(NoirTheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // SUBTITLE
        JLabel subtitle = new JLabel(
                "Secure investigation network"
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 10));
        subtitle.setForeground(SECONDARY_TEXT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // TITLE CONTAINER
        JPanel titleContent = new JPanel();

        titleContent.setLayout(
                new BoxLayout(titleContent, BoxLayout.Y_AXIS)
        );

        titleContent.setOpaque(false);

        titleContent.add(title);
        titleContent.add(Box.createVerticalStrut(3));
        titleContent.add(subtitle);

        // CHAT ICON
        JComponent chatIcon = createChatIcon();

        // TITLE ROW
        JPanel titleRow = new JPanel(new BorderLayout(8, 0));

        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleRow.add(chatIcon, BorderLayout.WEST);
        titleRow.add(titleContent, BorderLayout.CENTER);

        // PARTICIPANT COUNTER
        participantCount.setFont(
                new Font("Arial", Font.PLAIN, 11)
        );

        participantCount.setForeground(SECONDARY_TEXT);

        updateParticipantCount();

        JLabel statusDot = new JLabel("\u25CF");

        statusDot.setFont(new Font("Arial", Font.PLAIN, 10));
        statusDot.setForeground(ONLINE_COLOR);

        JPanel statusRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0)
        );

        statusRow.setOpaque(false);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusRow.add(statusDot);
        statusRow.add(participantCount);

        statusRow.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 18)
        );

        // GOLD SEPARATOR
        JPanel separator = new JPanel();

        separator.setBackground(GOLD);
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        separator.setPreferredSize(new Dimension(1, 1));

        separator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1)
        );

        // ADD COMPONENTS
        header.add(titleRow);
        header.add(Box.createVerticalStrut(7));
        header.add(statusRow);
        header.add(Box.createVerticalStrut(7));
        header.add(separator);
        header.add(Box.createVerticalStrut(6));
        // CHANNEL NAVIGATION
        header.add(createChannelTabs());

        header.add(Box.createVerticalStrut(5));

        activityLabel.setFont(
                new Font("Arial", Font.ITALIC, 10)
        );

        activityLabel.setForeground(SECONDARY_TEXT);

        activityLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        activityLabel.setPreferredSize(
                new Dimension(150, 12)
        );

        activityLabel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 12)
        );

        activityLabel.setVisible(false);

        header.add(activityLabel);

        return header;
    }

    private JComponent createChatIcon() {

        JComponent icon = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                int x = (getWidth() - 28) / 2;
                int y = (getHeight() - 28) / 2;

                g2.translate(x, y);

                g2.setColor(GOLD);

                g2.setStroke(new BasicStroke(
                        1.7f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                // SECOND MESSAGE
                g2.drawRoundRect(8, 10, 18, 12, 5, 5);
                g2.drawLine(20, 22, 24, 26);
                g2.drawLine(24, 26, 24, 21);

                // FIRST MESSAGE BACKGROUND
                g2.setColor(BACKGROUND);
                g2.fillRoundRect(2, 2, 19, 15, 5, 5);

                // FIRST MESSAGE BORDER
                g2.setColor(GOLD);
                g2.drawRoundRect(2, 2, 19, 15, 5, 5);

                // MESSAGE TAIL
                g2.drawLine(6, 17, 6, 21);
                g2.drawLine(6, 21, 11, 17);

                // MESSAGE DOTS
                for (int i = 0; i < 3; i++) {

                    g2.fillOval(
                            7 + i * 4,
                            8,
                            2, 2
                    );
                }

                g2.dispose();
            }
        };

        icon.setPreferredSize(new Dimension(32, 32));

        return icon;
    }

    private JPanel createChannelTabs() {

        JPanel tabs = new JPanel(
                new GridLayout(1, 3, 4, 0)
        );

        tabs.setOpaque(false);

        tabs.setAlignmentX(Component.LEFT_ALIGNMENT);

        tabs.setPreferredSize(new Dimension(280, 46));

        tabs.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 46)
        );

        for (ChatChannel channel : ChatChannel.values()) {

            JButton button = createChannelButton(channel);

            channelButtons.put(channel, button);

            tabs.add(button);
        }

        return tabs;
    }

    private JButton createChannelButton(ChatChannel channel) {

        String label = switch (channel) {
            case GENERAL -> "General";
            case PRIVATE -> "Private";
            case SYSTEM -> "System";
        };

        JButton button = new JButton(label) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                );

                boolean active = activeChannel == channel;

                // BACKGROUND
                if (active || getModel().isRollover()) {
                    g2.setColor(HOVER_BACKGROUND);
                } else {
                    g2.setColor(BUTTON_BACKGROUND);
                }

                g2.fillRoundRect(
                        1, 1,
                        getWidth() - 2,
                        getHeight() - 2,
                        8, 8
                );

                // ICON
                Color color = active ? GOLD : SECONDARY_TEXT;

                g2.setColor(color);

                drawChannelIcon(
                        g2,
                        channel,
                        getWidth() / 2 - 9,
                        5
                );

                // LABEL
                g2.setColor(color);

                g2.setFont(new Font(
                        "Arial",
                        active ? Font.BOLD : Font.PLAIN,
                        11
                ));

                FontMetrics fm = g2.getFontMetrics();

                int textX = (
                        getWidth() - fm.stringWidth(label)
                ) / 2;

                g2.drawString(label, textX, 37);

                // ACTIVE CHANNEL INDICATOR
                if (active) {

                    g2.setColor(GOLD);

                    g2.fillRoundRect(
                            5,
                            getHeight() - 4,
                            getWidth() - 10,
                            2,
                            2, 2
                    );
                }

                // UNREAD MESSAGE NOTIFICATION
                int unread = getUnreadCount(channel);

                if (unread > 0) {

                    String notification = unread > 99
                            ? "99+"
                            : String.valueOf(unread);

                    g2.setFont(new Font("Arial", Font.BOLD, 10));

                    FontMetrics metrics = g2.getFontMetrics();

                    int badgeHeight = 16;

                    int badgeWidth = Math.max(
                            16,
                            metrics.stringWidth(notification) + 8
                    );

                    int badgeX = getWidth() - badgeWidth - 4;
                    int badgeY = 3;

                    // BACKGROUND
                    g2.setColor(GOLD);

                    g2.fillRoundRect(
                            badgeX,
                            badgeY,
                            badgeWidth,
                            badgeHeight,
                            16, 16
                    );

                    // NUMBER
                    g2.setColor(BACKGROUND);

                    int numberX = badgeX
                            + (badgeWidth - metrics.stringWidth(notification)) / 2;

                    int numberY = badgeY
                            + (badgeHeight - metrics.getHeight()) / 2
                            + metrics.getAscent();

                    g2.drawString(notification, numberX, numberY);
                }

                g2.dispose();


            }
        };

        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addActionListener(event -> switchChannel(channel));

        return button;
    }

    private void drawChannelIcon(
            Graphics2D graphics,
            ChatChannel channel,
            int x,
            int y
    ) {

        Graphics2D g2 = (Graphics2D) graphics.create();

        g2.translate(x, y);

        g2.setStroke(new BasicStroke(
                1.5f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        switch (channel) {

            case GENERAL -> {

                // FIRST PERSON
                g2.drawOval(3, 1, 6, 6);

                g2.drawRoundRect(
                        1, 9, 10, 7, 4, 4
                );

                // SECOND PERSON
                g2.drawArc(10, 1, 6, 6, -90, 180);

                g2.drawArc(10, 9, 7, 7, -90, 180);
            }

            case PRIVATE -> {

                // LOCK BODY
                g2.drawRoundRect(
                        2, 7, 14, 10, 3, 3
                );

                // LOCK ARCH
                g2.drawArc(5, 0, 8, 12, 0, 180);

                // KEYHOLE
                g2.fillOval(8, 11, 2, 2);
                g2.drawLine(9, 12, 9, 15);
            }

            case SYSTEM -> {

                // RADIO SIGNAL
                g2.fillOval(7, 7, 4, 4);

                g2.drawArc(4, 4, 10, 10, -50, 100);
                g2.drawArc(4, 4, 10, 10, 130, 100);

                g2.drawArc(0, 0, 18, 18, -50, 100);
                g2.drawArc(0, 0, 18, 18, 130, 100);
            }
        }

        g2.dispose();
    }

    private void switchChannel(ChatChannel channel) {

        activeChannel = channel;

        clearUnread(channel);

        channelLayout.show(
                channelsPanel,
                channel.name()
        );

        switch (channel) {

            case GENERAL -> {

                input.requestFocusInWindow();

                if (!messageModel.isEmpty()) {

                    messageList.ensureIndexIsVisible(
                            messageModel.size() - 1
                    );
                }
            }

            case PRIVATE -> {

                refreshRecipients();

                refreshPrivateConversation();

                privateInput.requestFocusInWindow();
            }

            case SYSTEM -> {

                if (!systemModel.isEmpty()) {

                    systemList.ensureIndexIsVisible(
                            systemModel.size() - 1
                    );
                }
            }
        }

        for (JButton button : channelButtons.values()) {
            button.repaint();
        }

        channelsPanel.revalidate();
        channelsPanel.repaint();
    }

    private void incrementUnread(ChatChannel channel) {

        int current = unreadMessages.getOrDefault(channel, 0);

        unreadMessages.put(channel, current + 1);

        JButton button = channelButtons.get(channel);

        if (button != null) {
            button.repaint();
        }
    }

    private void clearUnread(ChatChannel channel) {

        unreadMessages.put(channel, 0);

        JButton button = channelButtons.get(channel);

        if (button != null) {
            button.repaint();
        }
    }

    private int getUnreadCount(ChatChannel channel) {
        return unreadMessages.getOrDefault(channel, 0);
    }

    private JTextField createMessageInput() {

        JTextField field = new JTextField() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // BACKGROUND
                g2.setColor(BACKGROUND);

                g2.fillRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        14, 14
                );

                // BORDER
                g2.setColor(hasFocus() ? GOLD : BORDER_COLOR);

                g2.setStroke(
                        new BasicStroke(hasFocus() ? 1.5f : 1f)
                );

                g2.drawRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        14, 14
                );

                g2.dispose();

                // USER TEXT
                super.paintComponent(g);

                // PLACEHOLDER
                if (getText().isEmpty()) {

                    Graphics2D placeholder =
                            (Graphics2D) g.create();

                    placeholder.setRenderingHint(
                            RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                    );

                    placeholder.setColor(SECONDARY_TEXT);
                    placeholder.setFont(getFont());

                    FontMetrics fm =
                            placeholder.getFontMetrics();

                    int y = (getHeight() - fm.getHeight()) / 2
                            + fm.getAscent();

                    placeholder.drawString(
                            "Write a message...",
                            getInsets().left,
                            y
                    );

                    placeholder.dispose();
                }
            }
        };

        // TEXT STYLE
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(TEXT);
        field.setCaretColor(GOLD);

        // TEXT SELECTION
        field.setSelectionColor(HOVER_BACKGROUND);
        field.setSelectedTextColor(TEXT);

        field.setOpaque(false);

        // INTERNAL PADDING
        field.setBorder(
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );

        field.setPreferredSize(new Dimension(200, 38));

        // FOCUS EFFECT
        field.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.repaint();
            }
        });

        return field;
    }

    private JButton createSendButton() {

        JButton button = new JButton() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // BUTTON BACKGROUND
                if (!isEnabled()) {

                    g2.setColor(HOVER_BACKGROUND);

                } else if (getModel().isPressed()) {

                    g2.setColor(new Color(174, 139, 76));

                } else if (getModel().isRollover()) {

                    g2.setColor(new Color(235, 204, 145));

                } else {

                    g2.setColor(GOLD);
                }

                g2.fillRoundRect(
                        1, 1,
                        getWidth() - 2,
                        getHeight() - 2,
                        12, 12
                );

                // ICON POSITION
                int x = (getWidth() - 20) / 2;
                int y = (getHeight() - 20) / 2;

                g2.translate(x, y);

                // ICON COLOR
                g2.setColor(
                        isEnabled() ? BACKGROUND : SECONDARY_TEXT
                );

                g2.setStroke(new BasicStroke(
                        1.7f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                // PAPER PLANE
                Polygon plane = new Polygon();

                plane.addPoint(1, 8);
                plane.addPoint(19, 1);
                plane.addPoint(12, 19);
                plane.addPoint(8, 12);

                g2.drawPolygon(plane);

                g2.drawLine(8, 12, 19, 1);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(40, 38));

        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setToolTipText("Send message");

        button.getAccessibleContext().setAccessibleName(
                "Send message"
        );

        return button;
    }

    private void styleScrollBar(JScrollBar scrollBar) {

        scrollBar.setPreferredSize(new Dimension(9, 9));
        scrollBar.setOpaque(false);
        scrollBar.setUnitIncrement(16);
        scrollBar.setUI(new NoirScrollBarUI());
    }

    private void sendMessage() {
        if (input.getText().isBlank()) return;
        try {
            chat.send(input.getText());
            input.setText("");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, "No se pudo enviar: " + exception.getMessage());
        }
    }

    private void animateMessageReceived(ChatChannel channel) {

        if (activityTimer != null) {
            activityTimer.stop();
        }

        String notification = switch (channel) {

            case GENERAL -> "New message in General";

            case PRIVATE -> "New confidential message";

            case SYSTEM -> "New system notification";
        };

        activityLabel.setText(notification);

        activityLabel.setVisible(true);
        revalidate();

        activityLabel.setForeground(GOLD);

        final int[] frame = {0};

        activityTimer = new Timer(40, event -> {

            frame[0]++;

            float progress = Math.min(
                    1f,
                    frame[0] / 50f
            );

            int red = (int) (
                    GOLD.getRed()
                            + (SECONDARY_TEXT.getRed()
                            - GOLD.getRed()) * progress
            );

            int green = (int) (
                    GOLD.getGreen()
                            + (SECONDARY_TEXT.getGreen()
                            - GOLD.getGreen()) * progress
            );

            int blue = (int) (
                    GOLD.getBlue()
                            + (SECONDARY_TEXT.getBlue()
                            - GOLD.getBlue()) * progress
            );

            activityLabel.setForeground(
                    new Color(red, green, blue)
            );

            if (progress >= 1f) {

                ((Timer) event.getSource()).stop();

                activityLabel.setText(" ");

                activityLabel.setVisible(false);
                revalidate();
                repaint();
            }
        });

        activityTimer.start();
    }

    private void showMessage(ChatHistory.Message message) {

        Runnable update = () -> {

            switch (message.channel()) {

                case GENERAL -> {

                    messageModel.addElement(message);

                    if (activeChannel == ChatChannel.GENERAL) {

                        messageList.ensureIndexIsVisible(
                                messageModel.size() - 1
                        );

                    } else if (!Objects.equals(
                            message.sender(),
                            chat.getLocalPlayer()
                    )) {

                        incrementUnread(ChatChannel.GENERAL);
                    }
                }

                case PRIVATE -> {

                    refreshRecipients();

                    String localPlayer = chat.getLocalPlayer();

                    boolean mine = Objects.equals(
                            message.sender(),
                            localPlayer
                    );

                    String otherPlayer = mine
                            ? message.recipient()
                            : message.sender();

                    String selected = (String)
                            recipientSelector.getSelectedItem();

                    boolean currentConversation =
                            activeChannel == ChatChannel.PRIVATE
                                    && Objects.equals(
                                    selected, otherPlayer
                            );

                    refreshPrivateConversation();

                    if (!mine && !currentConversation) {

                        incrementUnread(ChatChannel.PRIVATE);
                    }
                }

                case SYSTEM -> {

                    systemModel.addElement(message);

                    if (activeChannel == ChatChannel.SYSTEM) {

                        systemList.ensureIndexIsVisible(
                                systemModel.size() - 1
                        );

                    } else {

                        incrementUnread(ChatChannel.SYSTEM);
                    }
                }
            }

            updateParticipantCount();

            if (!Objects.equals(
                    message.sender(),
                    chat.getLocalPlayer()
            )) {

                animateMessageReceived(message.channel());
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    private void updateParticipantCount() {
        int participants = chat.getParticipants().size();
        participantCount.setText(
                participants + " participants"
        );
    }

    public Chat getChat() {
        return chat;
    }

    public int getDisplayedMessageCount() {
        return messageModel.size();
    }

    /** Las burbujas son parte de la vista, no necesitan otra clase pública. */
    private static final class BubbleRenderer extends JPanel
            implements ListCellRenderer<ChatHistory.Message> {

        private static final DateTimeFormatter TIME =
                DateTimeFormatter.ofPattern("HH:mm");

        private static final Color OTHER_BUBBLE =
                new Color(34, 60, 72);

        private static final Color MY_BUBBLE =
                new Color(52, 72, 62);

        private final String localPlayer;
        private final Map<String, Color> playerColors;

        private final JLabel senderLabel = new JLabel();
        private final JLabel timeLabel = new JLabel();

        private final JTextArea messageLabel = new JTextArea();

        private final JPanel messageContainer =
                new JPanel(new BorderLayout(0, 4));

        private final JPanel header =
                new JPanel(new BorderLayout(6, 0));

        private boolean mine;

        private String avatarName = "";
        private Color avatarColor = GOLD;

        // ROUNDED MESSAGE BUBBLE

        private final JPanel bubble =
                new JPanel(new BorderLayout()) {

                    @Override
                    protected void paintComponent(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                        );

                        // Background
                        g2.setColor(mine ? MY_BUBBLE : OTHER_BUBBLE);

                        g2.fillRoundRect(
                                0, 0,
                                getWidth() - 1,
                                getHeight() - 1,
                                16, 16
                        );

                        // Border
                        g2.setColor(mine
                                ? new Color(102, 119, 87)
                                : new Color(63, 88, 98));

                        g2.drawRoundRect(
                                0, 0,
                                getWidth() - 1,
                                getHeight() - 1,
                                16, 16
                        );

                        g2.dispose();
                    }
                };

        // PLAYER AVATAR

        private final JComponent avatar = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                );

                int size = 30;

                int x = (getWidth() - size) / 2;
                int y = 2;

                // Avatar background
                g2.setColor(new Color(34, 60, 72));

                g2.fillOval(x, y, size, size);

                // Avatar border
                g2.setColor(avatarColor);
                g2.setStroke(new BasicStroke(1.3f));

                g2.drawOval(x, y, size, size);

                // Player initial
                String initial = avatarName.isBlank()
                        ? "?"
                        : avatarName.substring(0, 1).toUpperCase();

                g2.setFont(new Font(
                        "Arial", Font.BOLD, 13
                ));

                FontMetrics fm = g2.getFontMetrics();

                int textX = x + (
                        size - fm.stringWidth(initial)
                ) / 2;

                int textY = y + (
                        size - fm.getHeight()
                ) / 2 + fm.getAscent();

                g2.drawString(initial, textX, textY);

                g2.dispose();
            }
        };

        // CONSTRUCTOR

        private BubbleRenderer(
                String localPlayer,
                Map<String, Color> playerColors
        ) {

            super(new BorderLayout(7, 0));

            this.localPlayer = localPlayer;
            this.playerColors = Map.copyOf(playerColors);

            setOpaque(true);
            setBackground(BACKGROUND);

            setBorder(
                    BorderFactory.createEmptyBorder(5, 6, 5, 6)
            );

            // AVATAR
            avatar.setPreferredSize(new Dimension(32, 34));

            // HEADER
            senderLabel.setFont(
                    new Font("Arial", Font.BOLD, 11)
            );

            timeLabel.setFont(
                    new Font("Arial", Font.PLAIN, 10)
            );

            timeLabel.setForeground(SECONDARY_TEXT);

            header.setOpaque(false);

            header.add(senderLabel, BorderLayout.CENTER);
            header.add(timeLabel, BorderLayout.EAST);

            // MESSAGE TEXT
            messageLabel.setEditable(false);
            messageLabel.setFocusable(false);
            messageLabel.setLineWrap(true);
            messageLabel.setWrapStyleWord(true);
            messageLabel.setOpaque(false);

            messageLabel.setForeground(TEXT);

            messageLabel.setFont(
                    new Font("Arial", Font.PLAIN, 12)
            );

            messageLabel.setBorder(null);
            messageLabel.setMargin(new Insets(0, 0, 0, 0));

            // MESSAGE BUBBLE
            bubble.setOpaque(false);

            bubble.setBorder(
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            );

            bubble.add(messageLabel, BorderLayout.CENTER);

            // MESSAGE CONTAINER
            messageContainer.setOpaque(false);

            messageContainer.add(header, BorderLayout.NORTH);
            messageContainer.add(bubble, BorderLayout.CENTER);
        }

        // IMPROVE PLAYER COLOR CONTRAST

        private static Color readable(Color color) {

            double brightness =
                    0.2126 * color.getRed()
                            + 0.7152 * color.getGreen()
                            + 0.0722 * color.getBlue();

            if (brightness >= 145) {
                return color;
            }

            return new Color(
                    (color.getRed() + 255) / 2,
                    (color.getGreen() + 255) / 2,
                    (color.getBlue() + 255) / 2
            );
        }

        // RENDER MESSAGE

        @Override
        public Component getListCellRendererComponent(
                JList<? extends ChatHistory.Message> list,
                ChatHistory.Message message,
                int index,
                boolean selected,
                boolean focused
        ) {

            removeAll();

            mine = message.sender().equals(localPlayer);

            Color playerColor = readable(
                    playerColors.getOrDefault(
                            message.sender(), GOLD
                    )
            );

            avatarName = message.sender();
            avatarColor = playerColor;

            // Sender and timestamp
            senderLabel.setText(
                    mine ? "You" : message.sender()
            );

            senderLabel.setForeground(
                    mine ? GOLD : playerColor
            );

            timeLabel.setText(
                    message.time().format(TIME)
            );

            // Message content
            messageLabel.setText(message.text());

            // Calculate available width
            int listWidth = list.getWidth() > 0
                    ? list.getWidth()
                    : 290;

            int availableWidth = Math.max(
                    80, listWidth - 12
            );

            int maxBubbleWidth = Math.max(
                    60,
                    Math.min(
                            (int) (availableWidth * 0.82),
                            availableWidth - (mine ? 0 : 39)
                    )
            );

            // Calculate dynamic bubble width
            FontMetrics fm = messageLabel.getFontMetrics(
                    messageLabel.getFont()
            );

            int textLength = 0;

            for (String line : message.text().split("\n", -1)) {
                textLength = Math.max(
                        textLength, fm.stringWidth(line)
                );
            }

            int headerWidth =
                    senderLabel.getPreferredSize().width
                            + timeLabel.getPreferredSize().width
                            + 6;

            int bubbleWidth = Math.min(
                    maxBubbleWidth,
                    Math.max(
                            85,
                            Math.max(textLength + 20, headerWidth)
                    )
            );

            int textWidth = Math.max(40, bubbleWidth - 20);

            // Reset previous renderer dimensions
            messageLabel.setPreferredSize(null);

            messageLabel.setSize(
                    textWidth,
                    Short.MAX_VALUE
            );

            int textHeight = Math.max(
                    16,
                    messageLabel.getPreferredSize().height
            );

            messageLabel.setPreferredSize(
                    new Dimension(textWidth, textHeight)
            );

            bubble.setPreferredSize(
                    new Dimension(bubbleWidth, textHeight + 16)
            );

            int containerHeight = textHeight + 34;

            messageContainer.setPreferredSize(
                    new Dimension(bubbleWidth, containerHeight)
            );

            // Align messages
            JPanel alignment = new JPanel(
                    new BorderLayout(7, 0)
            );

            alignment.setOpaque(false);

            if (mine) {

                alignment.add(
                        messageContainer,
                        BorderLayout.EAST
                );

            } else {

                JPanel otherMessage =
                        new JPanel(new BorderLayout(7, 0));

                otherMessage.setOpaque(false);

                otherMessage.add(avatar, BorderLayout.WEST);

                otherMessage.add(
                        messageContainer,
                        BorderLayout.CENTER
                );

                alignment.add(
                        otherMessage,
                        BorderLayout.WEST
                );
            }

            add(alignment, BorderLayout.CENTER);

            setPreferredSize(
                    new Dimension(
                            listWidth,
                            Math.max(44, containerHeight + 10)
                    )
            );

            return this;
        }
    }

    private JPanel createPrivateChannelPanel(
            Map<String, Color> playerColors
    ) {

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(BACKGROUND);

        // RECIPIENT SELECTOR
        JPanel recipientPanel = new JPanel(
                new BorderLayout(8, 0)
        );

        recipientPanel.setOpaque(false);

        recipientPanel.setBorder(
                new EmptyBorder(4, 6, 4, 6)
        );

        JLabel recipientLabel = new JLabel("Recipient:");

        recipientLabel.setForeground(GOLD);
        recipientLabel.setFont(new Font("Arial", Font.BOLD, 11));

        styleRecipientSelector();

        recipientPanel.add(recipientLabel, BorderLayout.WEST);
        recipientPanel.add(recipientSelector, BorderLayout.CENTER);

        // PRIVATE MESSAGE LIST
        configureMessageList(privateList, playerColors);

        JScrollPane scroll = createChatScroll(privateList);

        // PRIVATE INPUT
        privateInput.setToolTipText("Write a private message");

        JButton sendButton = createSendButton();

        Runnable updateButton = () -> {

            sendButton.setEnabled(
                    recipientSelector.getSelectedItem() != null
                            && !privateInput.getText().isBlank()
            );
        };

        sendButton.addActionListener(e -> sendPrivateMessage());

        privateInput.addActionListener(e -> sendPrivateMessage());

        privateInput.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateButton.run();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateButton.run();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateButton.run();
                    }
                });

        recipientSelector.addActionListener(e -> {

            if (!updatingRecipients) {

                refreshPrivateConversation();
                updateButton.run();
            }
        });

        // PRIVATE MESSAGE COMPOSER
        JPanel bottom = new JPanel(new BorderLayout(7, 0));

        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(5, 6, 6, 6));

        bottom.add(privateInput, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        refreshRecipients();
        refreshPrivateConversation();

        updateButton.run();

        panel.add(recipientPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void configureMessageList(
            JList<ChatHistory.Message> list,
            Map<String, Color> playerColors
    ) {

        list.setBackground(BACKGROUND);

        list.setSelectionBackground(BACKGROUND);
        list.setSelectionForeground(TEXT);

        list.setFocusable(false);

        list.setFixedCellHeight(-1);
        list.setFixedCellWidth(-1);

        list.setBorder(new EmptyBorder(3, 0, 3, 0));

        list.setCellRenderer(
                new BubbleRenderer(
                        chat.getLocalPlayer(),
                        playerColors
                )
        );

        list.addComponentListener(
                new java.awt.event.ComponentAdapter() {

                    @Override
                    public void componentResized(
                            java.awt.event.ComponentEvent e
                    ) {

                        int width = list.getWidth();

                        if (width > 0
                                && list.getFixedCellWidth() != width) {

                            list.setFixedCellWidth(width);
                            list.revalidate();
                            list.repaint();
                        }
                    }
                });
    }

    private JScrollPane createChatScroll(JComponent component) {

        JScrollPane scroll = new JScrollPane(
                component,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);

        scroll.setBackground(BACKGROUND);
        scroll.getViewport().setBackground(BACKGROUND);

        styleScrollBar(scroll.getVerticalScrollBar());

        return scroll;
    }

    private void styleRecipientSelector() {

        recipientSelector.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        recipientSelector.setBackground(BUTTON_BACKGROUND);
        recipientSelector.setForeground(TEXT);

        recipientSelector.setPreferredSize(
                new Dimension(150, 30)
        );

        recipientSelector.setBorder(
                BorderFactory.createLineBorder(BORDER_COLOR)
        );

        recipientSelector.setFocusable(false);

        recipientSelector.setUI(new BasicComboBoxUI() {

            @Override
            protected JButton createArrowButton() {

                JButton button = new JButton("\u25BE");

                button.setForeground(GOLD);
                button.setBackground(BUTTON_BACKGROUND);
                button.setBorderPainted(false);
                button.setFocusPainted(false);

                button.setPreferredSize(new Dimension(25, 28));

                return button;
            }
        });

        recipientSelector.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean selected,
                            boolean focused
                    ) {

                        JLabel label = (JLabel)
                                super.getListCellRendererComponent(
                                        list, value, index,
                                        selected, focused
                                );

                        label.setBackground(
                                selected
                                        ? HOVER_BACKGROUND
                                        : BUTTON_BACKGROUND
                        );

                        label.setForeground(selected ? GOLD : TEXT);

                        label.setBorder(new EmptyBorder(4, 8, 4, 8));

                        return label;
                    }
                });
    }

    private void refreshRecipients() {

        String selected =
                (String) recipientSelector.getSelectedItem();

        updatingRecipients = true;

        try {

            recipientSelector.removeAllItems();

            for (String player : chat.getParticipants()) {

                if (!Objects.equals(
                        player, chat.getLocalPlayer()
                )) {

                    recipientSelector.addItem(player);
                }
            }

            if (selected != null) {
                recipientSelector.setSelectedItem(selected);
            }

            if (recipientSelector.getSelectedIndex() == -1
                    && recipientSelector.getItemCount() > 0) {

                recipientSelector.setSelectedIndex(0);
            }

        } finally {

            updatingRecipients = false;
        }

        recipientSelector.setEnabled(
                recipientSelector.getItemCount() > 0
        );
    }

    private void refreshPrivateConversation() {

        privateModel.clear();

        String recipient =
                (String) recipientSelector.getSelectedItem();

        if (recipient == null) {
            return;
        }

        for (ChatHistory.Message message :
                chat.getHistory().getPrivateMessages(
                        chat.getLocalPlayer(),
                        recipient
                )) {

            privateModel.addElement(message);
        }

        if (!privateModel.isEmpty()) {

            privateList.ensureIndexIsVisible(
                    privateModel.size() - 1
            );
        }
    }

    private void sendPrivateMessage() {

        String recipient =
                (String) recipientSelector.getSelectedItem();

        String text = privateInput.getText();

        if (recipient == null || text.isBlank()) {
            return;
        }

        try {

            chat.sendPrivate(recipient, text);

            privateInput.setText("");
            privateInput.requestFocusInWindow();

        } catch (RuntimeException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo enviar: "
                            + exception.getMessage(),
                    "Private message",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createSystemChannelPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 4));

        panel.setBackground(BACKGROUND);

        systemList.setBackground(BACKGROUND);
        systemList.setFocusable(false);

        systemList.setFixedCellHeight(-1);

        systemList.setCellRenderer(
                new SystemMessageRenderer()
        );

        JScrollPane scroll = createChatScroll(systemList);

        for (ChatHistory.Message message :
                chat.getHistory().getMessages(ChatChannel.SYSTEM)) {

            systemModel.addElement(message);
        }

        JLabel readOnly = new JLabel(
                "Read-only channel",
                SwingConstants.CENTER
        );

        readOnly.setForeground(SECONDARY_TEXT);
        readOnly.setFont(new Font("Arial", Font.PLAIN, 10));

        readOnly.setBorder(new EmptyBorder(6, 0, 6, 0));

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(readOnly, BorderLayout.SOUTH);

        return panel;
    }

    private static final class SystemMessageRenderer
            extends JPanel
            implements ListCellRenderer<ChatHistory.Message> {

        private final JLabel title = new JLabel("SYSTEM");

        private final JLabel time = new JLabel();

        private final JTextArea content = new JTextArea();

        public SystemMessageRenderer() {

            super(new BorderLayout(0, 5));

            setBackground(BUTTON_BACKGROUND);

            setBorder(BorderFactory.createCompoundBorder(

                    BorderFactory.createMatteBorder(
                            0, 0, 4, 0, BACKGROUND
                    ),

                    new EmptyBorder(10, 10, 10, 10)
            ));

            title.setForeground(GOLD);
            title.setFont(new Font("Arial", Font.BOLD, 10));

            time.setForeground(SECONDARY_TEXT);
            time.setFont(new Font("Arial", Font.PLAIN, 10));

            content.setForeground(TEXT);
            content.setFont(new Font("Arial", Font.PLAIN, 12));

            content.setEditable(false);
            content.setFocusable(false);
            content.setOpaque(false);

            content.setLineWrap(true);
            content.setWrapStyleWord(true);

            JPanel header = new JPanel(new BorderLayout());

            header.setOpaque(false);

            header.add(title, BorderLayout.WEST);
            header.add(time, BorderLayout.EAST);

            add(header, BorderLayout.NORTH);
            add(content, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends ChatHistory.Message> list,
                ChatHistory.Message message,
                int index,
                boolean selected,
                boolean focused
        ) {

            time.setText(
                    message.time().format(
                            DateTimeFormatter.ofPattern("HH:mm")
                    )
            );

            content.setText(message.text());

            int width = list.getWidth() > 0
                    ? list.getWidth()
                    : 280;

            content.setPreferredSize(null);

            content.setSize(
                    Math.max(50, width - 24),
                    Short.MAX_VALUE
            );

            setPreferredSize(null);

            return this;
        }
    }
}
