package activation.Screen.Lobby;

import javax.swing.SwingUtilities;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LobbyRoster {

    public record Entry(
            int playerID,
            String name,
            String computerName
    ) {}

    private final Map<Integer, Entry> players =
            new ConcurrentHashMap<>();

    private volatile Runnable onChanged;

    public void upsert(int playerID, String name, String computerName) {
        players.put(
                playerID,
                new Entry(playerID, name, computerName)
        );

        notifyChanged();
    }

    public List<Entry> snapshot() {
        return players.values().stream()
                .sorted(Comparator.comparingInt(Entry::playerID))
                .toList();
    }

    public void setOnChanged(Runnable callback) {
        this.onChanged = callback;

        if (callback != null) {
            SwingUtilities.invokeLater(callback);
        }
    }

    private void notifyChanged() {
        Runnable callback = onChanged;

        if (callback != null) {
            SwingUtilities.invokeLater(callback);
        }
    }
}
