
package activation.Screen;

import app.ApplicationContext;

import game.profile.PlayerProfileService;
import game.settings.SettingKey;
import game.settings.SettingsService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.util.Map;
import java.util.UUID;

public final class SettingsDialog extends JDialog {

    private final ApplicationContext context;

    private final SettingsService settingsService;

    private final PlayerProfileService profileService;

    private final UUID playerId;

    private final SettingsTableModel model;

    private final JTable table;

    private final JLabel status;

    private final JButton saveButton;

    private final JButton resetButton;

    private final JButton reloadButton;

    // SETTINGS DISPLAYED IN THE TABLE

    private record SettingRow(

            String label,

            SettingKey key

    ) {}

    private static final SettingRow[] ROWS = {

            new SettingRow(
                    "Master Volume",
                    SettingKey.MASTER_VOLUME
            ),

            new SettingRow(
                    "Music Volume",
                    SettingKey.MUSIC_VOLUME
            ),

            new SettingRow(
                    "Sound Effects",
                    SettingKey.SOUND_EFFECTS_VOLUME
            ),

            new SettingRow(
                    "Mute Sound",
                    SettingKey.MUTE_AUDIO
            ),

            new SettingRow(
                    "Brightness",
                    SettingKey.BRIGHTNESS
            ),

            new SettingRow(
                    "Fullscreen",
                    SettingKey.FULLSCREEN
            ),

            new SettingRow(
                    "Animations",
                    SettingKey.ANIMATIONS_ENABLED
            ),

            new SettingRow(
                    "Highlight Possible Moves",
                    SettingKey.HIGHLIGHT_POSSIBLE_MOVES
            ),

            new SettingRow(
                    "Achievement Notifications",
                    SettingKey.ACHIEVEMENT_NOTIFICATIONS
            ),

            new SettingRow(
                    "Confirm Accusations",
                    SettingKey.CONFIRM_ACCUSATIONS
            ),

            new SettingRow(
                    "Player Name",
                    null
            ),

            new SettingRow(
                    "Default Server IP",
                    SettingKey.DEFAULT_SERVER_ADDRESS
            ),

            new SettingRow(
                    "Default Server Port",
                    SettingKey.DEFAULT_SERVER_PORT
            )
    };

    // CONSTRUCTOR

    public SettingsDialog(JFrame owner) {

        super(
                owner,
                "Settings",
                ModalityType.APPLICATION_MODAL
        );

        context =
                ApplicationContext.getInstance();

        settingsService =
                context.getSettingsService();

        profileService =
                context.getProfileService();

        playerId =
                context.getLocalPlayerId();

        model =
                new SettingsTableModel();

        table = new JTable(model) {

            @Override
            public TableCellEditor getCellEditor(
                    int row,
                    int column
            ) {

                Object value =
                        getValueAt(row, column);

                if (column == 1
                        && value instanceof Boolean) {

                    return getDefaultEditor(
                            Boolean.class
                    );
                }

                return super.getCellEditor(
                        row,
                        column
                );
            }

            @Override
            public TableCellRenderer getCellRenderer(
                    int row,
                    int column
            ) {

                Object value =
                        getValueAt(row, column);

                if (column == 1
                        && value instanceof Boolean) {

                    return getDefaultRenderer(
                            Boolean.class
                    );
                }

                return super.getCellRenderer(
                        row,
                        column
                );
            }
        };

        status =
                new JLabel("Loading settings...");

        saveButton =
                createButton("Save");

        resetButton =
                createButton("Restore Defaults");

        reloadButton =
                createButton("Reload");

        initializeInterface();

        loadSettings();
    }

    // INITIALIZE UI

    private void initializeInterface() {

        setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );

        setLayout(new BorderLayout());

        getContentPane().setBackground(
                new Color(20, 22, 27)
        );

        JScrollPane scrollPane =
                CluedoTableTheme.styleConnection(table);

        add(
                scrollPane,
                BorderLayout.CENTER
        );

        status.setForeground(
                new Color(225, 190, 140)
        );

        status.setBorder(
                BorderFactory.createEmptyBorder(
                        8, 10, 8, 10
                )
        );

        add(
                status,
                BorderLayout.NORTH
        );

        JPanel controls =
                new JPanel(
                        new GridLayout(1, 3, 5, 0)
                );

        controls.setBackground(
                new Color(20, 22, 27)
        );

        controls.add(reloadButton);
        controls.add(resetButton);
        controls.add(saveButton);

        add(
                controls,
                BorderLayout.SOUTH
        );

        saveButton.addActionListener(
                event -> saveSettings()
        );

        reloadButton.addActionListener(
                event -> loadSettings()
        );

        resetButton.addActionListener(
                event -> restoreDefaults()
        );

        setSize(600, 580);

