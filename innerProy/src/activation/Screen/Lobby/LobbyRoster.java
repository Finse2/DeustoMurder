package activation.Screen.Lobby;

import javax.swing.SwingUtilities;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * The LobbyRoster class manages the roster of players in a game lobby.
 * It provides methods to add or update player entries, retrieve a snapshot of the current roster,
 * and set a callback to be notified when the roster changes.
 * The class uses a thread-safe ConcurrentHashMap to store player entries and ensures that callbacks are executed on the Swing event dispatch thread.
 */
public final class LobbyRoster {
/**
     * The Entry record represents a single player entry in the lobby roster.
     * It contains the player's ID, name, and computer name.
     */
    public record Entry(
            int playerID,
            String name,
            String computerName
    ) {}

    private final Map<Integer, Entry> players = new ConcurrentHashMap<>();

    private volatile Runnable onChanged;

/**
     * Adds or updates a player entry in the lobby roster.
     * If an entry with the same player ID already exists, it will be updated with the new name and computer name.
     * After adding or updating the entry, the onChanged callback is notified if it is set.
     *
     * @param playerID The ID of the player to add or update.
     * @param name The name of the player.
     * @param computerName The name of the computer from which the player joined.
     */
    public void upsert(int playerID, String name, String computerName) {
        players.put(
                playerID,
                new Entry(playerID, name, computerName)
        );

        notifyChanged();
    }
/**
     * Returns a snapshot of the current roster of players in the lobby.
     * The snapshot is a list of player entries sorted by player ID.
     *
     * @return A list of Entry objects representing the current roster of players.
     */
    public List<Entry> snapshot() {
        return players.values().stream()
                .sorted(Comparator.comparingInt(Entry::playerID))
                .toList();
    }

    /*
     * Sets a callback to be notified when the roster changes.
     * The callback will be executed n the Swing event dispatch thread.
     * If the callback is set to null, no notifications will be sent.
     *
     * @param callback A Runnable to be executed when the roster changes, or null to disable notifications.
     */
    public void setOnChanged(Runnable callback) {
        this.onChanged = callback;

        if (callback != null) {
            SwingUtilities.invokeLater(callback);
        }
    }
/**
     * Notifies the onChanged callback, if it is set, that the roster has changed.
     * The callback will be executed on the Swing event dispatch thread.
     */
    private void notifyChanged() {
        Runnable callback = onChanged;

        if (callback != null) {
            SwingUtilities.invokeLater(callback);
        }
    }
}
