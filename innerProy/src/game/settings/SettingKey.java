
package game.settings;

import java.util.Objects;

public enum SettingKey {

    // AUDIO

    MASTER_VOLUME(
            "audio.master_volume",
            Type.INTEGER,
            "100",
            0,
            100
    ),

    MUSIC_VOLUME(
            "audio.music_volume",
            Type.INTEGER,
            "60",
            0,
            100
    ),

    SOUND_EFFECTS_VOLUME(
            "audio.sfx_volume",
            Type.INTEGER,
            "80",
            0,
            100
    ),

    MUTE_AUDIO(
            "audio.muted",
            Type.BOOLEAN,
            "false"
    ),

    // GRAPHICS

    FULLSCREEN(
            "graphics.fullscreen",
            Type.BOOLEAN,
            "false"
    ),

    ANIMATIONS_ENABLED(
            "graphics.animations",
            Type.BOOLEAN,
            "true"
    ),

    WINDOW_WIDTH(
            "graphics.window_width",
            Type.INTEGER,
            "1280",
            640,
            7680
    ),

    WINDOW_HEIGHT(
            "graphics.window_height",
            Type.INTEGER,
            "720",
            480,
            4320
    ),

    // INTERFACE

    LANGUAGE(
            "interface.language",
            Type.STRING,
            "en"
    ),

    THEME(
            "interface.theme",
            Type.STRING,
            "noir"
    ),

    ACHIEVEMENT_NOTIFICATIONS(
            "interface.achievement_notifications",
            Type.BOOLEAN,
            "true"
    ),

    GAME_NOTIFICATIONS(
            "interface.game_notifications",
            Type.BOOLEAN,
            "true"
    ),

    UI_SCALE(
            "interface.scale",
            Type.DOUBLE,
            "1.0",
            0.75,
            2.0
    ),

    // NETWORKING

    PREFERRED_PORT(
            "network.preferred_port",
            Type.INTEGER,
            "5000",
            1024,
            65535
    ),

    LAST_SERVER_ADDRESS(
            "network.last_server_address",
            Type.STRING,
            ""
    ),

    LAST_SERVER_PORT(
            "network.last_server_port",
            Type.INTEGER,
            "5000",
            1,
            65535
    ),

    // GAMEPLAY

    CONFIRM_ACCUSATIONS(
            "gameplay.confirm_accusations",
            Type.BOOLEAN,
            "true"
    ),

    SHOW_HINTS(
            "gameplay.show_hints",
            Type.BOOLEAN,
            "true"
    ),

    // ACCESSIBILITY

    REDUCED_MOTION(
            "accessibility.reduced_motion",
            Type.BOOLEAN,
            "false"
    ),

    HIGH_CONTRAST(
            "accessibility.high_contrast",
            Type.BOOLEAN,
            "false"
    ),
    BRIGHTNESS(
            "graphics.brightness",
            Type.INTEGER,
            "100",
            0,
            100
    ),

    HIGHLIGHT_POSSIBLE_MOVES(
            "gameplay.highlight_possible_moves",
            Type.BOOLEAN,
            "true"
    ),

    DEFAULT_SERVER_ADDRESS(
            "network.default_server_address",
            Type.STRING,
            "127.0.0.1"
    ),

    DEFAULT_SERVER_PORT(
            "network.default_server_port",
            Type.INTEGER,
            "5000",
            1,
            65535
    )
    ;



    // SETTING TYPES

    public enum Type {

        STRING,
        INTEGER,
        BOOLEAN,
        DOUBLE
    }

    // FIELDS

    private final String key;

    private final Type type;

    private final String defaultValue;

    private final Double minimum;

    private final Double maximum;

    // CONSTRUCTORS

    SettingKey(
            String key,
            Type type,
            String defaultValue
    ) {

        this.key = key;

        this.type = type;

        this.defaultValue = defaultValue;

        this.minimum = null;

        this.maximum = null;
    }

    SettingKey(
            String key,
            Type type,
            String defaultValue,
            double minimum,
            double maximum
    ) {

        this.key = key;

        this.type = type;

        this.defaultValue = defaultValue;

        this.minimum = minimum;

        this.maximum = maximum;
    }

    // GETTERS

    public String getKey() {
        return key;
    }

    public Type getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Double getMinimum() {
        return minimum;
    }

    public Double getMaximum() {
        return maximum;
    }

    // VALIDATION

    public void validate(String value) {

        Objects.requireNonNull(value);

        switch (type) {

            case STRING -> {

                if (value.length() > 4096) {

                    throw new IllegalArgumentException(
                            key + " value is too long."
                    );
                }
            }

            case BOOLEAN -> {

                if (!value.equals("true")
                        && !value.equals("false")) {

                    throw new IllegalArgumentException(
                            key + " requires a boolean."
                    );
                }
            }

            case INTEGER -> {

                try {

                    validateRange(
                            Integer.parseInt(value)
                    );

                } catch (NumberFormatException e) {

                    throw new IllegalArgumentException(
                            key + " requires an integer.",
                            e
                    );
                }
            }

            case DOUBLE -> {

                try {

                    validateRange(
                            Double.parseDouble(value)
                    );

                } catch (NumberFormatException e) {

                    throw new IllegalArgumentException(
                            key + " requires a number.",
                            e
                    );
                }
            }
        }
    }

    private void validateRange(double value) {

        if (!Double.isFinite(value)) {

            throw new IllegalArgumentException(
                    key + " requires a finite number."
            );
        }

        if (minimum != null && value < minimum) {

            throw new IllegalArgumentException(
                    key + " must be at least "
                            + minimum
            );
        }

        if (maximum != null && value > maximum) {

            throw new IllegalArgumentException(
                    key + " must not exceed "
                            + maximum
            );
        }
    }
}