        setLocationRelativeTo(getOwner());
    }

    // LOAD SETTINGS

    private void loadSettings() {

        setBusy(true, "Loading settings...");

        new SwingWorker<Object[], Void>() {

            @Override
            protected Object[] doInBackground()
                    throws Exception {

                Map<SettingKey, String> settings =
                        settingsService.getAllSettings(
                                playerId
                        );

                Object[] values =
                        new Object[ROWS.length];

                for (int i = 0;
                     i < ROWS.length;
                     i++) {

                    SettingKey key =
                            ROWS[i].key();

                    if (key == null) {

                        values[i] =
                                profileService
                                        .getDisplayName();

                        continue;
                    }

                    values[i] = convertValue(
                            key,
                            settings.get(key)
                    );
                }

                return values;
            }

            @Override
            protected void done() {

                try {

                    model.setValues(get());

                    setBusy(
                            false,
                            "Settings loaded."
                    );

                } catch (Exception e) {

                    setBusy(
                            false,
                            "Could not load settings."
                    );

                    model.setLoaded(false);

                    saveButton.setEnabled(false);

                    showError(e);
                }
            }

        }.execute();
    }

    // SAVE SETTINGS

    private void saveSettings() {

        if (table.isEditing()
                && !table.getCellEditor()
                .stopCellEditing()) {

            return;
        }

        Object[] values =
                model.copyValues();

        // Validate every row before writing anything.

        try {

            validateValues(values);

        } catch (IllegalArgumentException e) {

            status.setText(e.getMessage());

            showError(e);

            return;
        }

        setBusy(
                true,
                "Saving settings..."
        );

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
                    throws Exception {

                for (int i = 0;
                     i < ROWS.length;
                     i++) {

                    SettingKey key =
                            ROWS[i].key();

                    String value =
                            values[i].toString().trim();

                    if (key == null) {

                        profileService.updateName(
                                value
                        );

                        continue;
                    }

                    switch (key.getType()) {

                        case STRING ->

                                settingsService.setString(
                                        playerId,
                                        key,
                                        value
                                );

                        case INTEGER ->

                                settingsService.setInt(
                                        playerId,
                                        key,
                                        Integer.parseInt(value)
                                );

                        case BOOLEAN ->

                                settingsService.setBoolean(
                                        playerId,
                                        key,
                                        Boolean.parseBoolean(value)
                                );

                        case DOUBLE ->

                                settingsService.setDouble(
                                        playerId,
                                        key,
                                        Double.parseDouble(value)
                                );
                    }
                }

                return null;
            }

            @Override
            protected void done() {

                try {

                    get();

                    setBusy(
                            false,
                            "Settings saved successfully."
                    );

                } catch (Exception e) {

                    setBusy(
                            false,
                            "Some settings may not have saved."
                    );

                    showError(e);
                }
            }

        }.execute();
    }

    // RESTORE DEFAULT VALUES

    private void restoreDefaults() {

        if (table.isEditing()) {

            table.getCellEditor()
                    .cancelCellEditing();
        }

        Object[] values =
                model.copyValues();

        for (int i = 0;
             i < ROWS.length;
             i++) {

            SettingKey key =
                    ROWS[i].key();

            // Preserve the player's name.

            if (key == null) {

                continue;
            }

            values[i] = convertValue(
                    key,
                    key.getDefaultValue()
            );
        }

        model.setValues(values);

        status.setText(
                "Defaults restored. Press Save to apply."
        );
    }

    // VALIDATION

    private void validateValues(
            Object[] values
    ) {

        for (int i = 0;
             i < ROWS.length;
             i++) {

            if (values[i] == null) {

                throw new IllegalArgumentException(
                        ROWS[i].label()
                                + " cannot be empty."
                );
            }

            String value =
                    values[i].toString().trim();

            SettingKey key =
                    ROWS[i].key();

            if (key == null) {

                if (value.isBlank()
                        || value.length() > 64) {

                    throw new IllegalArgumentException(
                            "Player name must contain "
                                    + "1–64 characters."
                    );
                }

            } else {

                key.validate(value);
            }
        }
    }

    // CONVERT DATABASE VALUES

    private static Object convertValue(

            SettingKey key,

            String value

    ) {

        return switch (key.getType()) {

            case STRING -> value;

            case INTEGER ->
                    Integer.parseInt(value);

            case BOOLEAN ->
                    Boolean.parseBoolean(value);

            case DOUBLE ->
                    Double.parseDouble(value);
        };
    }

    // BUSY STATE

    private void setBusy(
            boolean busy,
            String message
    ) {

        table.setEnabled(!busy);

        saveButton.setEnabled(!busy);

        resetButton.setEnabled(!busy);

        reloadButton.setEnabled(!busy);

        status.setText(message);
    }

    // ERROR HANDLING

    private void showError(Exception e) {

        Throwable cause =
                e.getCause() == null
                        ? e
                        : e.getCause();

        CustomOptionPane.showMessageDialog(

                this,

                null,

                cause.getMessage(),

                CustomOptionPane.ERROR
        );
    }

    // BUTTON STYLING

    private static JButton createButton(
            String text
    ) {

        JButton button =
                new JButton(text);

        button.setBackground(
                new Color(75, 38, 42)
        );

        button.setForeground(
                new Color(225, 190, 140)
        );

        button.setFont(
                new Font(
                        "Georgia",
                        Font.BOLD,
                        14
                )
        );

        button.setFocusPainted(false);

        return button;
    }

    // TABLE MODEL

    private static final class SettingsTableModel
            extends AbstractTableModel {

        private Object[] values =
                new Object[ROWS.length];

        private boolean loaded;

        public void setValues(
                Object[] values
        ) {

            this.values = values.clone();

            loaded = true;

            fireTableDataChanged();
        }

        public void setLoaded(boolean loaded) {

            this.loaded = loaded;
        }

        public Object[] copyValues() {

            return values.clone();
        }

        @Override
        public int getRowCount() {

            return ROWS.length;
        }

        @Override
        public int getColumnCount() {

            return 2;
        }

        @Override
        public String getColumnName(
                int column
        ) {

            return column == 0
                    ? "Setting"
                    : "Value";
        }

        @Override
        public boolean isCellEditable(
                int row,
                int column
        ) {

            return loaded && column == 1;
        }

        @Override
        public Object getValueAt(
                int row,
                int column
        ) {

            if (column == 0) {

                return ROWS[row].label();
            }

            return values[row];
        }

        @Override
        public void setValueAt(
                Object value,
                int row,
                int column
        ) {

            if (column != 1) {

                return;
            }

            values[row] = value;

            fireTableCellUpdated(row, column);
        }
    }
}
