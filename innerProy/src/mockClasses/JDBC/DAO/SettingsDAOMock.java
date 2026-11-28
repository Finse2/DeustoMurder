
package mockClasses;

import game.settings.SettingKey;
import game.settings.SettingsService;

import utils.JDBC.DatabaseService;
import utils.JDBC.DAO.PlayerDAO;
import utils.JDBC.DAO.SettingsDAO;

import java.sql.SQLException;
import java.util.UUID;

public class SettingsDAOMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== SETTINGS DAO TEST ==="
        );

        DatabaseService database =
                DatabaseService.getInstance();

        PlayerDAO playerDAO =
                new PlayerDAO(database);

        SettingsDAO dao =
                new SettingsDAO(database);

        SettingsService service =
                new SettingsService(database);

        UUID player1 = UUID.randomUUID();

        UUID player2 = UUID.randomUUID();

        try {

            database.initialize();

            // TEST 1: SCHEMA

            check(

                    database.getSchemaVersion() == 3,

                    "Expected schema version 3"
            );

            pass("Schema version 3");

            // TEST 2: CREATE PLAYERS

            playerDAO.createPlayer(
                    player1,
                    "Settings Test Player 1"
            );

            playerDAO.createPlayer(
                    player2,
                    "Settings Test Player 2"
            );

            pass("Player initialization");

            // TEST 3: MISSING SETTING FALLBACK

            check(

                    service.getInt(
                            player1,
                            SettingKey.MASTER_VOLUME
                    ) == 100

                            && service.getBoolean(
                            player1,
                            SettingKey.ANIMATIONS_ENABLED
                    )

                            && dao.getSettingCount(player1) == 0,

                    "Default fallback"
            );

            pass("Missing settings return defaults");

            // TEST 4: INITIALIZE DEFAULT SETTINGS

            service.initialize(player1);

            service.initialize(player2);

            check(

                    dao.getSettingCount(player1)
                            == SettingKey.values().length

                            && dao.getSettingCount(player2)
                            == SettingKey.values().length,

                    "Default initialization"
            );

            pass("Default settings initialized");

            // TEST 5: STRING SETTINGS

            service.setString(

                    player1,

                    SettingKey.LANGUAGE,

                    "es"
            );

            check(

                    service.getString(

                            player1,

                            SettingKey.LANGUAGE

                    ).equals("es"),

                    "String setting"
            );

            pass("String setting persistence");

            // TEST 6: INTEGER SETTINGS

            service.setInt(

                    player1,

                    SettingKey.MASTER_VOLUME,

                    80
            );

            check(

                    service.getInt(

                            player1,

                            SettingKey.MASTER_VOLUME

                    ) == 80,

                    "Integer setting"
            );

            pass("Integer setting persistence");

            // TEST 7: BOOLEAN SETTINGS

            service.setBoolean(

                    player1,

                    SettingKey.FULLSCREEN,

                    true
            );

            check(

                    service.getBoolean(

                            player1,

                            SettingKey.FULLSCREEN
                    ),

                    "Boolean setting"
            );

            pass("Boolean setting persistence");

            // TEST 8: DOUBLE SETTINGS

            service.setDouble(

                    player1,

                    SettingKey.UI_SCALE,

                    1.5
            );

            check(

                    Math.abs(

                            service.getDouble(

                                    player1,

                                    SettingKey.UI_SCALE

                            ) - 1.5

                    ) < 0.00001,

                    "Double setting"
            );

            pass("Double setting persistence");

            // TEST 9: LONG SETTINGS

            dao.setLong(

                    player1,

                    "test.long_value",

                    3_000_000_000L
            );

            check(

                    dao.getLong(

                            player1,

                            "test.long_value",

                            0L

                    ) == 3_000_000_000L,

                    "64-bit setting"
            );

            pass("64-bit integer setting persistence");

            // TEST 10: UPDATE EXISTING SETTING

            service.setInt(

                    player1,

                    SettingKey.MASTER_VOLUME,

                    70
            );

            check(

                    service.getInt(

                            player1,

                            SettingKey.MASTER_VOLUME

                    ) == 70

                            && dao.getSettingCount(player1)
                            == SettingKey.values().length + 1,

                    "Setting update"
            );

            pass("Existing setting updated without duplication");

            // TEST 11: REPEATED INITIALIZATION

            service.initialize(player1);

            check(

                    service.getInt(

                            player1,

                            SettingKey.MASTER_VOLUME

                    ) == 70

                            && service.getString(

                            player1,

                            SettingKey.LANGUAGE

                    ).equals("es"),

                    "Initialization overwrote preferences"
            );

            pass("Repeated initialization preserves preferences");

            // TEST 12: PLAYER ISOLATION

            check(

                    service.getInt(

                            player2,

                            SettingKey.MASTER_VOLUME

                    ) == 100

                            && service.getString(

                            player2,

                            SettingKey.LANGUAGE

                    ).equals("en")

                            && !service.getBoolean(

                            player2,

                            SettingKey.FULLSCREEN
                    ),

                    "Settings leaked between players"
            );

            pass("Player settings isolation");

            // TEST 13: INVALID RANGE

            boolean rejected = false;

            try {

                service.setInt(

                        player1,

                        SettingKey.MASTER_VOLUME,

                        150
                );

            } catch (IllegalArgumentException e) {

                rejected = true;
            }

            check(

                    rejected

                            && service.getInt(

                            player1,

                            SettingKey.MASTER_VOLUME

                    ) == 70,

                    "Invalid volume accepted"
            );

            pass("Invalid setting range rejected");

            // TEST 14: INVALID TYPE

            rejected = false;

            try {

                service.setString(

                        player1,

                        SettingKey.MASTER_VOLUME,

                        "invalid"
                );

            } catch (IllegalArgumentException e) {

                rejected = true;
            }

            check(

                    rejected,

                    "Invalid setting type accepted"
            );

            pass("Invalid setting type rejected");

            // TEST 15: INVALID PORT

            rejected = false;

            try {

                service.setInt(

                        player1,

                        SettingKey.PREFERRED_PORT,

                        70000
                );

            } catch (IllegalArgumentException e) {

                rejected = true;
            }

            check(

                    rejected,

                    "Invalid port accepted"
            );

            pass("Invalid network port rejected");

            // TEST 16: NON-FINITE DOUBLE

            rejected = false;

            try {

                service.setDouble(

                        player1,

                        SettingKey.UI_SCALE,

                        Double.NaN
                );

            } catch (IllegalArgumentException e) {

                rejected = true;
            }

            check(

                    rejected,

                    "NaN accepted"
            );

            pass("Non-finite number rejected");

            // TEST 17: EFFECTIVE AUDIO VOLUME

            service.setInt(

                    player1,

                    SettingKey.MASTER_VOLUME,

                    80
            );

            service.setInt(

                    player1,

                    SettingKey.MUSIC_VOLUME,

                    50
            );

            service.setInt(

                    player1,

                    SettingKey.SOUND_EFFECTS_VOLUME,

                    25
            );

            check(

                    Math.abs(

                            service.getEffectiveMusicVolume(
                                    player1
                            ) - 0.4

                    ) < 0.00001

                            && Math.abs(

                            service.getEffectiveSoundEffectsVolume(
                                    player1
                            ) - 0.2

                    ) < 0.00001,

                    "Effective audio volume"
            );

            pass("Effective audio volume calculation");

            // TEST 18: MUTE AUDIO

            service.setBoolean(

                    player1,

                    SettingKey.MUTE_AUDIO,

                    true
            );

            check(

                    service.getEffectiveMusicVolume(
                            player1
                    ) == 0.0

                            && service.getEffectiveSoundEffectsVolume(
                            player1
                    ) == 0.0

                            && service.getInt(

                            player1,

                            SettingKey.MUSIC_VOLUME

                    ) == 50,

                    "Mute audio"
            );

            service.setBoolean(

                    player1,

                    SettingKey.MUTE_AUDIO,

                    false
            );

            pass("Mute preserves original volume settings");

            // TEST 19: ALL SETTINGS

            var settings =
                    service.getAllSettings(player1);

            check(

                    settings.size()
                            == SettingKey.values().length

                            && settings.get(
                            SettingKey.MASTER_VOLUME
                    ).equals("80")

                            && settings.get(
                            SettingKey.LANGUAGE
                    ).equals("es"),

                    "All settings retrieval"
            );

            pass("All settings retrieval");

            // TEST 20: INDIVIDUAL RESET

            service.resetSetting(

                    player1,

                    SettingKey.MASTER_VOLUME
            );

            check(

                    service.getInt(

                            player1,

                            SettingKey.MASTER_VOLUME

                    ) == 100

                            && !dao.hasSetting(

                            player1,

                            SettingKey.MASTER_VOLUME
                                    .getKey()
                    ),

                    "Individual setting reset"
            );

            pass("Individual setting reset");

            // TEST 21: UNKNOWN PLAYER

            rejected = false;

            try {

                dao.setSetting(

                        UUID.randomUUID(),

                        "test.unknown",

                        "value"
                );

            } catch (SQLException e) {

                rejected = true;
            }

            check(

                    rejected,

                    "Unknown player accepted"
            );

            pass("Unknown player rejected");

            // TEST 22: SQL PARAMETER SAFETY

            String unusualValue =
                    "O'Connor \"test\"; SELECT * FROM players";

            dao.setSetting(

                    player1,

                    "test.special_characters",

                    unusualValue
            );

            check(

                    dao.getSetting(

                            player1,

                            "test.special_characters"

                    ).orElseThrow().equals(unusualValue),

                    "Special character handling"
            );

            pass("Special characters preserved safely");

            // TEST 23: CORRUPTED STORED VALUE

            dao.setSetting(

                    player1,

                    SettingKey.FULLSCREEN.getKey(),

                    "not-a-boolean"
            );

            rejected = false;

            try {

                service.getBoolean(

                        player1,

                        SettingKey.FULLSCREEN
                );

            } catch (SQLException e) {

                rejected = true;
            }

            check(

                    rejected,

                    "Corrupted setting accepted"
            );

            service.setBoolean(

                    player1,

                    SettingKey.FULLSCREEN,

                    true
            );

            pass("Corrupted stored setting detected");

            // TEST 24: SERVER RESTART

            var saved =
                    dao.getAllSettings(player1);

            database.shutdown();

            database.initialize();

            check(

                    saved.equals(
                            dao.getAllSettings(player1)
                    )

                            && service.getString(

                            player1,

                            SettingKey.LANGUAGE

                    ).equals("es"),

                    "Settings persistence after restart"
            );

            pass("Settings survive server restart");

            // TEST 25: RESET ALL SETTINGS

            service.resetAllSettings(player1);

            check(

                    dao.getSettingCount(player1) == 0

                            && service.getInt(

                            player1,

                            SettingKey.MASTER_VOLUME

                    ) == 100

                            && service.getString(

                            player1,

                            SettingKey.LANGUAGE

                    ).equals("en")

                            && !service.getBoolean(

                            player1,

                            SettingKey.FULLSCREEN
                    ),

                    "Reset all settings"
            );

            pass("All settings reset to defaults");

            // TEST 26: RESET PLAYER ISOLATION

            check(

                    dao.getSettingCount(player2)
                            == SettingKey.values().length,

                    "Reset affected another player"
            );

            pass("Reset preserves other players' settings");

            // TEST 27: REINITIALIZATION

            service.initialize(player1);

            check(

                    dao.getSettingCount(player1)
                            == SettingKey.values().length

                            && service.getInt(

                            player1,

                            SettingKey.MASTER_VOLUME

                    ) == 100,

                    "Reinitialization failed"
            );

            pass("Settings reinitialized successfully");

            System.out.println(
                    "All assertions passed."
            );

        } finally {

            try {

                if (database.isInitialized()) {

                    dao.resetSettings(player1);

                    dao.resetSettings(player2);

                    if (playerDAO.playerExists(player1)) {

                        playerDAO.deletePlayer(player1);
                    }

                    if (playerDAO.playerExists(player2)) {

                        playerDAO.deletePlayer(player2);
                    }
                }

            } finally {

                database.shutdown();
            }
        }
    }

    // TEST UTILITIES

    private static void check(
            boolean condition,
            String message
    ) {

        if (!condition) {

            throw new AssertionError(message);
        }
    }

    private static void pass(String message) {

        System.out.println(
                "[PASS] " + message
        );
    }
}
