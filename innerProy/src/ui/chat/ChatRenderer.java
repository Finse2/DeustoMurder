package ui.chat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

/** Vista Swing de un Chat. No guarda mensajes ni gestiona sockets. */
public final class ChatRenderer extends JPanel {

    private static final Color BACKGROUND = new Color(25, 55, 75);

    private final Chat chat;
    private final DefaultListModel<ChatHistory.Message> messageModel = new DefaultListModel<>();
    private final JList<ChatHistory.Message> messageList = new JList<>(messageModel);
    private final JTextField input = new JTextField();
    private final JLabel participantCount = new JLabel();

    public ChatRenderer(Chat chat) {
        this(chat, Map.of());
    }

    public ChatRenderer(Chat chat, Map<String, Color> playerColors) {
        super(new BorderLayout(0, 5));
        this.chat = Objects.requireNonNull(chat, "chat");
        Objects.requireNonNull(playerColors, "playerColors");
        setBackground(BACKGROUND);

        JLabel title = new JLabel("Chat", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setBorder(new EmptyBorder(12, 10, 0, 10));

        participantCount.setHorizontalAlignment(SwingConstants.CENTER);
        participantCount.setForeground(new Color(175, 200, 215));
        participantCount.setFont(new Font("Arial", Font.PLAIN, 12));
        updateParticipantCount();

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.CENTER);
        top.add(participantCount, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        messageList.setBackground(BACKGROUND);
        messageList.setSelectionBackground(BACKGROUND);
        messageList.setSelectionForeground(Color.WHITE);
        messageList.setFocusable(false);
        messageList.setFixedCellHeight(-1);
        messageList.setCellRenderer(new BubbleRenderer(chat.getLocalPlayer(), playerColors));
        for (ChatHistory.Message message : chat.getHistory().getMessages()) {
            messageModel.addElement(message);
        }

        JScrollPane scroll = new JScrollPane(messageList);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        scroll.getViewport().setBackground(BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(15);
        add(scroll, BorderLayout.CENTER);

        input.setFont(new Font("Arial", Font.PLAIN, 13));
        input.setToolTipText("Escribe un mensaje");
        JButton send = new JButton("Enviar");
        send.setFont(new Font("Arial", Font.BOLD, 12));
        send.setForeground(Color.WHITE);
        send.setBackground(new Color(37, 105, 128));
        send.setFocusPainted(false);
        send.addActionListener(event -> sendMessage());
        input.addActionListener(event -> sendMessage());

        JPanel bottom = new JPanel(new BorderLayout(5, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(4, 7, 8, 7));
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(send, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        chat.getHistory().addListener(this::showMessage);
        if (!messageModel.isEmpty()) {
            messageList.ensureIndexIsVisible(messageModel.size() - 1);
        }
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

    private void showMessage(ChatHistory.Message message) {
        Runnable update = () -> {
            messageModel.addElement(message);
            updateParticipantCount();
            messageList.ensureIndexIsVisible(messageModel.size() - 1);
        };
        if (SwingUtilities.isEventDispatchThread()) update.run();
        else SwingUtilities.invokeLater(update);
    }

    private void updateParticipantCount() {
        participantCount.setText(chat.getParticipants().size() + " participantes");
    }

    public Chat getChat() {
        return chat;
    }

    public int getDisplayedMessageCount() {
        return messageModel.size();
    }

    /** Las burbujas son parte de la vista, no necesitan otra clase pública. */
    private static final class BubbleRenderer extends JPanel implements ListCellRenderer<ChatHistory.Message> {

        private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
        private static final Color OTHER_BUBBLE = new Color(32, 70, 94);
        private static final Color MY_BUBBLE = new Color(33, 96, 115);

        private final String localPlayer;
        private final Map<String, Color> playerColors;
        private final JPanel bubble = new JPanel(new BorderLayout(0, 3));
        private final JPanel header = new JPanel(new BorderLayout(8, 0));
        private final JLabel senderLabel = new JLabel();
        private final JLabel timeLabel = new JLabel();
        private final JTextArea messageLabel = new JTextArea();

        private BubbleRenderer(String localPlayer, Map<String, Color> playerColors) {
            super(new BorderLayout());
            this.localPlayer = localPlayer;
            this.playerColors = Map.copyOf(playerColors);
            setOpaque(true);
            setBackground(BACKGROUND);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            timeLabel.setForeground(new Color(185, 204, 216));
            header.setOpaque(false);
            header.add(senderLabel, BorderLayout.CENTER);
            header.add(timeLabel, BorderLayout.EAST);

            messageLabel.setEditable(false);
            messageLabel.setFocusable(false);
            messageLabel.setLineWrap(true);
            messageLabel.setWrapStyleWord(true);
            messageLabel.setOpaque(false);
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            messageLabel.setBorder(null);

            bubble.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            bubble.add(header, BorderLayout.NORTH);
            bubble.add(messageLabel, BorderLayout.CENTER);
        }

        private static Color readable(Color color) {
            double brightness = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
            if (brightness >= 145) return color;
            return new Color((color.getRed() + 255) / 2,
                    (color.getGreen() + 255) / 2, (color.getBlue() + 255) / 2);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ChatHistory.Message> list,
                                                        ChatHistory.Message message, int index,
                                                        boolean selected, boolean focused) {
            removeAll();
            boolean mine = message.sender().equals(localPlayer);
            senderLabel.setText(message.sender());
            senderLabel.setForeground(readable(playerColors.getOrDefault(
                    message.sender(), new Color(235, 215, 170))));
            timeLabel.setText(message.time().format(TIME));
            messageLabel.setText(message.text());
            bubble.setBackground(mine ? MY_BUBBLE : OTHER_BUBBLE);

            int listWidth = list.getWidth() > 0 ? list.getWidth() : 290;
            int bubbleWidth = Math.max(120, listWidth - 45);
            int textWidth = bubbleWidth - 20;
            messageLabel.setSize(textWidth, Short.MAX_VALUE);
            int textHeight = Math.max(18, messageLabel.getPreferredSize().height);
            messageLabel.setPreferredSize(new Dimension(textWidth, textHeight));
            bubble.setPreferredSize(new Dimension(bubbleWidth, textHeight + 38));
            add(bubble, mine ? BorderLayout.EAST : BorderLayout.WEST);
            setPreferredSize(new Dimension(Math.max(listWidth, bubbleWidth + 16), textHeight + 48));
            return this;
        }
    }
}
