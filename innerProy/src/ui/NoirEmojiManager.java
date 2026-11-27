package ui;

import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class NoirEmojiManager {

    private static final Path EMOJI_DIRECTORY =
            Path.of("innerProy", "ressources", "emojis");

    private static final String[] CATEGORIES = {
            "faces",
            "investigation",
            "reactions"
    };

    private static final Map<String, ImageIcon> CACHE =
            new HashMap<>();

    private NoirEmojiManager() {
    }

    // CONVERT EMOJI TO FILENAME

    public static String getFileName(String emoji) {

        StringJoiner name = new StringJoiner("-");

        emoji.codePoints()
                .filter(cp -> cp != 0xFE0F)
                .forEach(cp ->
                        name.add(Integer.toHexString(cp))
                );

        return name + ".png";
    }

    // FIND EMOJI FILE

    private static Path findEmoji(String fileName) {

        for (String category : CATEGORIES) {

            Path path = EMOJI_DIRECTORY
                    .resolve(category)
                    .resolve(fileName);

            if (Files.isRegularFile(path)) {
                return path;
            }
        }

        return null;
    }

    // LOAD AND SCALE EMOJI

    public static ImageIcon getIcon(String emoji, int size) {

        String fileName = getFileName(emoji);

        String key = fileName + ":" + size;

        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }

        Path path = findEmoji(fileName);

        if (path == null) {
            return null;
        }

        ImageIcon original = new ImageIcon(
                path.toString()
        );

        if (original.getIconWidth() <= 0) {
            return null;
        }

        Image scaled = original.getImage()
                .getScaledInstance(
                        size,
                        size,
                        Image.SCALE_SMOOTH
                );

        ImageIcon icon = new ImageIcon(scaled);

        icon.setDescription(emoji);

        CACHE.put(key, icon);

        return icon;
    }

    // CHECK IF EMOJI EXISTS

    public static boolean hasEmoji(String emoji) {

        return findEmoji(getFileName(emoji)) != null;
    }

    public static void renderMessage(
            JTextPane target,
            String message,
            int emojiSize
    ) {

        StyledDocument document = target.getStyledDocument();

        SimpleAttributeSet textStyle = new SimpleAttributeSet();

        StyleConstants.setForeground(
                textStyle,
                target.getForeground()
        );

        StyleConstants.setFontFamily(
                textStyle,
                target.getFont().getFamily()
        );

        StyleConstants.setFontSize(
                textStyle,
                target.getFont().getSize()
        );

        try {

            document.remove(0, document.getLength());

            int index = 0;

            while (index < message.length()) {

                int cp = message.codePointAt(index);

                int next = index + Character.charCount(cp);

                if (next < message.length()
                        && message.codePointAt(next) == 0xFE0F) {

                    next++;
                }

                String character = message.substring(index, next);

                ImageIcon icon = getIcon(character, emojiSize);

                if (icon != null) {

                    SimpleAttributeSet iconStyle =
                            new SimpleAttributeSet();

                    StyleConstants.setIcon(iconStyle, icon);

                    document.insertString(
                            document.getLength(),
                            "\uFFFC",
                            iconStyle
                    );

                } else {

                    document.insertString(
                            document.getLength(),
                            character,
                            textStyle
                    );
                }

                index = next;
            }

        } catch (BadLocationException e) {

            throw new IllegalStateException(
                    "Could not render message",
                    e
            );
        }
    }

    public static int measureText(
            String text,
            FontMetrics fm,
            int emojiSize
    ) {

        int width = 0;
        int index = 0;

        while (index < text.length()) {

            int cp = text.codePointAt(index);

            int next = index + Character.charCount(cp);

            if (next < text.length()
                    && text.codePointAt(next) == 0xFE0F) {

                next++;
            }

            String character = text.substring(index, next);

            if (getIcon(character, emojiSize) != null) {

                width += emojiSize;

            } else {

                width += fm.stringWidth(character);
            }

            index = next;
        }

        return width;
    }
}