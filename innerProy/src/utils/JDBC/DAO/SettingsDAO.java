
package utils.JDBC.DAO;

import utils.JDBC.DatabaseService;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SettingsDAO {

    private final DatabaseService database;

    // CONSTRUCTORS

    public SettingsDAO() {

        this(DatabaseService.getInstance());
    }

    public SettingsDAO(DatabaseService database) {

        this.database = Objects.requireNonNull(
                database, "database"
        );
    }

    // SAVE OR UPDATE SETTING

    public void setSetting(
            UUID playerId,
            String key,
            String value
    ) throws Exception {

        Objects.requireNonNull(playerId);
        Objects.requireNonNull(value);

        validateKey(key);

        database.execute(db -> {

            int affected = db.executeUpdate("""
                    INSERT INTO player_settings (

                        player_id,
                        setting_key,
                        setting_value,
                        updated_at
                    )

                    SELECT

                        player_id,
                        ?,
                        ?,
                        ?

                    FROM players

                    WHERE player_id = ?

                    ON CONFLICT(
                        player_id,
                        setting_key
                    )

                    DO UPDATE SET

                        setting_value =
                            excluded.setting_value,

                        updated_at = MAX(
                            player_settings.updated_at,
                            excluded.updated_at
                        )
                    """,

                    key,
                    value,
                    System.currentTimeMillis(),
                    playerId.toString()
            );

            if (affected != 1) {

                throw new SQLException(
                        "Could not save setting for player: "
                                + playerId
                );
            }

            return null;
        });
    }

    // TYPED SETTERS

    public void setInt(
            UUID playerId,
            String key,
            int value
    ) throws Exception {

        setSetting(
                playerId,
                key,
                Integer.toString(value)
        );
    }

    public void setLong(
            UUID playerId,
            String key,
            long value
    ) throws Exception {

        setSetting(
                playerId,
                key,
                Long.toString(value)
        );
    }

    public void setBoolean(
            UUID playerId,
            String key,
            boolean value
    ) throws Exception {

        setSetting(
                playerId,
                key,
                Boolean.toString(value)
        );
    }

    public void setDouble(
            UUID playerId,
            String key,
            double value
    ) throws Exception {

        if (!Double.isFinite(value)) {

            throw new IllegalArgumentException(
                    "Setting must be a finite number."
            );
        }

        setSetting(
                playerId,
                key,
                Double.toString(value)
        );
    }

    // GET SETTING

    public Optional<String> getSetting(
            UUID playerId,
            String key
    ) throws Exception {

        Objects.requireNonNull(playerId);

        validateKey(key);

        return database.execute(db -> {

            try (ResultSet rs = db.executeQuery("""
                    SELECT setting_value

                    FROM player_settings

                    WHERE player_id = ?

                      AND setting_key = ?
                    """,

                    playerId.toString(),
                    key
            )) {

                if (rs.next()) {

                    return Optional.of(
                            rs.getString("setting_value")
                    );
                }

                return Optional.empty();
            }
        });
    }

    // GET STRING

    public String getString(
            UUID playerId,
            String key,
            String defaultValue
    ) throws Exception {

        return getSetting(playerId, key)
                .orElse(defaultValue);
    }

    // GET INTEGER

    public int getInt(
            UUID playerId,
            String key,
            int defaultValue
    ) throws Exception {

        Optional<String> value =
                getSetting(playerId, key);

        if (value.isEmpty()) {
            return defaultValue;
        }

        try {

            return Integer.parseInt(value.get());

        } catch (NumberFormatException e) {

            throw new SQLException(
                    "Invalid integer setting: " + key,
                    e
            );
        }
    }

    // GET LONG

    public long getLong(
            UUID playerId,
            String key,
            long defaultValue
    ) throws Exception {

        Optional<String> value =
                getSetting(playerId, key);

        if (value.isEmpty()) {
            return defaultValue;
        }

        try {

            return Long.parseLong(value.get());

        } catch (NumberFormatException e) {

            throw new SQLException(
                    "Invalid long setting: " + key,
                    e
            );
        }
    }

    // GET BOOLEAN

    public boolean getBoolean(
            UUID playerId,
            String key,
            boolean defaultValue
    ) throws Exception {

        Optional<String> value =
                getSetting(playerId, key);

        if (value.isEmpty()) {
            return defaultValue;
        }

        return switch (value.get()) {

            case "true" -> true;

            case "false" -> false;

            default -> throw new SQLException(
                    "Invalid boolean setting: " + key
            );
        };
    }

    // GET DOUBLE

    public double getDouble(
            UUID playerId,
            String key,
            double defaultValue
    ) throws Exception {

        Optional<String> value =
                getSetting(playerId, key);

        if (value.isEmpty()) {
            return defaultValue;
        }

        try {

            double result =
                    Double.parseDouble(value.get());

            if (!Double.isFinite(result)) {

                throw new NumberFormatException(
                        "Non-finite number."
                );
            }

            return result;

        } catch (NumberFormatException e) {

            throw new SQLException(
                    "Invalid double setting: " + key,
                    e
            );
        }
    }

    // GET ALL SAVED SETTINGS

    public Map<String, String> getAllSettings(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db -> {

            Map<String, String> settings =
                    new LinkedHashMap<>();

            try (ResultSet rs = db.executeQuery("""
                    SELECT
                        setting_key,
                        setting_value

                    FROM player_settings

                    WHERE player_id = ?

                    ORDER BY setting_key
                    """,

                    playerId.toString()
            )) {

                while (rs.next()) {

                    settings.put(

                            rs.getString("setting_key"),

                            rs.getString("setting_value")
                    );
                }
            }

            return Collections.unmodifiableMap(settings);
        });
    }

    // INITIALIZE MISSING DEFAULTS

    public void initializeDefaults(
            UUID playerId,
            Map<String, String> defaults
    ) throws Exception {

        Objects.requireNonNull(playerId);

        Map<String, String> copy =
                Map.copyOf(defaults);

        // Validate every key before writing anything.

        for (String key : copy.keySet()) {

            validateKey(key);
        }

        database.execute(db -> {

            try (ResultSet rs = db.executeQuery("""
                    SELECT player_id

                    FROM players

                    WHERE player_id = ?
                    """,

                    playerId.toString()
            )) {

                if (!rs.next()) {

                    throw new SQLException(
                            "Unknown player: " + playerId
                    );
                }
            }

            long now = System.currentTimeMillis();

            for (var entry : copy.entrySet()) {

                db.executeUpdate("""
                        INSERT INTO player_settings (

                            player_id,
                            setting_key,
                            setting_value,
                            updated_at
                        )

                        SELECT

                            player_id,
                            ?,
                            ?,
                            ?

                        FROM players

                        WHERE player_id = ?

                        ON CONFLICT(
                            player_id,
                            setting_key
                        )

                        DO NOTHING
                        """,

                        entry.getKey(),
                        entry.getValue(),
                        now,
                        playerId.toString()
                );
            }

            return null;
        });
    }

    // CHECK IF SETTING EXISTS

    public boolean hasSetting(
            UUID playerId,
            String key
    ) throws Exception {

        return getSetting(playerId, key)
                .isPresent();
    }

    // REMOVE INDIVIDUAL SETTING

    public boolean removeSetting(
            UUID playerId,
            String key
    ) throws Exception {

        Objects.requireNonNull(playerId);

        validateKey(key);

        return database.execute(db ->

                db.executeUpdate("""
                        DELETE FROM player_settings

                        WHERE player_id = ?

                          AND setting_key = ?
                        """,

                        playerId.toString(),
                        key

                ) > 0
        );
    }

    // REMOVE ALL SETTINGS

    public int resetSettings(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db ->

                db.executeUpdate("""
                        DELETE FROM player_settings

                        WHERE player_id = ?
                        """,

                        playerId.toString()
                )
        );
    }

    // COUNT SETTINGS

    public long getSettingCount(
            UUID playerId
    ) throws Exception {

        Objects.requireNonNull(playerId);

        return database.execute(db -> {

            try (ResultSet rs = db.executeQuery("""
                    SELECT COUNT(*) AS total

                    FROM player_settings

                    WHERE player_id = ?
                    """,

                    playerId.toString()
            )) {

                return rs.next()
                        ? rs.getLong("total")
                        : 0L;
            }
        });
    }

    // VALIDATION

    private static void validateKey(String key) {

        Objects.requireNonNull(key, "key");

        if (key.isBlank()) {

            throw new IllegalArgumentException(
                    "Setting key cannot be empty."
            );
        }

        if (key.length() > 128) {

            throw new IllegalArgumentException(
                    "Setting key is too long."
            );
        }

        if (!key.matches("[a-zA-Z0-9._-]+")) {

            throw new IllegalArgumentException(
                    "Invalid setting key: " + key
            );
        }
    }
}
