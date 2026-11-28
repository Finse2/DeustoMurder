
package game.settings;

import utils.JDBC.DatabaseService;
import utils.JDBC.DAO.SettingsDAO;

import java.sql.SQLException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class SettingsService {

    private final SettingsDAO settingsDAO;

    // CONSTRUCTORS

    public SettingsService() {

        this(DatabaseService.getInstance());
    }

    public SettingsService(DatabaseService database) {

        Objects.requireNonNull(database);

        this.settingsDAO =
                new SettingsDAO(database);
    }

    // INITIALIZE PLAYER SETTINGS

    public void initialize(
            UUID playerId
    ) throws Exception {

        Map<String, String> defaults =
                new LinkedHashMap<>();

        for (SettingKey key : SettingKey.values()) {

            key.validate(
                    key.getDefaultValue()
            );

            defaults.put(

                    key.getKey(),

                    key.getDefaultValue()
            );
        }

        settingsDAO.initializeDefaults(

                playerId,

                defaults
        );
    }

    // GENERIC SETTER

    private void save(
            UUID playerId,
            SettingKey key,
            String value
    ) throws Exception {

        Objects.requireNonNull(playerId);
        Objects.requireNonNull(key);

        key.validate(value);

        settingsDAO.setSetting(

                playerId,

                key.getKey(),

                value
        );
    }

    // SET STRING

    public void setString(
            UUID playerId,
            SettingKey key,
            String value
    ) throws Exception {

        requireType(
                key,
                SettingKey.Type.STRING
        );

        save(playerId, key, value);
    }

    // SET INTEGER

    public void setInt(
            UUID playerId,
            SettingKey key,
            int value
    ) throws Exception {

        requireType(
                key,
                SettingKey.Type.INTEGER
        );

        save(
                playerId,
                key,
                Integer.toString(value)
        );
    }

    // SET BOOLEAN

    public void setBoolean(
            UUID playerId,
            SettingKey key,
            boolean value
    ) throws Exception {

        requireType(
                key,
                SettingKey.Type.BOOLEAN
        );

        save(
                playerId,
                key,
                Boolean.toString(value)
        );
    }

    // SET DOUBLE

    public void setDouble(
            UUID playerId,
            SettingKey key,
            double value
    ) throws Exception {

        requireType(
                key,
                SettingKey.Type.DOUBLE
        );

        save(
                playerId,
                key,
                Double.toString(value)
        );
    }

    // LOAD VALIDATED VALUE

    private String load(
            UUID playerId,
            SettingKey key
    ) throws Exception {

        Objects.requireNonNull(playerId);
        Objects.requireNonNull(key);

        String value = settingsDAO.getString(

                playerId,

                key.getKey(),

                key.getDefaultValue()
        );

        try {

            key.validate(value);

        } catch (IllegalArgumentException e) {

            throw new SQLException(
                    "Invalid stored setting: "
                            + key.getKey(),
                    e
            );
        }

        return value;
    }

    // GET STRING

    public String getString(
            UUID playerId,
            SettingKey key
    ) throws Exception {

        requireType(
                key,
                SettingKey.Type.STRING
        );

        return load(playerId, key);
    }

    // GET INTEGER

    public int getInt(
            UUID playerId,
            SettingKey key
    ) throws Exception {

        requireType(
                key,
                SettingKey.Type.INTEGER
        );

        return Integer.parseInt(
                load(playerId, key)
        );
    }

    // GET BOOLEAN

    public boolean getBoolean(
            UUID playerId,
            SettingKey key
    ) throws Exception {

        requireType(
                key,
                SettingKey.Type.BOOLEAN
        );

        return Boolean.parseBoolean(
                load(playerId, key)
        );
    }

    // GET DOUBLE

    public double getDouble(
            UUID playerId,
            SettingKey key
    ) throws Exception {

        requireType(
                key,
                SettingKey.Type.DOUBLE
        );

        return Double.parseDouble(
                load(playerId, key)
        );
    }

    // GET ALL EFFECTIVE SETTINGS

    public Map<SettingKey, String> getAllSettings(
            UUID playerId
    ) throws Exception {

        Map<String, String> saved =
                settingsDAO.getAllSettings(playerId);

        Map<SettingKey, String> result =
                new EnumMap<>(SettingKey.class);

        for (SettingKey key : SettingKey.values()) {

            String value = saved.getOrDefault(

                    key.getKey(),

                    key.getDefaultValue()
            );

            try {

                key.validate(value);

            } catch (IllegalArgumentException e) {

                throw new SQLException(
                        "Invalid stored setting: "
                                + key.getKey(),
                        e
                );
            }

            result.put(key, value);
        }

        return Collections.unmodifiableMap(result);
    }

    // RESET INDIVIDUAL SETTING

    public void resetSetting(
            UUID playerId,
            SettingKey key
    ) throws Exception {

        Objects.requireNonNull(key);

        settingsDAO.removeSetting(

                playerId,

                key.getKey()
        );
    }

    // RESET ALL SETTINGS

    public void resetAllSettings(
            UUID playerId
    ) throws Exception {

        settingsDAO.resetSettings(playerId);
    }

    // CALCULATE EFFECTIVE AUDIO VOLUME

    public double getEffectiveMusicVolume(
            UUID playerId
    ) throws Exception {

        Map<SettingKey, String> settings =
                getAllSettings(playerId);

        if (Boolean.parseBoolean(
                settings.get(SettingKey.MUTE_AUDIO)
        )) {

            return 0.0;
        }

        double master = Integer.parseInt(
                settings.get(SettingKey.MASTER_VOLUME)
        ) / 100.0;

        double music = Integer.parseInt(
                settings.get(SettingKey.MUSIC_VOLUME)
        ) / 100.0;

        return master * music;
    }

    public double getEffectiveSoundEffectsVolume(
            UUID playerId
    ) throws Exception {

        Map<SettingKey, String> settings =
                getAllSettings(playerId);

        if (Boolean.parseBoolean(
                settings.get(SettingKey.MUTE_AUDIO)
        )) {

            return 0.0;
        }

        double master = Integer.parseInt(
                settings.get(SettingKey.MASTER_VOLUME)
        ) / 100.0;

        double effects = Integer.parseInt(
                settings.get(
                        SettingKey.SOUND_EFFECTS_VOLUME
                )
        ) / 100.0;

        return master * effects;
    }

    // TYPE VALIDATION

    private static void requireType(
            SettingKey key,
            SettingKey.Type expected
    ) {

        Objects.requireNonNull(key);

        if (key.getType() != expected) {

            throw new IllegalArgumentException(

                    key.getKey()

                            + " requires type "

                            + key.getType()

                            + ", not "

                            + expected
            );
        }
    }
}
